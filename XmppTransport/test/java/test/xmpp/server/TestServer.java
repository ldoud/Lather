package test.xmpp.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestServer
{
    public static void main(String[] args) throws Exception
    {        
        new ClassPathXmlApplicationContext("server-applicationContext.xml");
        Thread.sleep(30 * 60 * 1000);
    }
}
