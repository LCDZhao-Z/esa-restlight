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
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverContext;
import io.esastack.restlight.jaxrs.impl.ext.ReaderInterceptorContextImpl;
import jakarta.ws.rs.ext.ReaderInterceptor;

public class ReaderInterceptorsAdapter implements RequestEntityResolverAdviceAdapter {

    private final ReaderInterceptor[] interceptors;
    private final ProvidersPredicate predicate;

    public ReaderInterceptorsAdapter(ReaderInterceptor[] interceptors, ProvidersPredicate predicate) {
        Checks.checkNotNull(interceptors, "interceptors");
        Checks.checkNotNull(predicate, "predicate");
        this.interceptors = interceptors;
        this.predicate = predicate;
    }

    @Override
    public Object aroundResolve(ResolverExecutor<RequestEntityResolverContext> executor) throws Exception {
        RequestEntityResolverContext context = executor.context();
        if (predicate.test(context.requestContext())) {
            return new ReaderInterceptorContextImpl(executor, interceptors).proceed();
        } else {
            return executor.proceed();
        }
    }
}

