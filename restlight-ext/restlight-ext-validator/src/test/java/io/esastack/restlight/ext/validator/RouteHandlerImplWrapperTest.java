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
package io.esastack.restlight.ext.validator;

import esa.commons.ClassUtils;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.LinkedHandlerInvoker;
import io.esastack.restlight.core.handler.impl.HandlerInvokerImpl;
import io.esastack.restlight.core.handler.impl.RouteHandlerImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.handler.method.RouteHandlerMethodImpl;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteHandlerImplWrapperTest {

    private static final BeanSubject subject = new BeanSubject();

    private static final Map<String, RouteHandlerImpl> handlerMethodAdapters = new LinkedHashMap<>(16);

    private RouteHandlerImpl handlerMethodInvokerAdapter;

    private static final Object[] simpleArg = {"Hello"};
    private static final String MESSAGE_INTERPOLATOR_FILE = "validation-message";

    private final HttpRequest request = MockHttpRequest.aMockRequest().build();
    private final HttpResponse response = MockHttpResponse.aMockResponse().build();

    private static final Validator validator =
            Validation.byDefaultProvider().configure().messageInterpolator(new ResourceBundleMessageInterpolator(
                    new PlatformResourceBundleLocator(MESSAGE_INTERPOLATOR_FILE)))
                    .buildValidatorFactory()
                    .getValidator();

    @BeforeAll
    static void setUp() {
        Locale.setDefault(Locale.CHINA);
        ClassUtils.userDeclaredMethods(BeanSubject.class)
                .forEach((method -> {
                    final HandlerMethod handlerMethod = HandlerMethodImpl.of(BeanSubject.class, method);
                    HandlerInvoker invoker = new HandlerInvokerImpl(handlerMethod, new BeanSubject());

                    final MethodDescriptor descriptor =
                            validator.getConstraintsForClass(handlerMethod.beanType())
                                    .getConstraintsForMethod(method.getName(), method.getParameterTypes());

                    invoker = LinkedHandlerInvoker.immutable(new BeanValidationHandlerAdvice[]{
                            new BeanValidationHandlerAdvice(validator, subject, method,
                                    descriptor != null && descriptor.hasConstrainedParameters(),
                                    descriptor != null && descriptor.hasConstrainedReturnValue())},
                            invoker);
                    handlerMethodAdapters.put(method.getName(),
                            new RouteHandlerImpl(RouteHandlerMethodImpl.of(handlerMethod,
                                    false, null), subject, invoker));
                }));
    }

    @Test
    void testNormalInvoke() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        final Object returnValue = handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response),
                simpleArg);
        assertEquals(simpleArg[0], returnValue);
    }

    @Test
    void testNormalWrapper() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        try {
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), simpleArg);
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
        }
    }

    @Test
    void testMessageWrapper() {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        try {
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), simpleArg);
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    void testParametersValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersValidation");

        try {
            Object[] args = {new BeanSubject.SimpleBean("abc", 34,
                    new BeanSubject.SimpleBean.InnerBean("a", 0)), ""};

            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(3L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersValidationWithGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersValidationWithGroup");

        try {
            Object[] args = {new BeanSubject.SimpleBean2("abc", 34,
                    new BeanSubject.SimpleBean2.InnerBean("a", 0)), ""};
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(2L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testReturnValueValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testReturnValueValidation");

        try {
            Object[] args = {"", -1};
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(4L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testReturnValueValidationWithGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testReturnValueValidationWithGroup");

        try {
            Object[] args = {"", -1};
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersAndReturnValueValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersAndReturnValueValidation");

        try {
            Object[] args = {"abc", -1, 100L};
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersAndReturnValueValidationWitGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersAndReturnValueValidationWithGroup");

        try {
            Object[] args = {"", -1, 100L};
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testInternationalCnSupport() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");

        // Test zh_CN
        try {
            handlerMethodInvokerAdapter.invoke(new RequestContextImpl(request, response), simpleArg);
        } catch (ConstraintViolationException e) {
            assertTrue(e.getConstraintViolations()
                    .toArray(new ConstraintViolation[]{})[0].getMessage().contains("CN"));
        }
    }

}
