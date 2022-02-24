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
package io.esastack.restlight.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializeOptionsTest {

    @Test
    void testConfigure() {
        final SerializeOptions options = SerializeOptionsConfigure.newOpts()
                .negotiation(true)
                .negotiationParam("foo")
                .configured();


        assertTrue(options.isNegotiation());
        assertEquals("foo", options.getNegotiationParam());
    }

    @Test
    void testDefaultOpts() {
        final SerializeOptions options = SerializeOptionsConfigure.defaultOpts();
        final SerializeOptions def = new SerializeOptions();

        assertEquals(def.isNegotiation(), options.isNegotiation());
        assertEquals(def.getNegotiationParam(), options.getNegotiationParam());
    }

}
