package test.xmpp.server;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(
        endpointInterface = "test.xmpp.server.HelloWorld", 
        serviceName = "HelloWorld")
public class HelloWorldService implements HelloWorld
{
    public String sayHi(@WebParam(name="text")String text)
    {
        System.out.println("Hello "+text);
        return "Hello " + text;
    }
}

