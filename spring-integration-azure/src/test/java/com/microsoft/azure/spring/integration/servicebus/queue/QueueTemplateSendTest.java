/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueueTemplateSendTest extends ServiceBusTemplateSendTest<ServiceBusQueueClientFactory> {

    @Before
    @Override
    public void setUp() {
        this.mockClientFactory = mock(ServiceBusQueueClientFactory.class);
        this.mockClient = mock(IQueueClient.class);
        Mockito.<Function<String, ? extends IMessageSender>>when(this.mockClientFactory.getSenderCreator())
                .thenReturn(s -> this.mockClient);
        when(this.mockClient.sendAsync(isA(IMessage.class))).thenReturn(future);

        this.sendOperation = new ServiceBusQueueTemplate(mockClientFactory);
    }
}
