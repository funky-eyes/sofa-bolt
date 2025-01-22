/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadFactory;
import com.alipay.remoting.config.ConfigManager;

/**
 * @author jianbin@apache.org
 */
public class BoltThreadFactory {

    boolean virtualThreadEnabled = ConfigManager.global_virtual_thread_enabled();

    public ThreadFactory createThreadFactory(String name) {
        return createThreadFactory(name, false);
    }

    public ThreadFactory createThreadFactory(String prefix, boolean daemon) {
        if (virtualThreadEnabled) {
            try {
                Method method = Thread.class.getDeclaredMethod("ofVirtual");
                Object virtualThreadBuilder = method.invoke(null);
                method = virtualThreadBuilder.getClass().getMethod("name", String.class);
                method.setAccessible(true);
                virtualThreadBuilder = method.invoke(virtualThreadBuilder, prefix);
                method.setAccessible(true);
                return (ThreadFactory) method.invoke(virtualThreadBuilder);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Use virtual thread pool failed, fallback to common thread pool", e);
            }
        }
        return new NamedThreadFactory(prefix, daemon);
    }

    public static BoltThreadFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static class InstanceHolder {
        private static final BoltThreadFactory INSTANCE = new BoltThreadFactory();
    }

}
