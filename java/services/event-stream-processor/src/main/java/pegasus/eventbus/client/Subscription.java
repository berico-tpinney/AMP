package pegasus.eventbus.client;

import cmf.bus.Envelope;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;

import java.util.Map;

public class Subscription {

	IRegistration registration;

	public Subscription(final EnvelopeHandler envelopeHandler) {
		registration = new IRegistration() {

			@Override
			public Object handleFailed(Envelope env, Exception ex) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object handle(Envelope env) throws Exception {
				return envelopeHandler.handleEnvelope(env);
			}

			@Override
			public Map<String, String> getRegistrationInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public IEnvelopeFilterPredicate getFilterPredicate() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public IRegistration getRegistration() {
		return registration;
	}

}
