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
package io.esastack.restlight.core.handler;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.ObjectUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.method.FieldParam;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.RouteRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HandlerRegistryImpl implements HandlerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(HandlerRegistryImpl.class);

    private final List<HandlerMapping> mappings = new CopyOnWriteArrayList<>();
    private final DeployContext context;
    private final RouteRegistry registry;
    private final Handlers handlers;

    public HandlerRegistryImpl(DeployContext context) {
        Checks.checkNotNull(context, "context");
        Checks.checkState(context.routeRegistry().isPresent(), "route registry is null.");
        Checks.checkState(context.handlers().isPresent(), "handlers is null");
        this.context = context;
        this.registry = context.routeRegistry().get();
        this.handlers = context.handlers().get();
    }

    @Override
    public List<HandlerMapping> mappings() {
        return Collections.unmodifiableList(mappings);
    }

    @Override
    public void addHandler(Object handler) {
        Checks.checkNotNull(handler, "handler");
        addHandlers(Collections.singleton(handler));
    }

    @Override
    public void addHandlers(Collection<Object> handlers) {
        if (handlers != null && !handlers.isEmpty()) {
            List<HandlerMapping> mappings = new LinkedList<>();
            handlers.forEach(handler -> {
                Class<?> userType = ClassUtils.getUserType(handler);

                // the resolvable param of singleton handler is not allowed in fields and setters.
                checkFieldsAndSetters(userType);

                ClassUtils.doWithUserDeclaredMethods(userType,
                        method -> {
                            HandlerContext context = HandlerContext.build(this.context,
                                    HandlerMethodImpl.of(userType, method));
                            Optional<HandlerMapping> mapping = RouteUtils
                                    .extractHandlerMapping(context,
                                            ObjectUtils.instantiateBeanIfNecessary(handler),
                                            userType, method);
                            if (mapping.isPresent()) {
                                Optional<Route> route = RouteUtils.extractRoute(context, mapping.get());
                                if (route.isPresent()) {
                                    mappings.add(mapping.get());
                                    registry.register(route.get());
                                }
                            }
                        },
                        RouteUtils::isHandlerMethod);
            });
            this.mappings.addAll(mappings);
        }
    }

    @Override
    public void addHandler(Class<?> clazz, boolean singleton) {
        Checks.checkNotNull(clazz, "clazz");
        addHandlers(Collections.singleton(clazz), singleton);
    }

    @Override
    public void addHandlers(Collection<Class<?>> classes, boolean singleton) {
        if (classes != null && !classes.isEmpty()) {
            if (singleton) {
                addHandlers(classes.stream().map(ObjectUtils::instantiateBeanIfNecessary)
                        .collect(Collectors.toList()));
            } else {
                List<HandlerMapping> mappings = new LinkedList<>();
                classes.forEach(clazz -> {
                    Class<?> userType = ClassUtils.getUserType(clazz);
                    ClassUtils.doWithUserDeclaredMethods(userType,
                            method -> {
                                HandlerContext context = HandlerContext.build(this.context,
                                        HandlerMethodImpl.of(userType, method));
                                Optional<HandlerMapping> mapping = RouteUtils
                                        .extractHandlerMapping(context, null, userType, method);
                                if (mapping.isPresent()) {
                                    Optional<Route> route = RouteUtils.extractRoute(context, mapping.get());
                                    if (route.isPresent()) {
                                        mappings.add(mapping.get());
                                        registry.register(route.get());
                                    }
                                }
                            },
                            RouteUtils::isHandlerMethod);
                });
                this.mappings.addAll(mappings);
            }
        }
    }

    @Override
    public void removeHandler(Object handler) {
        Checks.checkNotNull(handler, "handler");
        removeHandlers(Collections.singleton(handler));
    }

    @Override
    public void removeHandlers(Collection<Object> handlers) {
        if (handlers != null && !handlers.isEmpty()) {
            List<Object> removableHandlers = new LinkedList<>();
            for (Object handler : handlers) {
                for (Class<?> clazz : this.handlers.getClasses()) {
                    if (clazz.equals(handler)) {
                        removableHandlers.add(clazz);
                    }
                }

                Class<?> userType = ClassUtils.getUserType(handler);
                for (Object singleton : this.handlers.getSingletons()) {
                    if (singleton.equals(handler) || ClassUtils.getUserType(singleton).equals(userType)) {
                        removableHandlers.add(singleton);
                    }
                }
            }

            List<HandlerMapping> removableMappings = new LinkedList<>();
            removableHandlers.forEach(removable -> {
                Class<?> userType = ClassUtils.getUserType(removable);
                ClassUtils.doWithUserDeclaredMethods(userType,
                        method -> {
                            Optional<HandlerMapping> mapping = RouteUtils
                                    .extractHandlerMapping(new HandlerContext(context),
                                            null, userType, method);
                            if (mapping.isPresent()) {
                                // deRegister routes which have same mapping extracted from given handler
                                registry.deRegister(Route.route(RouteUtils.computeFullyMapping(mapping.get())));
                                for (HandlerMapping mp : mappings) {
                                    if (mp.mapping().equals(mapping.get().mapping())) {
                                        removableMappings.add(mp);
                                    }
                                }
                            }
                        },
                        RouteUtils::isHandlerMethod);
            });
            this.mappings.removeAll(removableMappings);
        }
    }

    @Override
    public void addHandlerMapping(HandlerMapping mapping) {
        Checks.checkNotNull(mapping, "mapping");
        addHandlerMappings(Collections.singletonList(mapping));
    }

    @Override
    public void addHandlerMappings(Collection<HandlerMapping> mappings) {
        if (mappings != null && !mappings.isEmpty()) {
            List<HandlerMapping> mps = new LinkedList<>();
            mappings.forEach(mapping -> RouteUtils.extractRoute(new HandlerContext(context), mapping)
                    .ifPresent(route -> {
                        mps.add(mapping);
                        registry.register(route);
                    }));
            mappings.addAll(mps);
        }
    }

    private void checkFieldsAndSetters(Class<?> userType) {
        ResolvableParamPredicate resolvable = context.paramPredicate().orElse(null);
        if (resolvable == null) {
            return;
        }
        ReflectionUtils.getAllDeclaredFields(userType)
                .forEach(f -> {
                    FieldParam param = new FieldParamImpl(f);
                    if (resolvable.test(param)) {
                        logger.warn("Can't resolve field({}) in a singleton handler. maybe you can"
                                + " add class({}) as prototype?", param, userType);
                    }
                });

        ReflectionUtils.getAllDeclaredMethods(userType).stream()
                .filter(ReflectionUtils::isSetter)
                .forEach(m -> {
                    MethodParam param = new MethodParamImpl(m, 0);
                    if (resolvable.test(param)) {
                        logger.warn("Can't resolve param({}) in a singleton handler. maybe you can"
                                + " add class({}) as prototype?", param, userType);
                    }
                });
    }
}

