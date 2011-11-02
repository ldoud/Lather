package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;

public class XMPPClientConduit 
    implements Conduit, CachedOutputStreamCallback, PacketListener
{
    // After messages are received they are passed to this observer.
    private MessageObserver msgObserver;    
    
    private XMPPConnection xmppConnection;
    
    private EndpointInfo endpointInfo;
    
    private EndpointReferenceType target;
    
    public XMPPClientConduit(
            EndpointInfo endpointInfo, 
            EndpointReferenceType target, 
            XMPPConnection xmppConnection)
    {
        this.endpointInfo = endpointInfo;
        this.target = target;
        this.xmppConnection = xmppConnection;
    }

    @Override
    public MessageObserver getMessageObserver()
    {
        return msgObserver;
    }

    @Override
    public void setMessageObserver(MessageObserver msgObserver)
    {
        this.msgObserver = msgObserver;
    }

    /**
     * Closes the XMPP connection that is used to send and receive messages.
     */
    @Override
    public void close()
    {
        xmppConnection.disconnect();
    }

    @Override
    public void close(Message msg) throws IOException
    {
        // TODO Auto-generated method stub
        // Is there resources to release per message?
    }

    @Override
    public EndpointReferenceType getTarget()
    {
        return target;
    }

    @Override
    public void prepare(Message msg) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * Triggered when a client is ready to send a message.
     */
    @Override
    public void onClose(CachedOutputStream arg0)
    {
        // Take the contents of the cached buffer
        // and write them to the service using XMPP.
        
    }

    @Override
    public void onFlush(CachedOutputStream arg0)
    {
        // Do nothing when a flush occurs.
    }

    @Override
    public void processPacket(Packet arg0)
    {
        // When an XMPP message is received
        // find the exchange that should receive it.
        
    }

}
