package org.xmpp.cxf.transport.xmpp.chat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.jivesoftware.smack.XMPPConnection;

/**
 * Listens for XMPP IQ packets targeted for this service.
 * Any IQ packets received are used to create CXF messages.
 * The CXF messages are then passed to a message observer for processing.
 * 
 * @author Leon Doud
 */
public class XMPPDestination implements Destination
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
                                new ByteArrayInputStream(message.getBody().getBytes())
                        );        
                        
                        Exchange msgExchange = new ExchangeImpl();
                        msgExchange.setConduit(new XMPPBackChannelConduit(chat));
                        cxfMsg.setExchange(msgExchange);
                        
                        msgObserver.onMessage(cxfMsg);
                        
                    }
                });

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

}
