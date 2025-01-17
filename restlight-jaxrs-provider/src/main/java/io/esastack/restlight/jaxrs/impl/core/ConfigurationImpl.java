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
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationImpl implements Configuration {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationImpl.class);

    /**
     * All classes and instance types which have registered, for checking duplicate purpose.
     */
    private final Set<Class<?>> classes = new HashSet<>();

    private final Map<String, Object> properties = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Integer>> contracts = new HashMap<>();
    private final List<Object> enabledFeatures = new LinkedList<>();

    /**
     * Registered classes, includes providers, features, root resources and so on, which should be instantiated and
     * injected soon.
     */
    private final Set<Class<?>> resourcesClasses = new HashSet<>();

    /**
     * Registered instances, includes providers, features, root resources and so on, which should be injected soon.
     */
    private final Set<Object> resourcesInstances = new HashSet<>();

    /**
     * Registered component classes, includes providers, features and so on.
     */
    private final List<Class<?>> providerClasses = new LinkedList<>();

    /**
     * Registered component instances, includes providers, features and so on.
     */
    private final List<Object> providerInstances = new LinkedList<>();

    public ConfigurationImpl(ConfigurationImpl from) {
        this.classes.addAll(from.classes);
        this.properties.putAll(from.properties);
        this.contracts.putAll(from.contracts);
        this.enabledFeatures.addAll(from.enabledFeatures);
        this.resourcesClasses.addAll(from.resourcesClasses);
        this.resourcesInstances.addAll(from.resourcesInstances);
        this.providerClasses.addAll(from.providerClasses);
        this.providerInstances.addAll(from.providerInstances);
    }

    public ConfigurationImpl() {
    }

    @Override
    public RuntimeType getRuntimeType() {
        return RuntimeType.SERVER;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableCollection(properties.keySet());
    }

    @Override
    public boolean isEnabled(Feature feature) {
        for (Object f : enabledFeatures) {
            if (f.equals(feature)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled(Class<? extends Feature> featureClass) {
        for (Object f : enabledFeatures) {
            if (featureClass.equals(ClassUtils.getUserType(f))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRegistered(Object component) {
        for (Object instance : getProviderInstances()) {
            if (instance.equals(component)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRegistered(Class<?> componentClass) {
        for (Object instance : getProviderInstances()) {
            if (ClassUtils.getUserType(instance).equals(componentClass)) {
                return true;
            }
        }

        for (Class<?> clazz : getProviderClasses()) {
            if (clazz.equals(componentClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
        Map<Class<?>, Integer> values = contracts.get(componentClass);
        return values == null ? Collections.emptyMap() : Collections.unmodifiableMap(values);
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>(resourcesClasses);
        classes.addAll(providerClasses);
        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Set<Object> getInstances() {
        final Set<Object> instances = new HashSet<>(resourcesInstances);
        instances.addAll(providerInstances);
        return Collections.unmodifiableSet(instances);
    }

    public void setProperty(String name, Object value) {
        Checks.checkNotNull(value, "value");
        properties.put(name, value);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public Collection<Class<?>> getProviderClasses() {
        return Collections.unmodifiableList(providerClasses);
    }

    public Collection<Object> getProviderInstances() {
        return Collections.unmodifiableList(providerInstances);
    }

    public void addEnabledFeature(Object feature) {
        Class<?> target = ClassUtils.getUserType(feature);
        if (Feature.class.isAssignableFrom(ClassUtils.getUserType(feature))) {
            this.enabledFeatures.add(feature);
        } else {
            logger.error("Failed to register {} as Feature.", target);
        }
    }

    public void addResourceClass(Class<?> clazz) {
        if (checkState(clazz, false)) {
            return;
        }

        if (JaxrsUtils.isRootResource(clazz)) {
            this.resourcesClasses.add(clazz);
        } else {
            logger.error("Failed to register {} as Resource.", clazz);
        }
    }

    public void addResourceInstance(Object instance) {
        Class<?> clazz = ClassUtils.getUserType(instance);
        if (checkState(clazz, false)) {
            return;
        }

        if (JaxrsUtils.isRootResource(clazz)) {
            this.resourcesInstances.add(instance);
        } else {
            logger.warn("Registering {}(unrecognized resource) is ignored.", clazz);
        }
    }

    public void addProviderInstance(Object instance, Map<Class<?>, Integer> contracts) {
        Class<?> target = ClassUtils.getUserType(instance);
        if (checkState(target, true)) {
            return;
        }

        Map<Class<?>, Integer> checked = checkContracts(target, contracts);
        if (!checked.isEmpty()) {
            this.providerInstances.add(instance);
            this.contracts.put(target, checked);
        } else {
            logger.error("Failed to register {} as Provider.", target);
        }
    }

    public void addProviderClass(Class<?> target, Map<Class<?>, Integer> contracts) {
        if (checkState(target, true)) {
            return;
        }

        Map<Class<?>, Integer> checked = checkContracts(target, contracts);
        if (!checked.isEmpty()) {
            this.providerClasses.add(target);
            this.contracts.put(target, checked);
        } else {
            logger.warn("Registering {}(unrecognized provider) is ignored.", target);
        }
    }

    private Map<Class<?>, Integer> checkContracts(Class<?> target, Map<Class<?>, Integer> contracts) {
        Map<Class<?>, Integer> checked = new HashMap<>();
        for (Map.Entry<Class<?>, Integer> entry : contracts.entrySet()) {
            if (!entry.getKey().isAssignableFrom(target)) {
                logger.warn("Failed to register {} as {}", target, entry.getKey());
            } else {
                checked.put(entry.getKey(), entry.getValue());
            }
        }
        return checked;
    }

    private boolean checkState(Class<?> clazz, boolean checkConstrain) {
        if (checkConstrain && !JaxrsUtils.isServerSide(clazz)) {
            return true;
        }
        if (!this.classes.add(clazz)) {
            logger.warn("Registering {}(which has been registered before) is ignored.", clazz);
            return true;
        }
        return false;
    }

}
