package test.xmpp.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import test.xmpp.service.HelloWorld;

public class TestCxfClient
{
    public static void main(String[] args)
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("client-iq-applicationContext.xml");
        HelloWorld client = (HelloWorld) context.getBean("helloClient");
        client.sayHi("Fred");

    }
}
