package eu.indenica.adaptation;

import org.drools.*;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.*;

import eu.indenica.common.PubSub;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.monitoring.Event;

public class DroolsAdaptationEngine implements AdaptationEngine {

	private PubSub pubsub;
	private KnowledgeBase knowledgeBase;
	private StatefulKnowledgeSession session;
	
	@Override
	public void init(PubSub pubsub) throws Exception {
		this.pubsub = pubsub;
		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		session = knowledgeBase.newStatefulKnowledgeSession();
	}

	@Override
	public void destroy() throws Exception {
		session.dispose();
	}

	@Override
	public void publish(RuntimeComponent source, Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRule(AdaptationRule rule) {
	}

	@Override
	public void setFact(Fact fact) {
		session.insert(fact);
		session.fireAllRules();
	}


}
