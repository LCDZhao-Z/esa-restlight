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
package io.esastack.restlight.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.annotation.Scheduled;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.method.RouteMethodInfo;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.impl.HandlerMappingImpl;
import io.esastack.restlight.core.handler.method.PrototypeRouteMethod;
import io.esastack.restlight.core.handler.impl.RouteHandlerImpl;
import io.esastack.restlight.core.handler.method.RouteHandlerMethodAdapter;
import io.esastack.restlight.core.handler.method.SingletonRouteMethod;
import io.esastack.restlight.core.locator.CompositeHandlerValueResolverLocator;
import io.esastack.restlight.core.locator.CompositeMappingLocator;
import io.esastack.restlight.core.locator.CompositeRouteMethodLocator;
import io.esastack.restlight.core.locator.HandlerValueResolverLocator;
import io.esastack.restlight.core.locator.MappingLocator;
import io.esastack.restlight.core.locator.RouteMethodLocator;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.core.spi.HandlerValueResolverLocatorFactory;
import io.esastack.restlight.core.spi.MappingLocatorFactory;
import io.esastack.restlight.core.spi.RouteMethodLocatorFactory;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.Routing;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import io.netty.util.concurrent.Future;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.esastack.restlight.core.util.InterceptorUtils.filter;

public final class RouteUtils {

    public static boolean isHandlerMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) && !method.isBridge();
    }

    public static boolean isConcurrent(HandlerMethod handler) {
        Class<?> type = handler.method().getReturnType();
        return (CompletableFuture.class.isAssignableFrom(type))
                || (FutureUtils.hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(type))
                || (Future.class.isAssignableFrom(type));
    }

    public static String scheduling(HandlerMethod handler, String defaultScheduling) {
        Scheduled scheduled = handler.getMethodAnnotation(Scheduled.class, false);
        if (scheduled == null) {
            scheduled = handler.getClassAnnotation(Scheduled.class, false);
        }
        if (scheduled == null) {
            if (defaultScheduling == null) {
                return Schedulers.BIZ;
            } else {
                return defaultScheduling;
            }
        }
        return scheduled.value();
    }

    public static Optional<HandlerMethod> extractHandlerMethod(Routing routing) {
        if (!routing.handler().isPresent()) {
            return Optional.empty();
        }

        Object handler = routing.handler().get();
        if (handler instanceof Handler) {
            return Optional.of(((Handler) handler).handlerMethod());
        } else if (handler instanceof HandlerMethod) {
            return Optional.of((HandlerMethod) handler);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Object> extractHandlerBean(Routing routing) {
        if (!routing.handler().isPresent()) {
            return Optional.empty();
        }

        Object handler = routing.handler().get();
        if (handler instanceof Handler) {
            return Optional.of(((Handler) handler).bean());
        }
        return Optional.empty();
    }

    public static Optional<HandlerMapping> extractHandlerMapping(HandlerContext context,
                                                                 Object bean, Class<?> userType, Method method) {
        return extractHandlerMapping(context, null, bean, userType, method);
    }

    public static Optional<HandlerMapping> extractHandlerMapping(HandlerContext context,
                                                                 HandlerMapping parent,
                                                                 Object bean, Class<?> userType, Method method) {
        final Optional<MappingLocator> mappingLocator;
        final Optional<RouteMethodLocator> methodLocator;
        if (!(mappingLocator = context.mappingLocator()).isPresent() ||
                !(methodLocator = context.methodLocator()).isPresent()) {
            return Optional.empty();
        }

        final Optional<Mapping> mapping = mappingLocator.get().getMapping(parent, userType, method);
        if (mapping.isPresent()) {
            Optional<RouteMethodInfo> handlerMethod = methodLocator.get().getRouteMethodInfo(parent, userType, method);
            if (!handlerMethod.isPresent()) {
                LoggerUtils.logger().debug("Found Mapping but could not generate" +
                                " RouteMethodInfo for it. userType: {}, method: {}",
                        userType.getName(), method.toString());
                return Optional.empty();
            }
            return Optional.of(new HandlerMappingImpl(mapping.get(), handlerMethod.get(), bean, parent));
        }
        return Optional.empty();
    }

    public static Optional<Route> extractRoute(HandlerContext context,
                                               HandlerMapping mapping) {
        if (!context.handlerResolverLocator().isPresent() || !context.resolverFactory().isPresent()
                || !context.exceptionResolverFactory().isPresent() || !context.handlerFactory().isPresent()) {
            return Optional.empty();
        }
        final Optional<HandlerValueResolver> handlerResolver = context.handlerResolverLocator().get()
                .getHandlerValueResolver(mapping);
        if (!handlerResolver.isPresent()) {
            HandlerMethod handlerMethod = mapping.methodInfo().handlerMethod();
            LoggerUtils.logger().debug("Found Mapping but could not generate" +
                            " HandlerValueResolver for it. userType: {}, method: {}",
                    handlerMethod.beanType().getName(),
                    handlerMethod.method().toString());
            return Optional.empty();
        }

        final ExceptionResolverFactory exceptionResolver = context.exceptionResolverFactory().get();
        final RouteMethodInfo methodInfo = mapping.methodInfo();
        final boolean singleton = mapping.bean().isPresent();

        final Object handler;
        final RouteHandlerMethodAdapter routeMethod;
        if (singleton) {
            handler = new RouteHandlerImpl(methodInfo.handlerMethod(), mapping.bean().get());
            routeMethod = new SingletonRouteMethod(mapping, context, handlerResolver.get(),
                    filter(context, mapping.mapping(), handler,
                            context.interceptors().orElse(Collections.emptyList())),
                    exceptionResolver.createResolver(methodInfo.handlerMethod()));
        } else {
            handler = methodInfo.handlerMethod();
            routeMethod = new PrototypeRouteMethod(mapping, context, handlerResolver.get(),
                    filter(context, mapping.mapping(), methodInfo.handlerMethod(),
                            context.interceptors().orElse(Collections.emptyList())),
                    exceptionResolver.createResolver(methodInfo.handlerMethod()));
        }

        final Scheduler scheduler = context.schedulers().get(methodInfo.handlerMethod().scheduler());
        Checks.checkNotNull(scheduler,
                "Could not find any scheduler named '" + methodInfo.handlerMethod().scheduler() + "'");

        return Optional.of(Route.route(scheduler)
                .mapping(computeFullyMapping(mapping))
                .handler(handler)
                .executionFactory(routeMethod::toExecution)
        );
    }

    public static ResolvableParamPredicate loadResolvableParamPredicate(DeployContext
                                                                                ctx) {
        Collection<ResolvableParamPredicate> paramPredicates = SpiLoader
                .cached(ResolvableParamPredicate.class)
                .getByFeature(ctx.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (paramPredicates.isEmpty()) {
            return null;
        }
        return param -> {
            for (ResolvableParamPredicate predicate : paramPredicates) {
                if (predicate.test(param)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static RouteMethodLocator loadRouteMethodLocator(DeployContext ctx) {
        List<RouteMethodLocatorFactory> factories =
                SpiLoader.cached(RouteMethodLocatorFactory.class)
                        .getByFeature(ctx.name(),
                                true,
                                Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                false);
        List<RouteMethodLocator> routeHandlerLocators = factories.stream()
                .map(factory -> factory.locator(ctx))
                .collect(Collectors.toList());
        return CompositeRouteMethodLocator.wrapIfNecessary(routeHandlerLocators);
    }

    public static MappingLocator loadMappingLocator(DeployContext ctx) {
        List<MappingLocatorFactory> factories =
                SpiLoader.cached(MappingLocatorFactory.class)
                        .getByFeature(ctx.name(),
                                true,
                                Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                false);
        List<MappingLocator> mappingLocators = factories.stream()
                .map(factory -> factory.locator(ctx))
                .collect(Collectors.toList());
        return CompositeMappingLocator.wrapIfNecessary(mappingLocators);
    }

    public static HandlerValueResolverLocator loadHandlerValueResolverLocator(DeployContext
                                                                                      ctx) {
        List<HandlerValueResolverLocatorFactory> factories =
                SpiLoader.cached(HandlerValueResolverLocatorFactory.class)
                        .getByFeature(ctx.name(),
                                true,
                                Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                false);
        List<HandlerValueResolverLocator> handlerValueResolverLocators = factories.stream()
                .map(factory -> factory.resolver(ctx))
                .collect(Collectors.toList());
        OrderedComparator.sort(handlerValueResolverLocators);
        return CompositeHandlerValueResolverLocator.wrapIfNecessary(handlerValueResolverLocators);
    }

    public static Mapping computeFullyMapping(HandlerMapping mapping) {
        Mapping value = mapping.mapping();
        if (mapping.methodInfo().isLocator()) {
            value = MappingUtils.combine(mapping.mapping(), Mapping.mapping("/**"));
        }

        Optional<HandlerMapping> parent = mapping.parent();
        while (parent.isPresent()) {
            value = MappingUtils.combine(parent.get().mapping(), value);
            parent = parent.get().parent();
        }

        return value;
    }

    private RouteUtils() {
    }
}
