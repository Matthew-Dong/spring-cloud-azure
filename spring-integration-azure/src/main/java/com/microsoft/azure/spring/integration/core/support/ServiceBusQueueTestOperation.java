/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ServiceBusQueueTestOperation extends ServiceBusQueueTemplate {
    private final Multimap<String, IMessage> topicsByName = ArrayListMultimap.create();
    private final Multimap<String, IMessageHandler> handlersByQueue =
            ArrayListMultimap.create();

    public ServiceBusQueueTestOperation(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        IMessage azureMessage = getMessageConverter().fromMessage(message, IMessage.class);

        topicsByName.put(name, azureMessage);
        getRandom(handlersByQueue.get(name)).map(c -> c.onMessageAsync(azureMessage));

        future.complete(null);
        return future;
    }

    @Override
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer,
            Class<?> payloadType) {
        IQueueClient queueClient =
                this.senderFactory.getQueueClientCreator().apply(name);

        ServiceBusMessageHandler handler = new QueueMessageHandler(consumer, payloadType, queueClient);

        try {
            queueClient.registerMessageHandler(handler);
        } catch (ServiceBusException | InterruptedException e) {
            throw new ServiceBusRuntimeException("Failed to internalSubscribe message handler", e);
        }

        handlersByQueue.put(name, handler);
    }

    @Override
    public boolean unsubscribe(String name) {
        handlersByQueue.removeAll(name);
        return true;
    }

    public static <E> Optional<E> getRandom (Collection<E> e) {

        return e.stream()
                .skip((int) (e.size() * Math.random()))
                .findFirst();
    }
}

