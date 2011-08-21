package org.apache.cxf.transport.xmpp.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.workgroup.settings.GenericSettings;

/**
 * Listens for XMPP IQ packets targeted for this service.
 * Any IQ packets received are used to create CXF messages.
 * The CXF messages are then passed to a message observer for processing.
 * 
 * @author Leon Doud
 */
public class XMPPDestination implements Destination, PacketListener
{
    private static final String XMPP_CHAT_MSG = "org.apache.cxf.transport.xmpp.XMPPDestination.CHAT";
    // Set during construction.
    private EndpointInfo epInfo;
    private XMPPConnection xmppConnection;
    
    // Values initialized during construction. 
    private EndpointReferenceType epRefType = new EndpointReferenceType();
    
    // After messages are received they are passed to this observer.
    private MessageObserver msgObserver = null;
    
    public XMPPDestination(XMPPConnection xmppConnection, EndpointInfo epInfo)
    {
        this.xmppConnection = xmppConnection;
        this.epInfo = epInfo;
        
        // Initialize the address of the epRefType member.
        AttributedURIType address = new AttributedURIType();
        address.setValue(epInfo.getAddress());
        epRefType.setAddress(address);
        
        // Receive SOAP via IQ.
//        xmppConnection.addPacketListener(this, new PacketFilter() {
//            
//            @Override
//            public boolean accept(Packet msg)
//            {                
//                // TODO Make a real filter.
//                if (msg instanceof GenericSettings)
//                {
//                    System.out.println("Filter accepted packet: "+msg.toXML());
//                    return true;
//                }
//                
//                System.out.println("Filter rejected packet: "+msg.toXML());
//                return false;
//            }
//        });
        
        // Receive SOAP via chat.
        xmppConnection.getChatManager().addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean local)
            {
                System.out.println("Starting chat with: "+chat.getParticipant());
                chat.addMessageListener(new MessageListener() {
                
                    @Override
                    public void processMessage(Chat chat,
                            org.jivesoftware.smack.packet.Message message)
                    {
                        System.out.println("Processing chat message: "+message.getBody());
                        Message cxfMsg = new MessageImpl();
                        cxfMsg.setContent(
                                InputStream.class, 
                                new StringBufferInputStream(message.getBody())
                        );        
                      
                        
                        Exchange msgExchange = new ExchangeImpl();
                        msgExchange.setConduit(new XMPPBackChannelConduit(chat));
//                        msgExchange.setSynchronous(false);
                        cxfMsg.setExchange(msgExchange);
                        
                        
                        //TODO properly set the rest of the CXF message.
                        
                        msgObserver.onMessage(cxfMsg);
                        
                    }
                });

            }
        });
        
//        System.out.println("XMPP Destination created for: "+xmppConnection.getUser());
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
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public Conduit getBackChannel(Message inMsg, Message notUsedMsg,
            EndpointReferenceType notUsedEpRefType) throws IOException
    {
        // TODO Auto-generated method stub
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
     * The destination will stop listening for XMPP packets.
     * Required by the Destination interface.
     * @see org.apache.cxf.transport.Destination
     */
    @Override
    public void shutdown()
    {
        xmppConnection.removePacketListener(this);
    }

    /**
     * Triggered when an XMPP packet matching the filter is received.
     * This method is serviced by one thread so the processing 
     * should be kept to a minimum.
     * Required by the PacketListener interface.
     * @see org.jivesoftware.smack.PacketListener
     */
    @Override
    public void processPacket(Packet msg)
    {
        
        
        // Blind cast is fine because the filter checks this.
        GenericSettings iqMsgWithSoap = (GenericSettings)msg; 
        System.out.println("Accepted packet with: "+iqMsgWithSoap.getQuery());
        
        Message cxfMsg = new MessageImpl();
        cxfMsg.setContent(
                InputStream.class, 
//                new StringBufferInputStream(iqMsgWithSoap.getChildElementXML())
//                new StringBufferInputStream(message)
                  new StringBufferInputStream(iqMsgWithSoap.getQuery())
        );
        
//        Exchange msgExchange = cxfMsg.getExchange();
//        msgExchange.setConduit(arg0);
//        msgExchange.setDestination(this);
//        msgExchange.setInMessage(cxfMsg);
//        msgExchange.setSynchronous(true);
     
        msgObserver.onMessage(cxfMsg);
    }
    
    private static String message = 
    "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>"+
    "<soap:Body>"+
    "<test:sayHi xmlns:test='http://server.xmpp.test/'>"+
   "        <arg0>World</arg0>"+
    "</test:sayHi>"+
    "</soap:Body>"+
    "</soap:Envelope>";
}
