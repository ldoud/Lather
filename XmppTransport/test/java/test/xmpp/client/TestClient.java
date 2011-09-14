package test.xmpp.client;

import org.apache.cxf.transport.xmpp.chat.XMPPBackChannelConduit;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.packet.SoapPacket;
import org.jivesoftware.smackx.workgroup.settings.GenericSettings;

public class TestClient
{
    public static void main(String[] args) throws Exception
    {

//        XMPPConnection.DEBUG_ENABLED = true;
        
        XMPPConnection xmppConnection = new XMPPConnection("localhost.localdomain");
//        XMPPConnection xmppConnection = new XMPPConnection();

        xmppConnection.connect();
        xmppConnection.login("user1", "user1");
        System.out.println("Logged in as:"+xmppConnection.getUser());
        

        
        SoapPacket iqPacket = new SoapPacket();
        iqPacket.setTo("service1@localhost.localdomain");
        iqPacket.setEnvelope(message);
//        iqPacket.setPacketID(Math.random()+"");
        xmppConnection.sendPacket(iqPacket);
       
        System.out.println("Sent IQ Packet");
        
        Thread.sleep(10000);
        xmppConnection.disconnect();

    }
    
    private static String message = 
        "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>"+
        "<soap:Body>"+
        "<test:sayHi xmlns:test='http://service.xmpp.test/'>"+
       "        <arg0>World</arg0>"+
        "</test:sayHi>"+
        "</soap:Body>"+
        "</soap:Envelope>";    
}
