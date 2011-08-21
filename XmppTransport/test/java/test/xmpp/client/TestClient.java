package test.xmpp.client;

import org.apache.cxf.transport.xmpp.XMPPBackChannelConduit;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.workgroup.settings.GenericSettings;

public class TestClient
{
    public static void main(String[] args) throws Exception
    {
        XMPPConnection xmppConnection = new XMPPConnection("localhost.localdomain");
        xmppConnection.connect();
        xmppConnection.login("user1", "user1");
        System.out.println("Logged in as:"+xmppConnection.getUser());
        
        
        GenericSettings iqPacket = new GenericSettings();
        iqPacket.setTo("service1@127.0.0.1");
        iqPacket.setQuery("message");
        xmppConnection.sendPacket(iqPacket);
        System.out.println("Sent IQ Packet");
        
        
        Chat chat = xmppConnection.getChatManager().createChat("service1@127.0.0.1", new MessageListener() {
            
            @Override
            public void processMessage(Chat chat, Message message) {
                System.out.println("Received message: " + message);
            }
        });
        System.out.println("Created chat");
        
        Thread.sleep(10000);

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
