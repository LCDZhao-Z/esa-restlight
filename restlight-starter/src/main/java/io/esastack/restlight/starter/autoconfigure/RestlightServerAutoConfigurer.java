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
package io.esastack.restlight.starter.autoconfigure;

import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.starter.ServerStarter;
import io.esastack.restlight.starter.condition.ConditionalOnEnableServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass({RestlightServer.class})
@ConditionalOnEnableServer
@EnableConfigurationProperties(AutoRestlightServerOptions.class)
public class RestlightServerAutoConfigurer {

    @Bean(destroyMethod = "")
    @Primary
    @Qualifier(Constants.SERVER)
    public ServerStarter defaultServerStarter(AutoRestlightServerOptions options) {
        return new ServerStarter(options);
    }
}
