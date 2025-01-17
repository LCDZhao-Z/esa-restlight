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
package io.esastack.restlight.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RouteOptionsTest {

    @Test
    void testConfigure() {
        final RouteOptions options = RouteOptionsConfigure.newOpts()
                .computeRate(100)
                .useCachedRouting(false)
                .configured();

        assertEquals(100, options.getComputeRate());
        assertFalse(options.isUseCachedRouting());
    }

    @Test
    void testDefaultOpts() {
        final RouteOptions options = RouteOptionsConfigure.defaultOpts();
        final RouteOptions def = new RouteOptions();
        assertEquals(def.getComputeRate(), options.getComputeRate());
        assertEquals(def.isUseCachedRouting(), options.isUseCachedRouting());
    }

}
