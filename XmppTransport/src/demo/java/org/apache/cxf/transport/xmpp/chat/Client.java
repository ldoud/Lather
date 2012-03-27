/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.xmpp.chat;

import org.apache.cxf.transport.xmpp.smackx.soap.SoapPacket;
import org.apache.cxf.transport.xmpp.smackx.soap.SoapProvider;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;

public class Client implements PacketListener {
    private static String message = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                                    + "<soap:Body>" + "<test:sayHi xmlns:test='http://service.xmpp.test/'>"
                                    + "        <arg0>World</arg0>" + "</test:sayHi>" + "</soap:Body>"
                                    + "</soap:Envelope>";

    private String prefix;

    public Client(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void processPacket(Packet msg) {
        System.out.println(prefix + " " + msg.toXML());
    }

    public static void main(String[] args) throws Exception {
        XMPPConnection xmppConnection = new XMPPConnection("localhost.localdomain");

        xmppConnection.connect();
        xmppConnection.login("user1", "user1");
        System.out.println("Logged in as:" + xmppConnection.getUser());

        // TODO Remove this hack and properly configure this.
        ProviderManager.getInstance().addIQProvider("Envelope", "http://www.w3.org/2003/05/soap-envelope",
                                                    new SoapProvider());

        xmppConnection.addPacketListener(new Client("Received"), new PacketFilter() {
            @Override
            public boolean accept(Packet arg0) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        xmppConnection.addPacketSendingListener(new Client("Sent"), new PacketFilter() {
            @Override
            public boolean accept(Packet arg0) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        SoapPacket iqPacket = new SoapPacket();
        iqPacket.setTo("service1@localhost.localdomain/{http://service.xmpp.test/}HelloWorld");
        iqPacket.setEnvelope(message);
        xmppConnection.sendPacket(iqPacket);

        System.out.println("Sent IQ Packet");

        Thread.sleep(10000);
        xmppConnection.disconnect();
    }

}
