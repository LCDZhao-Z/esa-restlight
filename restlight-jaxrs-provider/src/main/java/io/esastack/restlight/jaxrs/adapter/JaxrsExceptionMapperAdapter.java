/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.util.Futures;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.concurrent.CompletionStage;

import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

public class JaxrsExceptionMapperAdapter<T extends Throwable> implements ExceptionResolver<T> {

    private final ProxyComponent<ExceptionMapper<T>> underlying;

    public JaxrsExceptionMapperAdapter(ProxyComponent<ExceptionMapper<T>> underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public CompletionStage<Void> handleException(RequestContext context, T t) {
        Response response;
        try {
            response = underlying.proxied().toResponse(t);
            if (response == null) {
                response = Response.status(NO_CONTENT).build();
            }
            context.response().entity(response);
            return Futures.completedFuture();
        } catch (Throwable th) {
            return Futures.completedExceptionally(th);
        }
    }

    public ProxyComponent<ExceptionMapper<T>> underlying() {
        return underlying;
    }
}

