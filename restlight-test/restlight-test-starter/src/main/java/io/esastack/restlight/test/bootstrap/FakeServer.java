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
package io.esastack.restlight.test.bootstrap;

import esa.commons.Checks;
import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.server.processor.AbstractRestlightHandler;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

class FakeServer implements RestlightServer {

    final AbstractRestlightHandler handler;
    private volatile CompletableFuture<Void> stopFuture;

    FakeServer(RestlightHandler handler) {
        Checks.checkNotNull(handler, "handler");
        if (handler instanceof AbstractRestlightHandler) {
            this.handler = (AbstractRestlightHandler) handler;
        } else {
            throw new IllegalArgumentException("The type of handler should be AbstractRestlightHandler.");
        }
    }

    @Override
    public synchronized boolean isStarted() {
        return stopFuture != null;
    }

    @Override
    public synchronized void start() {
        handler.onStart();
        stopFuture = new CompletableFuture<>();
    }

    @Override
    public synchronized void shutdown() {
        handler.shutdown();
        stopFuture.complete(null);
    }

    @Override
    public void await() {
        if (stopFuture != null) {
            stopFuture.join();
        }
    }

    @Override
    public Executor ioExecutor() {
        return null;
    }

    @Override
    public Executor bizExecutor() {
        return null;
    }

    @Override
    public SocketAddress address() {
        return null;
    }
}
