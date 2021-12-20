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
package io.esastack.restlight.core.resolver;

import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.method.Param;

/**
 * This {@link ParamResolverContext} is designed to hold context which is passed among
 * {@link ParamResolverAdvice}s.
 */
public interface ParamResolverContext {

    /**
     * Obtains current {@link RequestContext}.
     *
     * @return  request context
     */
    RequestContext context();

    /**
     * Obtains the {@link Param} to resolve.
     *
     * @return  param
     */
    Param param();

    /**
     * Resolves the {@link #param()} by given {@link #context()}.
     *
     * @return  context
     * @throws Exception exception
     */
    Object proceed() throws Exception;

}
