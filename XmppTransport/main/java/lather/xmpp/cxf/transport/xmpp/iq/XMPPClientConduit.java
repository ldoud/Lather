package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import lather.smackx.soap.SoapPacket;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public class XMPPClientConduit 
    implements Conduit, PacketListener
{
    // TODO This needs to be dynamic and/or configurable.
    private String targetJid = "service1@localhost.localdomain";
    
    // After messages are received they are passed to this observer.
    private MessageObserver msgObserver;    
    
    // How to deliver the message to the service.
    private XMPPConnection xmppConnection;
    
    // Information about service being called.
    private EndpointInfo endpointInfo;
    private EndpointReferenceType target;
    
    // Messages sent to the service are stored in this table based on 
    // their PacketId so they can be retrieved when a response is received.
    private Hashtable<String, Exchange> exchangeCorrelationTable = new Hashtable<String, Exchange>();
    
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
        // Take the contents of the cached buffer
        // and write them to the service using XMPP.
        CachedOutputStream output = (CachedOutputStream)msg.getContent(OutputStream.class);
        
        // Null indicates this message represents the reply from the service.
        // This means that the request was already sent to the service and a response was received.
        if(output != null)
        {
            StringBuilder soapEnvelope = new StringBuilder();
            output.writeCacheTo(soapEnvelope);
            
            SoapPacket soapOverXmpp = new SoapPacket();
            soapOverXmpp.setEnvelope(soapEnvelope.toString());
            
            // TODO Target JID will have to become dynamic.
    //        String fullJid = targetJid + "/" + endpointInfo.getName().toString(); 
            String fullJid = "service1@localhost.localdomain/{http://service.xmpp.test/}HelloWorldServicePort";
            soapOverXmpp.setTo(fullJid);
            
            // Save the message so it can be used when the response is received.
            exchangeCorrelationTable.put(soapOverXmpp.getPacketID(), msg.getExchange());
            
            // Send the message to the service.
            xmppConnection.sendPacket(soapOverXmpp); 
        }
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
    public void processPacket(Packet xmppResponse)
    {
        // TODO Is there a better input stream than ByteArrayInputStream?
        Message responseMsg = new MessageImpl();
        SoapPacket soapMsg = (SoapPacket)xmppResponse;
        responseMsg.setContent(
                InputStream.class, 
                new ByteArrayInputStream(soapMsg.getChildElementXML().getBytes())
        );        
      
        Exchange msgExchange = exchangeCorrelationTable.remove(xmppResponse.getPacketID());
        msgExchange.setInMessage(responseMsg);

        msgObserver.onMessage(responseMsg);
    }

}
