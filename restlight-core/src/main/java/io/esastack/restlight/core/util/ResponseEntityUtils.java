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

import esa.commons.ClassUtils;
import esa.commons.Primitives;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.route.predicate.ProducesPredicate;

import java.util.List;

public final class ResponseEntityUtils {

    public static List<MediaType> getMediaTypes(RequestContext context) {
        List<MediaType> compatibleTypes = context.attrs().attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES).get();
        if (compatibleTypes == null) {
            return context.request().accepts();
        } else {
            return compatibleTypes;
        }
    }

    public static boolean isAssignableFrom(ResponseEntity entity, Class<?> target) {
        Class<?> entityType = ClassUtils.getUserType(entity.response().entity());
        if (entityType == null) {
            return false;
        }
        if (target.isAssignableFrom(entityType)) {
            return true;
        }
        // find return type from generic type of Future(CompletableFuture<T>, ListenableFuture<T>, Netty#Future<T>)
        Class<?> type = FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entityType, entity.genericType());
        if (type != null) {
            return target.isAssignableFrom(type);
        }
        return false;
    }

    public static boolean isPrimitiveOrWrapperType(ResponseEntity entity) {
        Class<?> entityType = ClassUtils.getUserType(entity.response().entity());
        if (entityType == null) {
            return false;
        }
        if (Primitives.isPrimitiveOrWraperType(entityType)) {
            return true;
        }
        // find return type from generic type of Future(CompletableFuture<T>, ListenableFuture<T>, Netty#Future<T>)
        Class<?> type = FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entityType, entity.genericType());
        if (type != null) {
            return Primitives.isPrimitiveOrWraperType(type);
        }
        return false;
    }

    private ResponseEntityUtils() {
    }

}

