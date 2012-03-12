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
package org.apache.cxf.transport.xmpp.iq;

import junit.framework.Assert;

import org.apache.cxf.message.Message;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.xmpp.smackx.soap.SoapPacket;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Before;
import org.junit.Test;

public class IQBackChannelConduitTest {
    
    // Test stubs for a SOAP packet.
//    private static final String REPLY_MSG = "<something>that does not matter</something>";
    private SoapPacket testInputPacket;
    
    // Test stubs for an XMPP connections.
//    private Packet testReplyPacket;
    private boolean testDisconnectedWasCalled;
    private XMPPConnection fakeXmppConnection;

    @Before
    public void initializeTestStubs() {
        testInputPacket = new SoapPacket();
        
        testDisconnectedWasCalled = false;
        fakeXmppConnection = new XMPPConnection("doesNotMatter") {
            
            @Override
            public void disconnect() {
                testDisconnectedWasCalled = true;
            }
            
            @Override
            public void sendPacket(Packet packet) {
//                testReplyPacket = packet;
            };
        };
    }
    
    @Test
    public void testMsgObserverIsSet() {
        MessageObserver obs = new MessageObserver() {
            @Override
            public void onMessage(Message arg0) {
                // Doesn't matter.
            }
        };
        
        IQBackChannelConduit conduit = new IQBackChannelConduit(testInputPacket, fakeXmppConnection);
        conduit.setMessageObserver(obs);
        Assert.assertEquals("The correct message observer is being used", obs,
                            conduit.getMessageObserver());
    }

    @Test
    public void testXmppConnectionNotClosed() {
        IQBackChannelConduit conduit = new IQBackChannelConduit(testInputPacket, fakeXmppConnection);
        conduit.close();
        
        Assert.assertEquals("XMPP was not closed", false, testDisconnectedWasCalled);
    }
    
    public void testSendingReply() {
//        CachedOutputStream outputStream = new CachedOutputStream();

        
    }
}
