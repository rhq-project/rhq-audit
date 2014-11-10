package org.rhq.idgen.common;

import javax.jms.JMSException;

import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.producer.ProducerConnectionContext;

/**
 * Produces requests for IDs, as well as consumes those requests.
 */
public class IDRequestMessageProcessor extends MessageProcessor {

    public IDRequestMessageProcessor(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    public MessageId sendIDRequestMessage(IDRequestMessage idRequestMessage) throws JMSException {
        ProducerConnectionContext context = createProducerConnectionContext(getEndpoint());
        return send(context, idRequestMessage);
    }

    public void listen(BasicMessageListener<IDRequestMessage> listener) throws JMSException {
        listen(createConsumerConnectionContext(getEndpoint()), listener);
    }

    /**
     * @return the endpoint used for all idgen messages
     */
    protected Endpoint getEndpoint() {
        return new Endpoint(Type.QUEUE, "idgen");
    }
}
