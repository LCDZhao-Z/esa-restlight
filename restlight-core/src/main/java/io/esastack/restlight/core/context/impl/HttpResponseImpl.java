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
package io.esastack.restlight.core.context.impl;

import esa.commons.annotation.Internal;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.httpserver.core.Response;
import io.esastack.restlight.core.context.HttpResponse;

import java.util.function.Consumer;

/**
 * Default implementation of {@link HttpResponse} that wraps the {@link Response} as delegate.
 */
@Internal
public class HttpResponseImpl implements HttpResponse {

    private final Response res;
    private Object entity;

    public HttpResponseImpl(Response res) {
        this.res = res;
    }

    @Override
    public void status(int code) {
        res.setStatus(code);
    }

    @Override
    public int status() {
        return res.status();
    }

    @Override
    public void entity(Object entity) {
        this.entity = entity;
    }

    @Override
    public Object entity() {
        return this.entity;
    }

    @Override
    public boolean isKeepAlive() {
        return res.isKeepAlive();
    }

    @Override
    public HttpHeaders headers() {
        return res.headers();
    }

    @Override
    public HttpHeaders trailers() {
        return res.trailers();
    }

    @Override
    public void addCookie(Cookie cookie) {
        res.addCookie(cookie);
    }

    @Override
    public void addCookie(String name, String value) {
        res.addCookie(name, value);
    }

    /**
     * Add a listener to this response, this listener will be called after current response has been write.
     *
     * @param listener listener
     * @param t accept object
     */
    public <T> void onEnd(Consumer<T> listener, T t) {
        res.onEndFuture().addListener(f -> listener.accept(t));
    }

    @Override
    public String toString() {
        return res.toString();
    }

}
