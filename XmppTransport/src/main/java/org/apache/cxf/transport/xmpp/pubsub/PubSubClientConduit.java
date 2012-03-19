package org.apache.cxf.transport.xmpp.pubsub;

import java.io.IOException;

import org.apache.cxf.message.Message;
import org.apache.cxf.transport.xmpp.common.AbstractConduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class PubSubClientConduit extends AbstractConduit {
    
    public PubSubClientConduit(EndpointReferenceType type) {
        super(type);
    }

    @Override
    public void close(Message msg) throws IOException {
        // TODO Auto-generated method stub
    }

}
