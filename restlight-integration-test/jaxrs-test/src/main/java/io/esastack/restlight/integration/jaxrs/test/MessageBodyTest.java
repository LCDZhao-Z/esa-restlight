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

package io.esastack.restlight.integration.jaxrs.test;

import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.jaxrs.entity.MessageBodyData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageBodyTest extends BaseIntegrationTest {

    @Test
    void testReader() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/message/body/reader")
                .contentType(MediaType.APPLICATION_JSON).execute().toCompletableFuture().get();
        MessageBodyData messageBodyData = responseBase.bodyToEntity(MessageBodyData.class);
        assertEquals("test", messageBodyData.getName());
    }

    @Test
    void testWriter() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/message/body/writer")
                .contentType(MediaType.APPLICATION_JSON).execute().toCompletableFuture().get();
        MessageBodyData messageBodyData = responseBase.bodyToEntity(MessageBodyData.class);
        assertEquals("test-byBodyWriter", messageBodyData.getName());
    }
}
