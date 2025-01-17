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
package io.esastack.restlight.ext.validator;

public class ConstraintDetail {

    private final String property;
    private final Object invalidValue;
    private final String message;

    public ConstraintDetail(String property, Object invalidValue, String message) {
        this.property = property;
        this.invalidValue = invalidValue;
        this.message = message;
    }

    public String getProperty() {
        return property;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ConstraintDetail{" + "property='" + property + '\'' +
                ", invalidValue='" + invalidValue + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

