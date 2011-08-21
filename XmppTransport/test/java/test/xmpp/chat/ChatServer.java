package test.xmpp.chat;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ChatServer
{
    public static void main(String[] args) throws Exception
    {        
        new ClassPathXmlApplicationContext("server-chat-applicationContext.xml");
        Thread.sleep(30 * 60 * 1000);
    }
}
