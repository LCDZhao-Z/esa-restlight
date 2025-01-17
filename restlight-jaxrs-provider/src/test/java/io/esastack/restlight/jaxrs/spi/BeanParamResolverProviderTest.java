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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.jaxrs.resolver.param.BeanParamResolver;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BeanParamResolverProviderTest {

    @Test
    void testFactoryBean() {
        final BeanParamResolverProvider provider = new BeanParamResolverProvider();
        final Optional<ParamResolverFactory> ret = provider.factoryBean(mock(DeployContext.class));
        assertTrue(ret.isPresent());
        assertEquals(BeanParamResolver.class, ret.get().getClass());
    }

}

