/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Integration Channel Adapters for Azure Service Bus code sample.
 *
 * @author Warren Zhu
 */
@SpringBootApplication
public class ServiceBusOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusOperationApplication.class, args);
    }
}
