/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import org.junit.Test;
import org.springframework.core.NestedRuntimeException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public abstract class SendOperationTest<O extends SendOperation> {

    protected O sendOperation;
    protected String payload = "payload";
    protected CompletableFuture<Void> future = new CompletableFuture<>();
    protected String partitionKey = "key";
    protected String destination = "event-hub";
    private String partitionId = "1";
    protected Message<?> message =
            new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));

    @Test
    public void testSendWithoutPartitionSupplier() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, null);

        assertNull(future.get());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartition() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, new PartitionSupplier());

        assertNull(future.get());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithPartitionId() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionId(partitionId);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(future.get());
        verifySendWithPartitionId(1);
        verifyPartitionSenderCalled(1);
    }

    @Test
    public void testSendWithPartitionKey() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(partitionKey);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(future.get());
        verifySendWithPartitionKey(1);
        verifyGetClientCreator(1);
    }

    @Test(expected = NestedRuntimeException.class)
    public void testSendCreateSenderFailure() throws Throwable {
        whenSendWithException();

        try {
            this.sendOperation.sendAsync(destination, this.message, null).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testSendFailure() {
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, this.message, null);
        this.future.completeExceptionally(new Exception("future failed."));

        try {
            future.get();
            fail("Test should fail.");
        } catch (InterruptedException ie) {
            fail("get() should fail with an ExecutionException.");
        } catch (ExecutionException ee) {
            assertEquals("future failed.", ee.getCause().getMessage());
        }
    }

    protected abstract void verifySendCalled(int times);

    protected abstract void verifyPartitionSenderCalled(int times);

    protected abstract void whenSendWithException();

    protected abstract void verifyGetClientCreator(int times);

    protected abstract void verifySendWithPartitionKey(int times);

    protected abstract void verifySendWithPartitionId(int times);
}
