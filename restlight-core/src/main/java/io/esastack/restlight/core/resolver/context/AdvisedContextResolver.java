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
package io.esastack.restlight.core.resolver.context;

import esa.commons.Checks;
import io.esastack.restlight.core.resolver.Resolver;
import io.esastack.restlight.core.resolver.ResolverContext;

public class AdvisedContextResolver implements Resolver<ResolverContext> {

    private final ContextResolver underlying;

    private static final ContextResolverContext DEFAULT_CONTEXT = new ContextResolverContextImpl();

    public AdvisedContextResolver(ContextResolver underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public Object resolve(ResolverContext context) throws Exception {
        return underlying.resolve(DEFAULT_CONTEXT);
    }
}

