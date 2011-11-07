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
        
        
        long startTime = System.currentTimeMillis();
        String serviceResponse = client.sayHi("XMPP Service Call-1");
        System.out.println("Elapsed time: "+(System.currentTimeMillis() - startTime));
        System.out.println("Service said: "+serviceResponse);
        
        startTime = System.currentTimeMillis();
        serviceResponse = client.sayHi("XMPP Service Call-2");
        System.out.println("Elapsed time: "+(System.currentTimeMillis() - startTime));
        System.out.println("Service said: "+serviceResponse);       
        
    }
}
