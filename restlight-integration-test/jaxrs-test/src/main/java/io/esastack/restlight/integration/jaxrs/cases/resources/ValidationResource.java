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

package io.esastack.restlight.integration.jaxrs.cases.resources;

import io.esastack.restlight.integration.jaxrs.entity.UserData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Controller
@Path("/validation/")
public class ValidationResource {

    @GET
    @Path("request/param")
    public UserData requestParam(@NotEmpty @QueryParam("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @POST
    @Path("request/entity")
    public UserData requestEntity(@Valid UserData userData) {
        return userData;
    }

    @GET
    @Path("response/param")
    @NotNull
    public UserData responseParam(@QueryParam("name") String name) {
        return null;
    }

    @POST
    @Path("response/entity")
    @Valid
    public UserData responseEntity(UserData userData) {
        return userData;
    }
}
