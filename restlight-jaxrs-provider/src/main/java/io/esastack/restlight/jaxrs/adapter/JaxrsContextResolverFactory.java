/*
 * Copyright 2022 OPPO ESA Stack Project
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
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Providers;

import java.util.List;

public class JaxrsContextResolverFactory implements ParamResolverFactory {

    private final Providers providers;

    public JaxrsContextResolverFactory(Providers providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @Override
    public ParamResolver<ParamResolverContext> createResolver(Param param,
                                                              StringConverterProvider converters,
                                                              List<? extends HttpRequestSerializer> serializers,
                                                              HandlerResolverFactory resolverFactory) {
        return new JaxrsContextResolver(param, providers);
    }

    @Override
    public boolean supports(Param param) {
        return JaxrsUtils.hasAnnotation(param, Context.class)
                && providers.getContextResolver(param.type(), jakarta.ws.rs.core.MediaType.WILDCARD_TYPE) != null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static final class JaxrsContextResolver implements ParamResolver<ParamResolverContext> {

        private final Param param;
        private final Providers providers;

        private JaxrsContextResolver(Param param, Providers providers) {
            this.param = param;
            this.providers = providers;
        }

        @Override
        public Object resolve(ParamResolverContext context) throws Exception {
            jakarta.ws.rs.ext.ContextResolver<?> resolver;
            for (MediaType mediaType : ResponseEntityUtils.getMediaTypes(context.requestContext())) {
                if ((resolver = providers.getContextResolver(param.type(),
                        MediaTypeUtils.convert(mediaType))) != null) {
                    return resolver.getContext(param.type());
                }
            }
            return null;
        }
    }
}

