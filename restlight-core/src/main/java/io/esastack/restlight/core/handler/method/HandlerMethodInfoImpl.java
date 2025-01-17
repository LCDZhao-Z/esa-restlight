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

package io.esastack.restlight.core.handler.method;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpStatus;

import java.util.Objects;

public class HandlerMethodInfoImpl implements HandlerMethodInfo {

    private final boolean locator;
    private final HandlerMethod handlerMethod;
    private final HttpStatus customStatus;

    private String strVal;

    public HandlerMethodInfoImpl(HandlerMethod handlerMethod,
                                 boolean locator,
                                 HttpStatus customStatus) {
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        this.handlerMethod = handlerMethod;
        this.locator = locator;
        this.customStatus = customStatus;
    }

    @Override
    public boolean isLocator() {
        return locator;
    }

    @Override
    public HandlerMethod handlerMethod() {
        return handlerMethod;
    }

    @Override
    public HttpStatus customStatus() {
        return customStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerMethodInfoImpl that = (HandlerMethodInfoImpl) o;
        return locator == that.locator && Objects.equals(handlerMethod, that.handlerMethod)
                && Objects.equals(customStatus, that.customStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locator, handlerMethod, customStatus);
    }

    @Override
    public String toString() {
        if (strVal == null) {
            final StringBuilder sb = new StringBuilder("HandlerMethodInfoImpl{");
            sb.append("locator=").append(locator);
            sb.append(", handlerMethod=").append(handlerMethod);
            sb.append(", customStatus=").append(customStatus);
            sb.append('}');
            strVal = sb.toString();
        }
        return strVal;
    }
}
