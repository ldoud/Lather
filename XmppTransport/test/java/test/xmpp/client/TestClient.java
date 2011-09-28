package test.xmpp.client;

import lather.smackx.soap.SoapPacket;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public class TestClient implements PacketListener
{
    private static String message = 
        "<Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\">"+
        "<Body>"+
        "<test:sayHi xmlns:test='http://service.xmpp.test/'>"+
       "        <arg0>World</arg0>"+
        "</test:sayHi>"+
        "</Body>"+
        "</Envelope>";

    private String prefix;
    
    public TestClient(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public void processPacket(Packet msg)
    {
        System.out.println(prefix+" "+msg.toXML());
    }    
    
    public static void main(String[] args) throws Exception
    {
        XMPPConnection xmppConnection = new XMPPConnection("localhost.localdomain");

        xmppConnection.connect();
        xmppConnection.login("user1", "user1");
        System.out.println("Logged in as:"+xmppConnection.getUser());
        
        xmppConnection.addPacketListener(new TestClient("Received"), new PacketFilter() {            
            @Override
            public boolean accept(Packet arg0)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
        
        xmppConnection.addPacketSendingListener(new TestClient("Sent"), new PacketFilter() {            
            @Override
            public boolean accept(Packet arg0)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
        
        SoapPacket iqPacket = new SoapPacket();
        iqPacket.setTo("service1@localhost.localdomain/Smack");
        iqPacket.setEnvelope(message);
//        iqPacket.setFrom("user1@localhost.localdomain");
        xmppConnection.sendPacket(iqPacket);
       
        System.out.println("Sent IQ Packet");
        
        Thread.sleep(10000);
        xmppConnection.disconnect();
    }

}
