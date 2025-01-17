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
package io.esastack.restlight.core.server.processor.schedule;

import esa.commons.Checks;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.core.config.ServerOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.server.Connection;
import io.esastack.restlight.core.server.connection.ConnectionHandler;
import io.esastack.restlight.core.server.connection.ConnectionInitHandler;
import io.esastack.restlight.core.server.connection.DisConnectionHandler;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.util.LoggerUtils;
import io.esastack.restlight.core.util.PromiseUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScheduledRestlightHandler implements RestlightHandler {

    private final DispatcherHandler dispatcher;
    private ScheduledHandler handler;
    private final List<Scheduler> schedulers = new LinkedList<>();
    private final RequestTaskHook hook;
    private final List<ConnectionInitHandler> initialConnections;
    private final List<ConnectionHandler> connections;
    private final List<DisConnectionHandler> disConnections;

    private volatile long terminationTimeoutSeconds;

    public ScheduledRestlightHandler(ServerOptions options,
                                     DispatcherHandler dispatcher) {
        this(options, dispatcher, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public ScheduledRestlightHandler(ServerOptions options,
                                     DispatcherHandler dispatcher,
                                     List<RequestTaskHook> hooks,
                                     List<ConnectionInitHandler> initialConnections,
                                     List<ConnectionHandler> connections,
                                     List<DisConnectionHandler> disConnections) {
        Checks.checkNotNull(options, "options");
        Checks.checkNotNull(dispatcher, "dispatcher");
        this.dispatcher = dispatcher;
        this.hook = hooks == null || hooks.isEmpty() ? t -> t : toRequestTaskHook(hooks);
        this.terminationTimeoutSeconds = options.getBizTerminationTimeoutSeconds();
        if (connections != null) {
            OrderedComparator.sort(connections);
            this.connections = Collections.unmodifiableList(connections);
        } else {
            this.connections = null;
        }
        if (disConnections != null) {
            OrderedComparator.sort(disConnections);
            this.disConnections = Collections.unmodifiableList(disConnections);
        } else {
            this.disConnections = null;
        }
        if (initialConnections != null) {
            OrderedComparator.sort(initialConnections);
            this.initialConnections = Collections.unmodifiableList(initialConnections);
        } else {
            this.initialConnections = null;
        }
    }

    @Override
    public synchronized void onStart() {
        final List<Route> routes = dispatcher.routes();
        final Set<Scheduler> tmp = new HashSet<>();
        for (Route route : routes) {
            Scheduler scheduler = route.scheduler();
            Checks.checkNotNull(scheduler);
            if (tmp.add(scheduler)) {
                this.schedulers.add(scheduler);
            }
        }
        this.handler = new ScheduledHandler(dispatcher, schedulers, hook);
    }

    @Override
    public List<Scheduler> schedulers() {
        return this.schedulers;
    }

    @Override
    public CompletableFuture<Void> process(RequestContext context) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        handler.process(context, promise);
        return promise;
    }

    @Override
    public void onConnectionInit(Connection connection) {
        if (initialConnections == null) {
            return;
        }

        for (ConnectionInitHandler h : initialConnections) {
            try {
                h.onConnectionInit(connection);
            } catch (Throwable th) {
                LoggerUtils.logger().error("Error occurred when executing ConnectionInitHandler#onConnectionInit", th);
            }
        }
    }

    @Override
    public void onConnected(Connection connection) {
        if (connections == null) {
            return;
        }

        for (ConnectionHandler h : connections) {
            try {
                h.onConnect(connection);
            } catch (Throwable th) {
                LoggerUtils.logger().error("Error occurred when executing ConnectionHandler#onConnect()", th);
            }
        }
    }

    @Override
    public void onDisconnected(Connection connection) {
        if (disConnections == null) {
            return;
        }

        for (DisConnectionHandler h : disConnections) {
            try {
                h.onDisconnect(connection);
            } catch (Throwable th) {
                LoggerUtils.logger().error("Error occurred when executing DisConnectionHandler#onDisconnect()",
                        th);
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        try {
            dispatcher.shutdown();

            if (schedulers.isEmpty()) {
                return;
            }

            schedulers.forEach(e -> {
                if (e instanceof ExecutorScheduler) {
                    Executor executor = ((ExecutorScheduler) e).executor();
                    if (executor instanceof ThreadPoolExecutor) {
                        LoggerUtils.logger()
                                .info("Try to shutdown scheduler({}) with {} actively executing task(s)",
                                        e.name(), ((ThreadPoolExecutor) executor).getActiveCount());
                    }
                }
            });

        } catch (Exception e) {
            LoggerUtils.logger().error("Error while trying to shutdown Restlight server.", e);
        }

        if (schedulers.size() == 1) {
            doShutdown(schedulers.get(0));
        } else {
            final CountDownLatch latch = new CountDownLatch(schedulers.size());
            new Thread(() -> schedulers.forEach(scheduler -> {
                try {
                    doShutdown(scheduler);
                } finally {
                    latch.countDown();
                }
            }), "Scheduler-Shutdown").start();

            try {
                latch.await();
            } catch (InterruptedException e) {
                LoggerUtils.logger()
                        .error("Error occurred during waiting submitted biz task to finish.", e);
            }
        }
    }

    private void doShutdown(Scheduler scheduler) {
        try {
            if (scheduler instanceof ExecutorScheduler) {
                Executor executor = ((ExecutorScheduler) scheduler).executor();
                if (executor instanceof ExecutorService) {
                    ExecutorService es = (ExecutorService) executor;
                    if (es instanceof ThreadPoolExecutor) {
                        LoggerUtils.logger()
                                .info("Try to shutdown scheduler({}) with {} actively executing task(s)",
                                        scheduler.name(), ((ThreadPoolExecutor) es).getActiveCount());
                    }
                    es.shutdown();
                    try {
                        // Blocks until all tasks completes, or timeout occurs, or current thread is interrupted
                        es.awaitTermination(terminationTimeoutSeconds, TimeUnit.SECONDS);
                    } finally {
                        final List<RequestTask> tasks =
                                es.shutdownNow()
                                        .stream()
                                        .filter(task -> task instanceof RequestTask)
                                        .map(task -> (RequestTask) task)
                                        .collect(Collectors.toList());

                        if (tasks.isEmpty()) {
                            LoggerUtils.logger().info("Succeed to shutdown scheduler({})", scheduler.name());
                        } else {
                            dispatcher.handleUnfinishedWorks(tasks);
                            LoggerUtils.logger()
                                    .warn("Succeed to shutdown scheduler({}) with unfinished {} task(s)",
                                            scheduler.name(),
                                            tasks.size());
                        }
                    }
                } else {
                    scheduler.shutdown();
                }
            } else {
                scheduler.shutdown();
            }
        } catch (Throwable e) {
            LoggerUtils.logger()
                    .error("Failed to shutdown scheduler(" + scheduler.name() + ").", e);
        }
    }

    public void setTerminationTimeoutSeconds(long terminationTimeoutSeconds) {
        this.terminationTimeoutSeconds = terminationTimeoutSeconds;
    }

    private static RequestTaskHook toRequestTaskHook(List<RequestTaskHook> hooks) {
        final RequestTaskHook[] arr = hooks.toArray(new RequestTaskHook[0]);
        if (arr.length == 1) {
            // unwrap loop
            final RequestTaskHook rth = arr[0];
            return task -> {
                RequestTask maybeWrapped = rth.onRequest(task);
                if (maybeWrapped == null) {
                    handleUncommitted(task);
                    return null;
                } else {
                    return maybeWrapped;
                }
            };
        } else {
            OrderedComparator.sort(arr);
            return task -> {
                RequestTask maybeWrapped = task;
                for (RequestTaskHook hook : arr) {
                    maybeWrapped = hook.onRequest(maybeWrapped);
                    if (maybeWrapped == null) {
                        handleUncommitted(task);
                        return null;
                    }
                }
                return maybeWrapped;
            };
        }
    }

    private static void handleUncommitted(RequestTask task) {
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger()
                    .debug("{} rejected by RequestTaskHook, but response haven't been committed", task);
        }
        if (!task.promise().toCompletableFuture().isDone()) {
            PromiseUtils.setSuccess(task.promise());
        }
    }
}
