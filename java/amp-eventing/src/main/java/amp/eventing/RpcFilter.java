package amp.eventing;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.eventing.EnvelopeHelper;
import amp.eventing.EventContext.Directions;


public class RpcFilter implements IEventProcessor {

    protected static final Logger log = LoggerFactory.getLogger(RpcFilter.class);
    protected List<UUID> sentRequests;

    
    public RpcFilter() {
        sentRequests = new ArrayList<UUID>();
    }

    
    @Override
    public void processEvent(EventContext context, IContinuationCallback continuation) throws Exception {
    	
    	if (Directions.In == context.getDirection()) {
    		this.processInbound(context, continuation);
    	}
    	else if (Directions.Out == context.getDirection()) {
    		this.processOutbound(context, continuation);
    	}
    }
    
    
    public void processInbound(EventContext context, IContinuationCallback continuation) throws Exception {

        boolean ourOwnRequest = false;
        EnvelopeHelper env = new EnvelopeHelper(context.getEnvelope());

        try {
            if (env.IsRequest()) {
                UUID requestId = env.getMessageId();

                synchronized (this) {
                    if (sentRequests.contains(requestId)) {
                        log.info("Filtering out our own request: " + requestId.toString());
                        ourOwnRequest = true;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to inspect an incoming event for potential filtering", ex);
        }

        if (!ourOwnRequest) { continuation.continueProcessing(); }
    }

    public void processOutbound(EventContext context, IContinuationCallback continuation) throws Exception {

        EnvelopeHelper env = new EnvelopeHelper(context.getEnvelope());

        if (env.IsRequest()) {
            final UUID requestId = env.getMessageId();
            Duration timeout = env.getRpcTimeout();

            synchronized (this) {
                log.debug(String.format("Adding requestId %s to the RPC Filter list", requestId.toString()));
                sentRequests.add(requestId);
            }

            if (timeout == Duration.ZERO) {
                Timer gc = new Timer(true);
                gc.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        requestTimeout_GarbageCollect(requestId);
                    }

                }, timeout.getMillis() * 2);
            } else {
                log.warn(String.format(
                                "Request %s was sent without a timeout: it will never be removed from the RPC Filter list",
                                requestId.toString()));
            }
        }
        
        continuation.continueProcessing();
    }

    public void requestTimeout_GarbageCollect(UUID requestId) {
        synchronized (this) {
            log.debug(String.format("Removing requestId %s from the RPC Filter list", requestId.toString()));
            sentRequests.remove(requestId);
        }
    }
}
