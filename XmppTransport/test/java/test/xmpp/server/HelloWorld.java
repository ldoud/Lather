package test.xmpp.server;

import javax.jws.WebService;

@WebService
public interface HelloWorld
{
    public String sayHi(String text);
}
