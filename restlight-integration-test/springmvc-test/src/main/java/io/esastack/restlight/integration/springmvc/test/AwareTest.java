/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.springmvc.test;

import io.esastack.restclient.RestResponseBase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwareTest extends BaseIntegrationTest {

    @Test
    void testBizAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/biz").execute()
                .toCompletableFuture().get();
        assertEquals(ThreadPoolExecutor.class.getName(), response.bodyToEntity(String.class));
    }

    @Test
    void testIoAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/io").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).contains("EventLoopGroup"));
    }

    @Test
    void testServerAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/server").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("server"));
    }

    @Test
    void testDeployContextAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/context").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("context"));
    }
}
