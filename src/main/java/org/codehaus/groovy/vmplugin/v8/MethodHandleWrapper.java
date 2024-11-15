/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.vmplugin.v8;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrap method handles
 *
 * @since 3.0.0
 */
class MethodHandleWrapper {
    private final MethodHandle cachedMethodHandle;
    private final MethodHandle targetMethodHandle;
    private final boolean canSetTarget;
    private final AtomicLong latestHitCount = new AtomicLong(0);

    public MethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, boolean canSetTarget) {
        this.cachedMethodHandle = cachedMethodHandle;
        this.targetMethodHandle = targetMethodHandle;
        this.canSetTarget = canSetTarget;
    }

    public static MethodHandleWrapper getNullMethodHandleWrapper() {
        return NullMethodHandleWrapper.INSTANCE;
    }

    public MethodHandle getCachedMethodHandle() {
        return cachedMethodHandle;
    }

    public MethodHandle getTargetMethodHandle() {
        return targetMethodHandle;
    }

    public boolean isCanSetTarget() {
        return canSetTarget;
    }

    public long incrementLatestHitCount() {
        return latestHitCount.incrementAndGet();
    }

    public void resetLatestHitCount() {
        latestHitCount.set(0);
    }

    public long getLatestHitCount() {
        return latestHitCount.get();
    }

    private static class NullMethodHandleWrapper extends MethodHandleWrapper {
        public static final NullMethodHandleWrapper INSTANCE = new NullMethodHandleWrapper(null, null, false);

        private NullMethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, boolean canSetTarget) {
            super(cachedMethodHandle, targetMethodHandle, canSetTarget);
        }
    }
}
