package org.apache.cxf.transport.xmpp.pubsub;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.xmpp.common.AbstractDestination;
import org.apache.cxf.transport.xmpp.smackx.soap.SoapPacket;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class PubSubDestination extends AbstractDestination implements ItemEventListener<PayloadItem<SoapPacket>>{

    public PubSubDestination(EndpointInfo epInfo) {
        super(epInfo);
    }

    @Override
    public void handlePublishedItems(ItemPublishEvent<PayloadItem<SoapPacket>> events) {       
        for(PayloadItem<SoapPacket> pi : events.getItems())
        {
            System.out.println(pi.getPayload().getChildElementXML());
        }
    }
}
