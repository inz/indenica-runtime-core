package eu.indenica.adaptation;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.osoa.sca.annotations.Property;

import com.google.common.collect.Maps;

import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.ActionEvent;
import eu.indenica.events.Event;

public class DroolsAdaptationEngine implements AdaptationEngine {

	private PubSub pubsub;
	private KnowledgeBase knowledgeBase;
	private StatefulKnowledgeSession session;
	private KnowledgeBuilder knowledgeBuilder;

	private Map<String, Fact> factBuffer = Maps.newHashMap();

	@Property
	public String rules;

	@Override
	public void init() throws Exception {
		this.pubsub = PubSubFactory.getPubSub();
		knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		knowledgeBuilder.add(ResourceFactory
				.newInputStreamResource(new ByteArrayInputStream(rules
						.getBytes())), ResourceType.DRL);
		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		session = knowledgeBase.newStatefulKnowledgeSession();
		session.insert(this);
	}

	@Override
	public void destroy() throws Exception {
		session.dispose();
	}

	@Override
	public void eventReceived(RuntimeComponent source, Event event) {
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

	protected void performAction(Action action) {
		pubsub.publish(this, new ActionEvent(action));
	}
}
