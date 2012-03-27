package org.apache.cxf.transport.xmpp.common;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.XMPPConnection;

public abstract class AbstractConduit implements Conduit, XMPPConnectionUser {
    
    // After messages are received they are passed to this observer.
    private MessageObserver msgObserver;
    
    // XMPP connection that might be shared with other destinations.
    private XMPPConnection connection;

    // Information about service being called.
    private EndpointReferenceType target;
    
    public AbstractConduit(EndpointReferenceType refType) {
        target = refType;
    }

    @Override
    public MessageObserver getMessageObserver() {
        return msgObserver;
    }

    @Override
    public void setMessageObserver(MessageObserver observer) {
        msgObserver = observer;
    }

    @Override
    public void setXmppConnection(XMPPConnection conn) {
        connection = conn;
    }

    public XMPPConnection getXmppConnection() {
        return connection;
    }

    @Override
    public void close() {
        // Nothing
    }
    
    @Override
    public EndpointReferenceType getTarget() {
        return target;
    }

    @Override
    public void prepare(Message msg) throws IOException {
        msg.setContent(OutputStream.class, new CachedOutputStream());
    }
    
    @Override
    public abstract void close(Message msg) throws IOException;
}
