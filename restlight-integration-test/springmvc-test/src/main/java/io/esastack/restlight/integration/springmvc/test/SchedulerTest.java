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

import static org.junit.jupiter.api.Assertions.assertTrue;


class SchedulerTest extends BaseIntegrationTest {

    @Test
    void testBiz() throws Exception {
        RestResponseBase response = restClient.get(domain + "/scheduler/biz").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("biz"));
    }

    @Test
    void testIo() throws Exception {
        RestResponseBase response = restClient.get(domain + "/scheduler/io").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("i/o"));
    }

    @Test
    void testCustom() throws Exception {
        RestResponseBase response = restClient.get(domain + "/scheduler/custom").execute()
                .toCompletableFuture().get();
        assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("custom"));
    }
}
