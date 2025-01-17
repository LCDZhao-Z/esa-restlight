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

import io.esastack.restlight.core.handler.method.Param;

/**
 * Interface for predicating whether the implementations can used to handle the {@link Param}.
 */
public interface ParamPredicate {

    /**
     * Whether current {@link ParamResolver} implementation is support given parameter.
     *
     * @param param param
     * @return {@code true} if it supports
     */
    boolean supports(Param param);

}

