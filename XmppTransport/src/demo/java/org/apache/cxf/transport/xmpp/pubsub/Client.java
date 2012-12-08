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

package org.apache.cxf.transport.xmpp.pubsub;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import test.xmpp.service.HelloWorld;

public final class Client {
    private Client() {
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
              "client-pubsub-applicationContext.xml");
        HelloWorld client = (HelloWorld)context.getBean("helloClient");

        long startTime = System.currentTimeMillis();
        client.yell("One way message!!!");
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        client.yell("Second message!!");
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime));
    }
}
