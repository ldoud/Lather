package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;
import java.io.OutputStream;

import lather.smackx.soap.SoapPacket;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class XMPPBackChannelConduit implements Conduit
{
    private MessageObserver msgObserver = null;
    private SoapPacket soapMsg;
    private XMPPConnection connection;
    
    public XMPPBackChannelConduit(SoapPacket soapMsg, XMPPConnection connection)
    {
        this.soapMsg = soapMsg;
        this.connection = connection;
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

    @Override
    public void close()
    {
        // The XMPP connection stays open long after the reply conduit.
        // The connection belongs to the XMPP transport factory and
        // all the services on the Bus that use XMPP.
    }

    /**
     * The resources for this message should be closed.
     * This will trigger the writing of the SOAP response to the client.
     */
    @Override
    public void close(Message msg) throws IOException
    {
        CachedOutputStream soapResponse = (CachedOutputStream)msg.getContent(OutputStream.class);
        StringBuilder replyMsg = new StringBuilder();
        soapResponse.writeCacheTo(replyMsg);
        
       
        SoapPacket responseIQ = new SoapPacket();
        responseIQ.setType(IQ.Type.RESULT);
        responseIQ.setPacketID(soapMsg.getPacketID());
        responseIQ.setFrom(soapMsg.getTo());
        responseIQ.setTo(soapMsg.getFrom());
        responseIQ.setEnvelope(replyMsg.toString());
        System.out.println("Sending response: "+responseIQ.toXML());
        
        connection.sendPacket(responseIQ);
    }

    @Override
    public EndpointReferenceType getTarget()
    {
        return EndpointReferenceUtils.getAnonymousEndpointReference();
    }

    /**
     * Puts an output stream in the message.
     * The interceptors will write the response into this output stream.
     */
    @Override
    public void prepare(Message msg) throws IOException
    {
        msg.setContent(OutputStream.class, new CachedOutputStream());
    }

}
