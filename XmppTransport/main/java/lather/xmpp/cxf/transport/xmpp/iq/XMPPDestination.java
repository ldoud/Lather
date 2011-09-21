package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;

import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Listens for XMPP IQ packets targeted for this service.
 * Any IQ packets received are used to create CXF messages.
 * The CXF messages are then passed to a message observer for processing.
 * 
 * @author Leon Doud
 */
public class XMPPDestination implements Destination, PacketListener
{
    private XMPPConnection xmppConnection;
    
    // Values initialized during construction. 
    private EndpointReferenceType epRefType = new EndpointReferenceType();
    
    // After messages are received they are passed to this observer.
    private MessageObserver msgObserver = null;
    
    public XMPPDestination(XMPPConnection xmppConnection, EndpointInfo epInfo)
    {
        this.xmppConnection = xmppConnection;
        
        // Initialize the address of the epRefType member.
        AttributedURIType address = new AttributedURIType();
        address.setValue(epInfo.getAddress());
        epRefType.setAddress(address);
     
        xmppConnection.addPacketListener(this, new PacketFilter() {            
            @Override
            public boolean accept(Packet arg0)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
        
    }
    
    /**
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public void setMessageObserver(MessageObserver msgObserver)
    {
        this.msgObserver = msgObserver;
    }

    /**
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public EndpointReferenceType getAddress()
    {
        return epRefType;
    }

    /**
     * Not used.
     * The back channel is set on the exchange of the message 
     * when the message is received.
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public Conduit getBackChannel(Message inMsg, Message notUsedMsg,
            EndpointReferenceType notUsedEpRefType) throws IOException
    {
        return null;
    }

    /**
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public MessageObserver getMessageObserver()
    {
        return msgObserver;
    }

    /**
     * Log out of XMPP.
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public void shutdown()
    {
       xmppConnection.disconnect();
    }

    @Override
    public void processPacket(Packet msg)
    {
       System.out.println("Packet received: "+msg.toXML());
        
    }

}
