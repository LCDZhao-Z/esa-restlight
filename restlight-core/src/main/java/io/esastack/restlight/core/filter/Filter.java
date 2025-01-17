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
package io.esastack.restlight.core.filter;

import esa.commons.spi.SPI;
import io.esastack.restlight.core.util.Ordered;

import java.util.concurrent.CompletionStage;

@SPI
public interface Filter extends Ordered {

    /**
     * Note: we do not allowed any exception or error here. if a exception or error is threw in this function we will
     * ignore it to protect the process of current request.
     * <p>
     * IMPORTANT: never block current thread please, cause that will effect the performance.
     *
     * @param context context
     * @param chain   filter chain
     * @return future
     */
    CompletionStage<Void> doFilter(FilterContext context, FilterChain chain);

    /**
     * Default to lowest order.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}

