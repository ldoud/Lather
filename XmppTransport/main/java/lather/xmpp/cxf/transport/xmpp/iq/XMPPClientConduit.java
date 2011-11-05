package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;
import java.io.OutputStream;

import lather.smackx.soap.SoapPacket;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

public class XMPPClientConduit 
    implements Conduit, PacketListener
{
    // TODO This needs to be dynamic and/or configurable.
    private String targetJid = "service1@localhost.localdomain";
    
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
        xmppConnection.addPacketListener(this, new PacketFilter() {
            
            @Override
            public boolean accept(Packet arg0)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
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
        // Clean up the resources used for message correlation.
        System.out.println("closing time...");
        
        // Take the contents of the cached buffer
        // and write them to the service using XMPP.
        CachedOutputStream output = (CachedOutputStream)msg.getContent(OutputStream.class);
        StringBuilder soapEnvelope = new StringBuilder();
        output.writeCacheTo(soapEnvelope);
        
        SoapPacket soapOverXmpp = new SoapPacket();
        soapOverXmpp.setEnvelope(soapEnvelope.toString());
        
//        soapOverXmpp.setPacketID(soapMsg.getPacketID());
//        soapOverXmpp.setFrom(soapMsg.getTo());
//        soapOverXmpp.setTo(soapMsg.getFrom());
        
        // TODO Target JID will have to become dynamic.
//        String fullJid = targetJid + "/" + endpointInfo.getName().toString(); 
        String fullJid = "service1@localhost.localdomain/{http://service.xmpp.test/}HelloWorldServicePort";
        System.out.println("Sending message: "+soapEnvelope.toString());
        System.out.println("Sending to: "+fullJid);
        soapOverXmpp.setTo(fullJid);
        
        xmppConnection.sendPacket(soapOverXmpp);   
    }

    @Override
    public EndpointReferenceType getTarget()
    {
        return target;
    }

    @Override
    public void prepare(Message msg) throws IOException
    {   
        msg.setContent(OutputStream.class, new CachedOutputStream());
    }

    /**
     * Triggered when a response is received.
     * Note this conduit can call many services.
     * Responses will need to be correlated with their requests.
     */
    @Override
    public void processPacket(Packet responseMsg)
    {
        // When an XMPP message is received
        // find the exchange that should receive it.
        System.out.println("Response received: "+responseMsg.toXML());
    }

}
