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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.reflect.BeanUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.handler.method.ConstructorParam;
import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParam;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.handler.method.ResolvableParam;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.Resolver;
import io.esastack.restlight.core.resolver.ResolverContext;
import io.esastack.restlight.core.resolver.context.AdvisedContextResolver;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.AdvisedParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.core.util.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerFactoryImpl implements HandlerFactory {

    private final ConcurrentHashMap<Class<?>, ResolvableHandler> resolvableHandlers = new ConcurrentHashMap<>();
    private final HandlerContextProvider handlerContexts;
    private final HandlerContext defaultContext;
    private final Handlers handlers;

    public HandlerFactoryImpl(DeployContext deployContext, Handlers handlers) {
        Checks.checkNotNull(deployContext, "deployContext");
        Checks.checkNotNull(handlers, "handlers");
        this.handlerContexts = deployContext.handlerContexts().orElseThrow(() ->
                new IllegalStateException("HandlerContextProvider is absent"));
        this.defaultContext = new HandlerContext(deployContext);
        this.handlers = handlers;
    }

    @Override
    public Object instantiate(Class<?> clazz, RequestContext context) {
        return doInstantiate(getOrDefaultContext(clazz, null), clazz, context);
    }

    @Override
    public Object instantiate(Class<?> clazz, Method method, RequestContext context) {
        return doInstantiate(getOrDefaultContext(clazz, method), clazz, context);
    }

    @Override
    public Object getInstance(Class<?> clazz, RequestContext context) {
        for (Object singleton : handlers.getSingletons()) {
            if (ClassUtils.getUserType(singleton).equals(clazz)) {
                return singleton;
            }
        }

        // no way but to instantiate one
        return instantiate(clazz, context);
    }

    @Override
    public Object getInstance(Class<?> clazz, Method method, RequestContext context) {
        for (Object singleton : handlers.getSingletons()) {
            if (ClassUtils.getUserType(singleton).equals(clazz)) {
                return singleton;
            }
        }

        // no way but to instantiate one
        return instantiate(clazz, method, context);
    }

    @Override
    public void doInit(Object instance, RequestContext context) {
        Class<?> userType = ClassUtils.getUserType(instance);
        doInit0(getOrDefaultContext(userType, null), instance, userType, context);
    }

    @Override
    public void doInit(Object instance, Method method, RequestContext context) {
        Class<?> userType = ClassUtils.getUserType(instance);
        doInit0(getOrDefaultContext(userType, method), instance, userType, context);
    }

    @SuppressWarnings("unchecked")
    protected Object doInstantiate(HandlerContext handlerContext,
                                   Class<?> clazz, RequestContext context) {
        final ResolvableHandler handler = getResolvableHandler(clazz, handlerContext);
        final ResolvableParam<ConstructorParam, Resolver>[] consParams = handler.consParamResolvers;
        final Object[] args = new Object[consParams.length];
        for (int i = 0; i < consParams.length; i++) {
            ResolvableParam<ConstructorParam, Resolver> resolvable = consParams[i];
            ConstructorParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                //it may return a null value
                try {
                    ResolverContext resolverContext = new ParamResolverContextImpl(context, param);
                    args[i] = resolvable.resolver().resolve(resolverContext);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }

        try {
            return handler.constructor.newInstance(args);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("Could not instantiate class: [" + clazz + "]", ex.getTargetException());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not instantiate class: [" + clazz + "]", ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected void doInit0(HandlerContext handlerContext, Object instance, Class<?> clazz, RequestContext context) {
        final ResolvableHandler handler = getResolvableHandler(clazz, handlerContext);

        // set fields
        for (ResolvableParam<FieldParam, Resolver> r : handler.fieldParamResolvers) {
            FieldParam param = r.param();
            //resolve args with resolver
            if (r.resolver() != null) {
                //it may return a null value
                try {
                    ResolverContext resolverContext = new ParamResolverContextImpl(context, param);
                    BeanUtils.setFieldValue(instance, param.name(), r.resolver().resolve(resolverContext));
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }

        // set methods
        for (ResolvableParam<MethodParam, Resolver> r : handler.setterParamResolvers) {
            MethodParam param = r.param();
            //resolve args with resolver
            if (r.resolver() != null) {
                //it may return a null value
                try {
                    ResolverContext resolverContext = new ParamResolverContextImpl(context, param);
                    Object arg = r.resolver().resolve(resolverContext);
                    ReflectionUtils.invokeMethod(param.method(), instance, arg);
                } catch (InvocationTargetException ex) {
                    throw new IllegalArgumentException("Error occurred while invoking method: [" +
                            param.method() + "]", ex.getTargetException());
                } catch (Exception ex) {
                    throw WebServerException.wrap(ex);
                }
            }
        }
    }

    private ResolvableHandler getResolvableHandler(Class<?> clazz, HandlerContext context) {
        return resolvableHandlers.computeIfAbsent(clazz, clz -> new ResolvableHandler(clazz, context));
    }

    private HandlerContext getOrDefaultContext(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            return defaultContext;
        }
        HandlerContext context = handlerContexts
                .getContext(HandlerMethodImpl.of(clazz, method));
        if (context != null) {
            return context;
        } else {
            return defaultContext;
        }
    }

    private static class ResolvableHandler {

        private final Constructor<?> constructor;
        private final ResolvableParam<ConstructorParam, Resolver>[] consParamResolvers;
        private final ResolvableParam<MethodParam, Resolver>[] setterParamResolvers;
        private final ResolvableParam<FieldParam, Resolver>[] fieldParamResolvers;
        private final HandlerContext context;

        private ResolvableHandler(Class<?> clazz, HandlerContext context) {
            ResolvableParamPredicate resolvable = context.paramPredicate()
                    .orElseThrow(() -> new IllegalStateException("paramPredicate is null"));
            HandlerResolverFactory resolverFactory = context.resolverFactory()
                    .orElseThrow(() -> new IllegalStateException("resolverFactory is null"));
            this.constructor = ConstructorUtils.extractResolvable(clazz, resolvable);
            Checks.checkState(this.constructor != null,
                    "There is no suitable constructor to instantiate class: " + clazz.getName());
            this.consParamResolvers = mergeConsParamResolvers(constructor, resolvable, resolverFactory);
            this.setterParamResolvers = mergeSetterParamResolvers(clazz, resolvable, resolverFactory);
            this.fieldParamResolvers = mergeFieldParamResolvers(clazz, resolvable, resolverFactory);
            this.context = context;
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<ConstructorParam, Resolver>[] mergeConsParamResolvers(
                Constructor<?> constructor, ResolvableParamPredicate resolvable, HandlerResolverFactory factory) {
            List<ResolvableParam<ConstructorParam, Resolver>> resolvers = new LinkedList<>();
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                ConstructorParam param = new ConstructorParamImpl(constructor, i);
                if (!resolvable.test(param)) {
                    continue;
                }
                resolvers.add(getResolverWrap(param, factory));
            }
            return resolvers.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<MethodParam, Resolver>[] mergeSetterParamResolvers(
                Class<?> clazz, ResolvableParamPredicate resolvable, HandlerResolverFactory factory) {
            List<ResolvableParam<MethodParam, Resolver>> resolvers = new LinkedList<>();
            ReflectionUtils.getAllDeclaredMethods(clazz).stream()
                    .filter(ReflectionUtils::isSetter)
                    .forEach(m -> {
                        MethodParam param = new MethodParamImpl(m, 0);
                        if (resolvable.test(param)) {
                            resolvers.add(getResolverWrap(param, factory));
                        }
                    });
            return resolvers.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<FieldParam, Resolver>[] mergeFieldParamResolvers(
                Class<?> clazz, ResolvableParamPredicate resolvable, HandlerResolverFactory factory) {
            List<ResolvableParam<FieldParam, Resolver>> resolvers = new LinkedList<>();
            ReflectionUtils.getAllDeclaredFields(clazz)
                    .forEach(f -> {
                        FieldParam param = new FieldParamImpl(f);
                        if (resolvable.test(param)) {
                            resolvers.add(getResolverWrap(param, factory));
                        }
                    });
            return resolvers.toArray(new ResolvableParam[0]);
        }

        private <P extends Param> ResolvableParam<P, Resolver> getResolverWrap(P param,
                                                                               HandlerResolverFactory factory) {
            ParamResolver paramResolver = factory.getParamResolver(param);
            if (paramResolver != null) {
                return new ResolvableParam<>(param, new AdvisedParamResolver(paramResolver,
                        factory.getParamResolverAdvices(param, paramResolver)));
            } else {
                ContextResolver contextResolver = factory.getContextResolver(param);
                if (contextResolver != null) {
                    return new ResolvableParam<>(param, new AdvisedContextResolver(contextResolver));
                } else {
                    throw new IllegalArgumentException("There is no resolver to handle param: ["
                            + param.toString() + "]");
                }
            }
        }
    }
}

