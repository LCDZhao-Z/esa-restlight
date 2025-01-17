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
package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.param.QueryBeanParamResolver;
import io.esastack.restlight.core.spi.ParamResolverProvider;

import java.util.Optional;

public class QueryBeanParamResolverProvider implements ParamResolverProvider {

    @Override
    public Optional<ParamResolverFactory> factoryBean(DeployContext ctx) {
        return Optional.of(new QueryBeanParamResolver(ctx));
    }

}
