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
package io.esastack.restlight.core.deploy;

import esa.commons.Checks;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.locator.HandlerValueResolverLocator;
import io.esastack.restlight.core.locator.MappingLocator;
import io.esastack.restlight.core.locator.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionMapper;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.core.route.RouteRegistry;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DelegatingDeployContext implements DeployContext {

    private final DeployContext underlying;

    public DelegatingDeployContext(DeployContext underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public Attributes attrs() {
        return underlying.attrs();
    }

    @Override
    public Optional<List<Object>> singletonControllers() {
        return underlying.singletonControllers();
    }

    @Override
    public Optional<List<Class<?>>> prototypeControllers() {
        return underlying.prototypeControllers();
    }

    @Override
    public Optional<List<Object>> extensions() {
        return underlying.extensions();
    }

    @Override
    public Optional<List<Object>> advices() {
        return underlying.advices();
    }

    @Override
    public Optional<List<InterceptorFactory>> interceptors() {
        return underlying.interceptors();
    }

    @Override
    public Optional<List<ExceptionMapper>> exceptionMappers() {
        return underlying.exceptionMappers();
    }

    @Override
    public Optional<HandlerResolverFactory> resolverFactory() {
        return underlying.resolverFactory();
    }

    @Override
    public Optional<HandlerAdvicesFactory> handlerAdvicesFactory() {
        return underlying.handlerAdvicesFactory();
    }

    @Override
    public Optional<RouteMethodLocator> methodLocator() {
        return underlying.methodLocator();
    }

    @Override
    public Optional<MappingLocator> mappingLocator() {
        return underlying.mappingLocator();
    }

    @Override
    public Optional<HandlerValueResolverLocator> handlerResolverLocator() {
        return underlying.handlerResolverLocator();
    }

    @Override
    public Optional<ExceptionResolverFactory> exceptionResolverFactory() {
        return underlying.exceptionResolverFactory();
    }

    @Override
    public String name() {
        return underlying.name();
    }

    @Override
    public RestlightOptions options() {
        return underlying.options();
    }

    @Override
    public Map<String, Scheduler> schedulers() {
        return underlying.schedulers();
    }

    @Override
    public Optional<RouteRegistry> routeRegistry() {
        return underlying.routeRegistry();
    }

    @Override
    public Optional<DispatcherHandler> dispatcherHandler() {
        return underlying.dispatcherHandler();
    }

    @Override
    public Optional<List<HandlerConfigure>> handlerConfigures() {
        return underlying.handlerConfigures();
    }

    @Override
    public Optional<ResolvableParamPredicate> paramPredicate() {
        return underlying.paramPredicate();
    }

    @Override
    public Optional<Handlers> handlers() {
        return underlying.handlers();
    }

    @Override
    public Optional<HandlerFactory> handlerFactory() {
        return underlying.handlerFactory();
    }

    @Override
    public Optional<HandlerContextProvider> handlerContexts() {
        return underlying.handlerContexts();
    }

    public DeployContext unwrap() {
        return underlying;
    }
}

