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
package io.esastack.restlight.jaxrs.util;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.UrlUtils;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.impl.MappingImpl;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

public final class JaxrsMappingUtils {

    private JaxrsMappingUtils() {
    }

    /**
     * Extracts the default value from given {@link Param} which may be annotated by the JAX-RS annotation {@link
     * DefaultValue}.
     *
     * @param param param
     * @return default value
     */
    public static String extractDefaultValue(Param param) {
        if (param == null) {
            return null;
        }
        DefaultValue defaultValueAnn = JaxrsUtils.getAnnotation(param, DefaultValue.class);
        String defaultValue = null;
        if (defaultValueAnn != null) {
            defaultValue = defaultValueAnn.value();
        }
        return defaultValue;
    }

    /**
     * Extracts an instance of {@link Mapping} from given {@link Method} which may be annotated by the JAX-RS
     * annotations such as {@link GET}, {@link POST}, {@link Consumes}, {@link Produces} and so on...
     *
     * @param userType    type of target method's declaring class.
     * @param method      target method
     * @param contextPath context path
     * @return optional value of {@link Mapping}
     */
    public static Optional<Mapping> extractMapping(Class<?> userType,
                                                   Method method,
                                                   String contextPath) {

        if (method == null || userType == null) {
            return Optional.empty();
        }

        contextPath = ConverterUtils.standardContextPath(contextPath);
        final Class<?> annotatedUserType = findAnnotatedClass(userType).orElse(userType);
        final Method annotatedMethod = findAnnotatedMethod(method).orElse(method);

        final String parentPath = getAnnotation(annotatedUserType, Path.class)
                .map(Path::value)
                .orElse(null);

        final String path = getAnnotation(annotatedMethod, Path.class)
                .map(Path::value)
                .orElse(null);


        final String[] parentConsumes = getAnnotation(annotatedUserType, Consumes.class)
                .map(Consumes::value)
                .orElse(null);
        final String[] consumes = getAnnotation(annotatedMethod, Consumes.class)
                .map(Consumes::value)
                .orElse(null);

        final String[] parentProduces = getAnnotation(annotatedUserType, Produces.class)
                .map(Produces::value)
                .orElse(null);
        final String[] produces = getAnnotation(annotatedMethod, Produces.class)
                .map(Produces::value)
                .orElse(null);

        final String parentHttpMethod = getMethod(annotatedUserType);
        final String httpMethod = getMethod(annotatedMethod);

        if (path == null && httpMethod == null) {
            return Optional.empty();
        }

        if (parentPath == null
                && parentConsumes == null
                && parentProduces == null
                && parentHttpMethod == null) {
            return Optional.of(getMapping(contextPath, path, httpMethod, consumes, produces));
        } else {
            return Optional.of(getMapping(contextPath, parentPath, parentHttpMethod, parentConsumes, parentProduces)
                    .combine(getMapping(StringUtils.empty(), path, httpMethod, consumes, produces)));
        }
    }

    public static boolean isMethod(Method element) {
        Method annotatedMethod = findAnnotatedMethod(element).orElse(element);
        return JaxrsMappingUtils.getMethod(annotatedMethod) != null;
    }

    private static String getMethod(AnnotatedElement element) {
        String method =
                getAnnotation(element, HttpMethod.class)
                        .map(HttpMethod::value)
                        .orElse(null);
        if (method == null) {
            method = getAnnotation(element, GET.class)
                    .map(get -> HttpMethod.GET)
                    .orElse(null);
        }
        if (method == null) {
            method = getAnnotation(element, POST.class)
                    .map(get -> HttpMethod.POST)
                    .orElse(null);
        }
        if (method == null) {
            method = getAnnotation(element, PUT.class)
                    .map(get -> HttpMethod.PUT)
                    .orElse(null);
        }
        if (method == null) {
            method = getAnnotation(element, DELETE.class)
                    .map(get -> HttpMethod.DELETE)
                    .orElse(null);
        }

        if (method == null) {
            method = getAnnotation(element, PATCH.class)
                    .map(get -> HttpMethod.PATCH)
                    .orElse(null);
        }

        if (method == null) {
            method = getAnnotation(element, OPTIONS.class)
                    .map(get -> HttpMethod.OPTIONS)
                    .orElse(null);
        }

        if (method == null) {
            method = getAnnotation(element, HEAD.class)
                    .map(get -> HttpMethod.HEAD)
                    .orElse(null);
        }
        return method;
    }

    private static Mapping getMapping(String contextPath,
                                      String path,
                                      String httpMethod,
                                      String[] consumes,
                                      String[] produces) {
        final String p = ConverterUtils.standardContextPath(contextPath)
                + StringUtils.emptyIfNull(UrlUtils.prependLeadingSlash(path));
        MappingImpl mapping = Mapping.mapping(p)
                .consumes(consumes == null ? new String[0] : consumes)
                .produces(produces == null ? new String[0] : produces);
        if (!StringUtils.isEmpty(httpMethod)) {
            mapping = mapping.method(httpMethod);
        }
        return mapping;
    }

    private static <A extends Annotation> Optional<A> getAnnotation(AnnotatedElement element,
                                                                    Class<A> targetClass) {
        return Optional.ofNullable(
                AnnotationUtils.findAnnotation(element, targetClass));
    }

    private static Optional<Method> findAnnotatedMethod(Method method) {
        if (method == null) {
            return Optional.empty();
        }
        if (isAnnotatedElement(method)) {
            return Optional.of(method);
        }
        for (Method m : ClassUtils.findOverriddenMethods(method)) {
            if (isAnnotatedElement(m)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    private static Optional<Class<?>> findAnnotatedClass(Class<?> element) {
        if (element == null || Object.class.equals(element)) {
            return Optional.empty();
        }

        if (isAnnotatedElement(element)) {
            return Optional.of(element);
        }
        for (Class<?> interface0 : element.getInterfaces()) {
            if (isAnnotatedElement(interface0)) {
                return Optional.of(interface0);
            }
        }

        return findAnnotatedClass(element.getSuperclass());
    }

    private static boolean isAnnotatedElement(AnnotatedElement element) {
        return AnnotationUtils.hasAnnotation(element, Path.class)
                || AnnotationUtils.hasAnnotation(element, HttpMethod.class)
                || AnnotationUtils.hasAnnotation(element, Consumes.class)
                || AnnotationUtils.hasAnnotation(element, Produces.class);
    }

}
