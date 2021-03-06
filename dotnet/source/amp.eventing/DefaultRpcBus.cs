﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Common.Logging;

using cmf.bus;
using amp.bus;
using cmf.eventing;
using cmf.eventing.patterns.rpc;

namespace amp.eventing
{
    public class DefaultRpcBus : DefaultEventBus, IRpcEventBus
    {
        public DefaultRpcBus(DefaultEnvelopeBus envBus)
            : base(envBus)
        {
        }


        public object GetResponseTo(object request, TimeSpan timeout, string expectedTopic)
        {
            _log.Debug("Enter GetResponseTo");

            if (null == request) { throw new ArgumentNullException("Cannot get response to a null request"); }

            // the response we're going to get
            object response = null;

            try
            {
                // create the ID for the request and set it on the envelope
                Guid requestId = Guid.NewGuid();
                Envelope env = new Envelope();
                env.SetMessageId(requestId);

                // add pattern & timeout information to the headers
                env.SetMessagePattern(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC);
                env.SetRpcTimeout(timeout);

                // create an event context
                EventContext context = new EventContext(EventContext.Directions.Out, env, request);

                // let the outbound processor do its thing
                this.ProcessEvent(context, this.OutboundChain.Sort(), () =>
                {
                    // create an RPC registration
                    RpcRegistration rpcRegistration = new RpcRegistration(requestId, expectedTopic, this.ProcessInbound);

                    // register with the envelope bus
                    _envBus.Register(rpcRegistration);

                    // now that we're setup to receive the response, send the request
                    _envBus.Send(context.Envelope);

                    // get the envelope from the registraton
                    response = rpcRegistration.GetResponse(timeout);

                    // unregister from the bus
                    _envBus.Unregister(rpcRegistration);
                });
            }
            catch (Exception ex)
            {
                _log.Error("Exception publishing an event", ex);
                throw;
            }

            _log.Debug("Leave GetResponseTo");
            return response;
        }

        public TResponse GetResponseTo<TResponse>(object request, TimeSpan timeout) where TResponse : class
        {
            string expectedTopic = typeof(TResponse).FullName;

            object responseObject = this.GetResponseTo(request, timeout, expectedTopic);

            return responseObject as TResponse;
        }

        public IEnumerable GatherResponsesTo(object request, TimeSpan timeout, params string[] expectedTopics)
        {
            throw new NotImplementedException();
        }

        public IEnumerable<TResponse> GatherResponsesTo<TResponse>(object request, TimeSpan timeout) where TResponse : class
        {
            throw new NotImplementedException();
        }

        public void RespondTo(IDictionary<string, string> headers, object response)
        {
            _log.Debug("Enter RespondTo");

            if (null == response) { throw new ArgumentNullException("Cannot respond with a null event"); }
            if (null == headers) { throw new ArgumentNullException("Must provide non-null request headers"); }
            if (Guid.Empty == headers.GetMessageId()) { throw new ArgumentNullException("Cannot respond to a request because the provided request headers do not contain a message ID"); }
            
            try
            {
                Envelope env = new Envelope();
                env.SetCorrelationId(headers.GetMessageId());

                EventContext context = new EventContext(EventContext.Directions.Out, env, response);

                this.ProcessEvent(context, this.OutboundChain.Sort(), () =>
                {
                    _envBus.Send(env);
                });
            }
            catch (Exception ex)
            {
                _log.Error("Exception responding to an event", ex);
                throw;
            }

            _log.Debug("Leave RespondTo");
        }


        protected virtual object ProcessInbound(Envelope env)
        {
            object ev = null;
            EventContext context = new EventContext(EventContext.Directions.In, env);

            this.ProcessEvent(context, this.InboundChain.Sort(), () =>
            {
                _log.Info("Completed inbound processing - returning event");
                ev = context.Event;
            });

            return ev;
        }
    }
}
