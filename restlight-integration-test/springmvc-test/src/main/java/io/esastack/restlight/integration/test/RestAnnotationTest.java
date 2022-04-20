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

package io.esastack.restlight.integration.test;

import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.entity.UserData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author chenglu
 */
public class RestAnnotationTest extends BaseIntegrationTest {

    @Test
    public void testGet() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get").addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testPost() throws Exception {
        UserData user = UserData.Builder.aRestResult()
                .name("test").age(10).birthDay(new Date())
                .weight(BigDecimal.valueOf(123.01)).build();
        RestResponseBase response = restClient.post(domain + "/rest/annotation/post").entity(user).execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        Assert.assertEquals(user.getName(), userResult.getName());
    }

    @Test
    public void testDelete() throws Exception {
        RestResponseBase response = restClient.delete(domain + "/rest/annotation/delete/test").execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userResult.getName());
    }

    @Test
    public void testPut() throws Exception {
        RestResponseBase response = restClient.put(domain + "/rest/annotation/put").addCookie("name", "test").execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userResult.getName());
    }

    @Test
    public void testQueryBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/querybean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testRequestBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/requestbean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testHeader() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/header")
                .addHeader("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testMatrix() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/matrix")
                .addParam("name", "test,test2").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testAttribute() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/attribute").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testResponseStatus() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/reponsestatus").execute()
                .toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
    }

    @Test
    public void testCustomBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/custombean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testCustomBody() throws Exception {
        UserData user = UserData.Builder.aRestResult()
                .name("test").age(10).birthDay(new Date())
                .weight(BigDecimal.valueOf(123.01)).build();
        RestResponseBase response = restClient.post(domain + "/rest/annotation/post/custombody").entity(user).execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        Assert.assertEquals(user.getName(), userResult.getName());
    }

    @Test
    public void testCustomResponseBody() throws Exception {
        RestResponseBase response = restClient.get(domain + "/rest/annotation/get/customresponsebody")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test", user.getName());
    }
}
