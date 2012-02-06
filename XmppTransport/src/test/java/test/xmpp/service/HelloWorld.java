package test.xmpp.service;

import javax.jws.WebService;

@WebService
public interface HelloWorld
{
    public String sayHi(String text);
}
