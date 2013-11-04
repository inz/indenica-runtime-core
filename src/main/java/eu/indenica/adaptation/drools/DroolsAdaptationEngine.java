package eu.indenica.adaptation.drools;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import eu.indenica.adaptation.AdaptationEngine;
import eu.indenica.adaptation.AdaptationRule;
import eu.indenica.adaptation.AdaptationRuleImpl;
import eu.indenica.adaptation.Fact;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.events.ActionEvent;
import eu.indenica.events.Event;
import eu.indenica.messaging.ManagementClient;
import eu.indenica.messaging.ManagementNameProvider;

@Scope("COMPOSITE")
@EagerInit
public class DroolsAdaptationEngine implements AdaptationEngine {
    private static Logger LOG = LoggerFactory.getLogger();

    private PubSub pubsub;
    private KnowledgeBase knowledgeBase;
    private StatefulKnowledgeSession session;
    private ExecutorService executor;

    private Map<String, Object> globals = Maps.newHashMap();
    private Map<String, Fact> factBuffer = Maps.newHashMap();

    // @Property
    protected AdaptationRule[] rules;

    @Property
    protected String[] inputEventTypes;

    private String hostName;

    private ManagementClient mgmtClient;

    // /**
    // * @param rules
    // * the rules to set
    // */
    // public void setRules(String[] rules) {
    // LOG.debug("Setting rules: {}", rules);
    // this.rules = rules;
    // }

    @Property
    public void setRules(AdaptationRuleImpl[] rules) {
        LOG.debug("Setting rules: {}", rules);
        this.rules = rules;
    }

    /**
     * @param inputEventTypes
     *            the inputEventTypes to set
     */
    public void setInputEventTypes(String[] inputEventTypes) {
        LOG.debug("Setting input event types: {}", inputEventTypes);
        this.inputEventTypes = inputEventTypes;
    }

    /**
     * Sets a global value in the rule context
     * 
     * @param identifier
     *            the global identifier
     * @param value
     *            the value assigned to the global identifier
     */
    public void setGlobal(String identifier, Object value) {
        globals.put(identifier, value);
        setGlobals();
    }

    private void setGlobals() {
        if(session == null)
            return;
        for(Map.Entry<String, Object> global : globals.entrySet()) {
            if(session.getGlobal(global.getKey()) == null) {
                addDummyRule(global.getKey(), global.getValue().getClass());
            }
            session.setGlobal(global.getKey(), global.getValue());
        }
    }

    @Init
    @Override
    public void init() throws Exception {
        LOG.info("Initializing Adaptation Engine...");
        this.pubsub = PubSubFactory.getPubSub();

        knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();

        setGlobal("engine", this);
        if(rules != null)
            for(AdaptationRule rule : rules)
                addRule(rule);

        session = knowledgeBase.newStatefulKnowledgeSession();
        setGlobals();

        executor = Executors.newSingleThreadExecutor();
        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                LOG.trace("Firing rules...");
                session.fireUntilHalt();
                return null;
            }
        });

        if(hostName == null) {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
            LOG.warn("Node name not set, using hostname ({}).", hostName);
        }
        mgmtClient = new ManagementClient(hostName, SERVICE_NAME);
        registerManagementListener();

        LOG.debug("Adaptation Engine started.");
    }

    @Destroy
    @Override
    public void destroy() throws Exception {
        LOG.debug("Stopping Adaptation Engine...");
        session.halt();
        session.dispose();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        pubsub.destroy();
        LOG.info("Adaptation Engine stopped.");
    }

    @Override
    public void eventReceived(String source, Event event) {
        LOG.debug("Received event {} from {}", event, source);
        if(!(event instanceof Fact))
            LOG.warn("Event received is not fact event!");
        updateFact(event);
    }

    /**
     * @param event
     * @return
     */
    private Fact updateFact(Event event) {
        boolean newFact = false;
        if(!factBuffer.containsKey(event.getEventType())) {
            factBuffer.put(event.getEventType(), (Fact) event);
            newFact = true;
        }

        LOG.info("Update fact {}", event);

        // TODO: probably need to update fact using session.update();

        Fact fact = factBuffer.get(event.getEventType());
        fact.setEvent(event);
        if(newFact)
            setFact(fact);
        // session.fireAllRules();
        return fact;
    }

    @Override
    public void addRule(AdaptationRule rule) {
        LOG.debug("Adding rule: {}", rule);
        KnowledgeBuilder knowledgeBuilder =
                KnowledgeBuilderFactory.newKnowledgeBuilder();
        knowledgeBuilder.add(ResourceFactory
                .newInputStreamResource(new ByteArrayInputStream(rule
                        .getStatement().getBytes(Charsets.UTF_8))),
                ResourceType.DRL);
        registerInputEventTypes(rule);
        if(knowledgeBuilder.hasErrors())
            LOG.error("Errors: {}", knowledgeBuilder.getErrors());
        knowledgeBase.addKnowledgePackages(knowledgeBuilder
                .getKnowledgePackages());
    }

    /**
     * Add noop rule referencing the specified global value.
     */
    private void addDummyRule(String name, Class<?> clazz) {
        LOG.debug("Adding access rule for global '{}'...", name);
        AdaptationRuleImpl rule = new AdaptationRuleImpl();

        StringBuilder stmt = new StringBuilder();
        stmt.append("package ").append(getClass().getPackage().getName())
                .append(";\n");
        stmt.append("global ").append(clazz.getCanonicalName()).append(" ")
                .append(name).append(";\n");

        stmt.append("rule \"Global ").append(name).append("\"\n")
                .append("when eval(false)\n")
                .append("then throw new RuntimeException(\"Unexpected!\");\n")
                .append("end");
        rule.setStatement(stmt.toString());
        addRule(rule);
    }

    /**
     * @param rule
     */
    private void registerInputEventTypes(AdaptationRule rule) {
        if(rule.getInputEventTypes() == null) {
            LOG.debug("No input events for rule {}", rule);
            return;
        }
        for(String eventType : rule.getInputEventTypes()) {
            String source = null;
            if(eventType.contains(",")) {
                String[] split = eventType.split(",", 2);
                eventType = split[1].trim();
                source = split[0].trim();
                // FIXME: Correctly get RuntimeComponent reference to register.
                LOG.trace("Found source: {}", source);
            }
            LOG.debug("Register listener for {} from {}", eventType, source);
            pubsub.registerListener(this, null, eventType);
        }
    }

    @Override
    public void setFact(Fact fact) {
        LOG.debug("Add new fact: {}", fact);
        session.insert(fact);
    }

    public void performAction(ActionEvent actionEvent) {
        LOG.info("Perform action {}", actionEvent);
        pubsub.publish(this.getClass().getSimpleName(), actionEvent);
    }

    public void publishEvent(final Event event) throws Exception {
        LOG.info("Publishing event {}", event);
        final String component = this.getClass().getSimpleName();
        new Callable<Void>() {
            public Void call() throws Exception {
                pubsub.publish(component, event);
                return null;
            }
        }.call();

    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.common.RuntimeComponent#setHostName(java.lang.String)
     */
    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Registers a control interface listener with the messaging fabric.
     * 
     * <p>
     * This allows for the addition and removal of rules at runtime.
     * 
     * <p>
     * Control messages must be sent to the appropriate queue. The queue name is
     * designed as follows:
     * 
     * <pre>
     *   {@code <prefix>.<node-name>.<service-name>}
     * </pre>
     * 
     * <ul>
     * <li> {@code <prefix>} is the control infrastructure queue name prefix,
     * {@link ManagementNameProvider#MANAGEMENT_PREFIX}
     * <li> {@code <node-name>} is the node name assigned by the runtime
     * configuration, or the current machine's host name if no node name was set
     * <li> {@code <service-name>} is {@code monitoring}
     * </ul>
     * 
     * @throws JMSException
     *             if something goes wrong
     */
    private void registerManagementListener() throws JMSException {
        LOG.debug("Connecting management listener...");
        mgmtClient.registerListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String command = null;
                AdaptationRuleImpl rule = new AdaptationRuleImpl();
                try {
                    LOG.debug("Received message {}", message);
                    command = message.getStringProperty("command");
//                    ruleName = message.getStringProperty("ruleName");
                    String stmt = message.getStringProperty("ruleStatement");
                    @SuppressWarnings("unchecked")
                    List<String> inputEventTypes =
                            (List<String>) message
                                    .getObjectProperty("ruleInputEventTypes");
                    rule.setStatement(stmt);
                    if(inputEventTypes != null)
                        rule.setInputEventTypes(inputEventTypes
                                .toArray(new String[inputEventTypes.size()]));
                } catch(JMSException e) {
                    LOG.error("Error processing message!", e);
                } catch(ClassCastException e) {
                    LOG.error("Received object was not an adaptation rule!", e);
                }

                if("addRule".equals(command)) {
                    addRule(rule);
                } else if("removeRule".equals(command)) {
                    // removeRule(rule);
                } else {
                    LOG.error("Could not understand message {}", message);
                }
            }
        });
    }
}
