/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.springmvc.resolver.param;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractPathVariableParamResolver;
import io.esastack.restlight.springmvc.annotation.shaded.PathVariable0;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the PathVariable.
 */
public class PathVariableParamResolver extends AbstractPathVariableParamResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(PathVariable0.shadedClass());
    }

    @Override
    protected NameAndValue<String> createNameAndValue(Param param) {
        PathVariable0 pathVariable =
                PathVariable0.fromShade(param.getAnnotation(PathVariable0.shadedClass()));
        assert pathVariable != null;
        return new NameAndValue<>(pathVariable.value(), pathVariable.required());
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
