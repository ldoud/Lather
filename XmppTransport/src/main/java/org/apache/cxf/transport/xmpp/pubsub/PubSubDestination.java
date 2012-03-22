package org.apache.cxf.transport.xmpp.pubsub;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.xmpp.common.AbstractDestination;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class PubSubDestination extends AbstractDestination implements ItemEventListener<PayloadItem<SimplePayload>>{

    public PubSubDestination(EndpointInfo epInfo) {
        super(epInfo);
    }

    @Override
    public void handlePublishedItems(ItemPublishEvent<PayloadItem<SimplePayload>> events) {   
        
        for(PayloadItem<SimplePayload> pi : events.getItems())
        {
            SimplePayload soapMsg = pi.getPayload();
            
            Message cxfMsg = new MessageImpl();
            cxfMsg.setContent(InputStream.class,
                              new ByteArrayInputStream(soapMsg.toXML().getBytes()));
    
            Exchange msgExchange = new ExchangeImpl();
            msgExchange.setOneWay(true);
            msgExchange.setDestination(this);
            cxfMsg.setExchange(msgExchange);
    
            // TODO Fix this so a different thread is used.
            getMessageObserver().onMessage(cxfMsg);
        }
    }
}
