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

package io.esastack.restlight.core.resolver.param.entity;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.param.ParamPredicate;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface RequestEntityResolverFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link RequestEntityResolverAdapter} to {@link RequestEntityResolverFactory} which
     * always use the given {@link RequestEntityResolverAdapter} as the result of
     * {@link #createResolver(Param, StringConverterProvider, List)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static RequestEntityResolverFactory singleton(RequestEntityResolverAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link RequestEntityResolver} for given handler method.
     *
     * @param param         param
     * @param converters    the provider which is used to get a {@link StringConverter} when resolving
     *                      given {@code param}.
     * @param serializers   all the {@link HttpRequestSerializer}s in the context
     * @return resolver
     */
    RequestEntityResolver createResolver(Param param,
                                         StringConverterProvider converters,
                                         List<? extends HttpRequestSerializer> serializers);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements RequestEntityResolverFactory {

        private final RequestEntityResolverAdapter resolver;

        private Singleton(RequestEntityResolverAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public boolean supports(Param param) {
            return resolver.supports(param);
        }

        @Override
        public RequestEntityResolver createResolver(Param param,
                                                    StringConverterProvider converters,
                                                    List<? extends HttpRequestSerializer> serializers) {
            return resolver;
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }
}


