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
package io.esastack.restlight.core.resolver.param;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface ParamResolverFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link ParamResolverAdapter} to {@link ParamResolverFactory} which
     * always use the given {@link ParamResolverAdapter} as the result of
     * {@link #createResolver(Param, StringConverterProvider, List, HandlerResolverFactory)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ParamResolverFactory singleton(ParamResolverAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates a instance of {@link ParamResolver} for given handler method.
     *
     * @param param         method
     * @param converters    the provider which is used to get a {@link StringConverter} when resolving
     *                      given {@code param}.
     * @param serializers   all the {@link HttpRequestSerializer}s of current context
     * @param resolverFactory resolverFactory.
     * @return resolver
     */
    ParamResolver createResolver(Param param,
                                 StringConverterProvider converters,
                                 List<? extends HttpRequestSerializer> serializers,
                                 HandlerResolverFactory resolverFactory);

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ParamResolverFactory {

        private final ParamResolverAdapter resolver;

        Singleton(ParamResolverAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ParamResolver createResolver(Param param,
                                            StringConverterProvider converters,
                                            List<? extends HttpRequestSerializer> serializers,
                                            HandlerResolverFactory resolverFactory) {
            return resolver;
        }

        @Override
        public boolean supports(Param param) {
            return resolver.supports(param);
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }
}
