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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.context.RequestContext;
import jakarta.ws.rs.container.ResourceContext;

public class ResourceContextImpl implements ResourceContext {

    private final HandlerFactory underlying;
    private final RequestContext context;

    public ResourceContextImpl(HandlerFactory underlying, RequestContext context) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(context, "context");
        this.underlying = underlying;
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getResource(Class<T> resourceClass) {
        return (T) underlying.getInstance(resourceClass, context);
    }

    @Override
    public <T> T initResource(T resource) {
        underlying.doInit(resource, context);
        return resource;
    }
}

