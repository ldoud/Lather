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

package org.apache.cxf.transport.xmpp.common;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Classes that require XMPP connection feature.
 */
public interface XMPPConnectionUser {
    
    /**
     * This maybe called multiple times during initialization.
     * Therefore the factory should not be used until the server is started.
     * @param factory Factory that is used for creating connections.
     */
    public void setXmppConnection(XMPPConnection conn, boolean shared);
    
    
    public XMPPConnection getXmppConnection();
}