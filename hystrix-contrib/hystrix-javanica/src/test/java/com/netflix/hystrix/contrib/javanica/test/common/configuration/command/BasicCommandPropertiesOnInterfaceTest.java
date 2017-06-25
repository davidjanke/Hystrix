/**
 * Copyright 2016 Netflix, Inc.
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
package com.netflix.hystrix.contrib.javanica.test.common.configuration.command;

import com.netflix.hystrix.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.test.common.BasicHystrixTest;
import com.netflix.hystrix.contrib.javanica.test.common.domain.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dmgcodevil
 */
public abstract class BasicCommandPropertiesOnInterfaceTest extends BasicHystrixTest {

    private UserService mainUserService;
    private UserService secondaryUserService;

    protected abstract MainUserService createMainUserService();
    protected abstract SecondaryUserService createSecondaryUserService();

    @Before
    public void setUp() throws Exception {
        mainUserService = createMainUserService();
        secondaryUserService = createSecondaryUserService();
    }

    @Test
    public void testGetMainUser() throws NoSuchFieldException, IllegalAccessException {
        User u1 = mainUserService.getUser("1", "name: ");
        assertEquals("name: 1", u1.getName());
        assertProperties(1, "MainUserService.getUser", "UserGroupKey", "Test", HystrixEventType.SUCCESS, 110, false);
    }

    @Test
    public void testGetSecondaryUser() throws NoSuchFieldException, IllegalAccessException {
        User u2 = secondaryUserService.getUser("1", "name: ");
        assertEquals("name: 1", u2.getName());
        assertProperties(1, "SecondaryUserService.getUser", "UserGroupKey", "Test", HystrixEventType.SUCCESS, 1100, true);
    }

    @Test
    public void testGetMainUserDefaultPropertiesValues() {
        User u1 = mainUserService.getUserDefProperties("1", "name: ");
        assertEquals("name: 1", u1.getName());
        assertDefaultProperties(1, "MainUserService.getUserDefProperties", "MainUserService", "MainUserService", HystrixEventType.SUCCESS, 1000, true);
    }

    @Test
    public void testGetSecondaryUserDefaultPropertiesValues() {
        User u2 = secondaryUserService.getUserDefProperties("1", "name: ");
        assertEquals("name: 1", u2.getName());
        assertDefaultProperties(1, "SecondaryUserService.getUserDefProperties", "SecondaryUserService", "SecondaryUserService", HystrixEventType.SUCCESS, 1000, true);
    }

    private void assertProperties(int expectedExecutedCommands, String expectedCommandKey, String expectedGroupKey, String expectedThreadPoolName, HystrixEventType expectedHystrixEventType, int expectedTimeOutInMilliseconds, boolean expectedInterruptOnTimeout) {
        assertEquals(expectedExecutedCommands, HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size());
        HystrixInvokableInfo<?> command = HystrixRequestLog.getCurrentRequest()
                .getAllExecutedCommands().iterator().next();

        checkCommandProperties(command, expectedCommandKey, expectedGroupKey, expectedThreadPoolName, expectedHystrixEventType, expectedTimeOutInMilliseconds, expectedInterruptOnTimeout);
        HystrixThreadPoolProperties mainProperties = getThreadPoolProperties(command);
        checkThreadPoolProperties(mainProperties);
    }

    private void assertDefaultProperties(int expectedExecutedCommands, String expectedCommandKey, String expectedGroupKey, String expectedThreadPoolName, HystrixEventType expectedHystrixEventType, int expectedTimeOutInMilliseconds, boolean expectedInterruptOnTimeout) {
        assertEquals(expectedExecutedCommands, HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size());
        HystrixInvokableInfo<?> command = HystrixRequestLog.getCurrentRequest()
                .getAllExecutedCommands().iterator().next();

        checkCommandProperties(command, expectedCommandKey, expectedGroupKey, expectedThreadPoolName, expectedHystrixEventType, expectedTimeOutInMilliseconds, expectedInterruptOnTimeout);
    }

    private void checkCommandProperties(HystrixInvokableInfo<?> command, String expectedCommandKey, String expectedGroupKey, String expectedThreadPoolName, HystrixEventType expectedHystrixEventType, int expectedTimeOutInMilliseconds, boolean expectedInterruptOnTimeout) {
        assertEquals(expectedCommandKey, command.getCommandKey().name());
        assertEquals(expectedGroupKey, command.getCommandGroup().name());
        assertEquals(expectedThreadPoolName, command.getThreadPoolKey().name());
        assertTrue(command.getExecutionEvents().contains(expectedHystrixEventType));
        // assert properties
        assertEquals(expectedTimeOutInMilliseconds, command.getProperties().executionTimeoutInMilliseconds().get().intValue());
        assertEquals(expectedInterruptOnTimeout, command.getProperties().executionIsolationThreadInterruptOnTimeout().get());

    }

    private void checkThreadPoolProperties(HystrixThreadPoolProperties properties) {
        assertEquals(30, (int) properties.coreSize().get());
        assertEquals(35, (int) properties.maximumSize().get());
        assertEquals(true, properties.getAllowMaximumSizeToDivergeFromCoreSize().get());
        assertEquals(101, (int) properties.maxQueueSize().get());
        assertEquals(2, (int) properties.keepAliveTimeMinutes().get());
        assertEquals(15, (int) properties.queueSizeRejectionThreshold().get());
        assertEquals(1440, (int) properties.metricsRollingStatisticalWindowInMilliseconds().get());
        assertEquals(12, (int) properties.metricsRollingStatisticalWindowBuckets().get());
    }

    public static interface  UserService {
        public User getUser(String id, String name);
        public User getUserDefProperties(String id, String name);
    }

    public static class MainUserService implements UserService {

        @HystrixCommand(groupKey = "UserGroupKey", threadPoolKey = "Test",
                fallbackMethod = "mainFallback",
                commandProperties = {
                        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "110"),
                        @HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "false")
                },
                threadPoolProperties = {
                        @HystrixProperty(name = "coreSize", value = "30"),
                        @HystrixProperty(name = "maximumSize", value = "35"),
                        @HystrixProperty(name = "allowMaximumSizeToDivergeFromCoreSize", value = "true"),
                        @HystrixProperty(name = "maxQueueSize", value = "101"),
                        @HystrixProperty(name = "keepAliveTimeMinutes", value = "2"),
                        @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                        @HystrixProperty(name = "queueSizeRejectionThreshold", value = "15"),
                        @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1440")
                })
        public User getUser(String id, String name) {
            return new User(id, name + id); // it should be network call
        }

        @HystrixCommand
        public User getUserDefProperties(String id, String name) {
            return new User(id, name + id); // it should be network call
        }

        private User mainFallback(String id, String name) {return new User();}

    }

    public static class SecondaryUserService implements UserService {

        @HystrixCommand(groupKey = "UserGroupKey", threadPoolKey = "Test",
                fallbackMethod = "secondaryFallback",
                commandProperties = {
                        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1100"),
                        @HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true")
                },
                threadPoolProperties = {
                        @HystrixProperty(name = "coreSize", value = "30"),
                        @HystrixProperty(name = "maximumSize", value = "35"),
                        @HystrixProperty(name = "allowMaximumSizeToDivergeFromCoreSize", value = "true"),
                        @HystrixProperty(name = "maxQueueSize", value = "101"),
                        @HystrixProperty(name = "keepAliveTimeMinutes", value = "2"),
                        @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                        @HystrixProperty(name = "queueSizeRejectionThreshold", value = "15"),
                        @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1440")
                })
        public User getUser(String id, String name) {
            return new User(id, name + id); // it should be network call
        }

        @HystrixCommand
        public User getUserDefProperties(String id, String name) {
            return new User(id, name + id); // it should be network call
        }

        private User secondaryFallback(String id, String name) {return new User();}

    }
}
