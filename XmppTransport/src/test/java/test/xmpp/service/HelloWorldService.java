package test.xmpp.service;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

@WebService(
        endpointInterface = "test.xmpp.service.HelloWorld", 
        serviceName = "HelloWorld")
@BindingType(value="http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class HelloWorldService implements HelloWorld
{
    public String sayHi(@WebParam(name="text")String text)
    {
        System.out.println("Hello "+text);
        return "Hello " + text;
    }
}

