package eu.indenica.adaptation.drools;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.indenica.adaptation.AdaptationEngine;
import eu.indenica.adaptation.AdaptationRule;
import eu.indenica.adaptation.AdaptationRuleImpl;
import eu.indenica.adaptation.Fact;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.ActionEvent;
import eu.indenica.events.Event;

@Scope("COMPOSITE")
@EagerInit
public class DroolsAdaptationEngine implements AdaptationEngine {
	private static Logger LOG = LoggerFactory.getLogger();

	private PubSub pubsub;
	private KnowledgeBase knowledgeBase;
	private StatefulKnowledgeSession session;
	private KnowledgeBuilder knowledgeBuilder;

	private Map<String, Fact> factBuffer = Maps.newHashMap();

	@Property
	protected String[] rules;

	@Property
	protected String[] inputEventTypes;

	/**
	 * @param rules
	 *            the rules to set
	 */
	public void setRules(String[] rules) {
		LOG.debug("Setting rules: {}", rules);
		this.rules = rules;
	}

	public void setRules(AdaptationRuleImpl[] rules) {
		LOG.debug("Setting rules: {}", rules);
		Collection<String> ruleStatements = Lists.newArrayList();
		for(AdaptationRule rule : rules)
			ruleStatements.add(rule.getStatement());
		this.rules = Iterables.toArray(ruleStatements, String.class);
	}

	/**
	 * @param inputEventTypes
	 *            the inputEventTypes to set
	 */
	public void setInputEventTypes(String[] inputEventTypes) {
		LOG.debug("Setting input event types: {}", inputEventTypes);
		this.inputEventTypes = inputEventTypes;
	}

	@Init
	@Override
	public void init() throws Exception {
		LOG.info("Initializing Adaptation Engine...");
		this.pubsub = PubSubFactory.getPubSub();
		knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		for(String rule : rules) {
			LOG.debug("Adding rule: {}", rule);
			knowledgeBuilder.add(ResourceFactory
					.newInputStreamResource(new ByteArrayInputStream(rule
							.getBytes())), ResourceType.DRL);
		}
		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		session = knowledgeBase.newStatefulKnowledgeSession();
		session.insert(this);
		LOG.debug("Adaptation Engine started.");
	}

	@Destroy
	@Override
	public void destroy() throws Exception {
		LOG.debug("Stopping Adaptation Engine...");
		session.dispose();
		LOG.info("Adaptation Engine stopped.");
	}

	@Override
	public void eventReceived(RuntimeComponent source, Event event) {
		LOG.debug("Received event {} from {}", source, event);
		updateFact(event);
	}

	/**
	 * @param event
	 * @return
	 */
	private Fact updateFact(Event event) {
		boolean newFact = false;
		if(!factBuffer.containsKey(event.getEventType())) {
			factBuffer.put(event.getEventType(), new Fact());
			newFact = true;
		}

		Fact fact = factBuffer.get(event.getEventType());
		fact.setEvent(event);
		if(newFact)
			setFact(fact);
		session.fireAllRules();
		return fact;
	}

	@Override
	public void addRule(AdaptationRule rule) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFact(Fact fact) {
		session.insert(fact);
	}

	protected void performAction(ActionEvent actionEvent) {
		LOG.info("Perform action {}", actionEvent);
		pubsub.publish(this, actionEvent);
	}
}
