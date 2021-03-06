/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public abstract class InboundChannelAdapterTest<A extends AbstractInboundChannelAdapter> {

    protected String destination = "dest";
    protected String consumerGroup = "group";
    protected A adapter;
    private String[] payloads = {"payload1", "payload2"};
    private List<Message<?>> messages =
            Arrays.stream(payloads).map(p -> MessageBuilder.withPayload(p).build()).collect(Collectors.toList());

    @Before
    public abstract void setUp();

    @Test
    public void sendAndReceive() throws InterruptedException {
        DirectChannel channel = new DirectChannel();
        channel.setBeanName("output");
        this.adapter.doStart();
        this.adapter.setOutputChannel(channel);

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> receivedMessages = new CopyOnWriteArrayList<>();
        channel.subscribe(message -> {
            try {
                receivedMessages.add((String) message.getPayload());
            } finally {
                latch.countDown();
            }

        });

        this.messages.forEach(this.adapter::receiveMessage);
        Assert.isTrue(latch.await(5L, TimeUnit.SECONDS), "Failed to receive message");

        for (int i = 0; i < receivedMessages.size(); i++) {
            assertEquals(receivedMessages.get(i), payloads[i]);
        }
    }
}
