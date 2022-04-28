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

import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import org.junit.Assert;
import org.junit.Test;

public class AsyncResponseTest extends BaseIntegrationTest {

    @Test
    public void testAsync() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/async/response/async").addParam("name", "test")
                .addParam("timeout", "100").execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());

        responseBase = restClient.get(domain + "/async/response/async").addParam("name", "test")
                .addParam("timeout", "300").execute().toCompletableFuture().get();
        userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("timeout", userData.getName());
    }

    @Test
    public void testFuture() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/async/response/future").addParam("name", "test")
                .addParam("timeout", "100").execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }
}
