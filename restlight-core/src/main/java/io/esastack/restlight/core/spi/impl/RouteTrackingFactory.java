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

import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Optional;

public class RouteTrackingFactory implements RouteFilterFactory {

    private static final RouteFilter SINGLETON = new RouteTracking();

    @Override
    public Optional<RouteFilter> create(HandlerMethod method) {
        return Optional.of(SINGLETON);
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }
}

