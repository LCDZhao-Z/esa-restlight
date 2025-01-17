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
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.ret.ReturnValueResolverContext;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverContext;
import io.esastack.restlight.jaxrs.impl.ext.WriterInterceptorContextImpl;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;

public class WriterInterceptorsAdapter implements ResponseEntityResolverAdviceAdapter {

    private final WriterInterceptor[] interceptors;
    private final ProvidersPredicate predicate;

    public WriterInterceptorsAdapter(WriterInterceptor[] interceptors, ProvidersPredicate predicate) {
        Checks.checkNotNull(interceptors, "interceptors");
        Checks.checkNotNull(predicate, "predicate");
        this.interceptors = interceptors;
        this.predicate = predicate;
    }

    @Override
    public void aroundResolve0(ResolverExecutor<ResponseEntityResolverContext> executor) throws Exception {
        ReturnValueResolverContext context = executor.context();
        if (predicate.test(context.requestContext())) {
            MultivaluedMap<String, Object> headers =
                    JaxrsUtils.convertToMap(context.requestContext().response().headers());
            try {
                new WriterInterceptorContextImpl(executor, ResponseEntityStreamUtils
                        .getUnClosableOutputStream(context.requestContext()), headers, interceptors).proceed();
            } catch (Throwable th) {
                JaxrsUtils.convertThenAddToHeaders(headers, context.requestContext().response().headers());
                throw th;
            }
        } else {
            executor.proceed();
        }
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    public boolean alsoApplyWhenMissingHandler() {
        return true;
    }
}

