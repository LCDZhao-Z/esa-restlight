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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

public class UriInfoParamResolver implements ParamResolverFactory {

    @Override
    public boolean supports(Param param) {
        return JaxrsUtils.hasAnnotation(param, Context.class) && UriInfo.class.equals(param.type());
    }

    @Override
    public ParamResolver<ParamResolverContext> createResolver(Param param,
                                                              StringConverterProvider converters,
                                                              List<? extends HttpRequestSerializer> serializers,
                                                              HandlerResolverFactory resolverFactory) {
        return new UriInfoResolver();
    }

    private static class UriInfoResolver implements ParamResolver<ParamResolverContext> {

        private UriInfoResolver() {
        }

        @Override
        public Object resolve(ParamResolverContext context) throws Exception {
            return JaxrsContextUtils.getUriInfo(context.requestContext());
        }
    }
}

