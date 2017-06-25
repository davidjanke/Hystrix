package com.netflix.hystrix.contrib.javanica.test.spring.configuration.command;

import com.netflix.hystrix.contrib.javanica.test.common.configuration.command.BasicCommandPropertiesOnInterfaceTest;
import com.netflix.hystrix.contrib.javanica.test.spring.conf.AopCglibConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by david on 2017.06.25..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AopCglibConfig.class, CommandPropertiesOnInterfaceTest.CommandPropertiesOnInterfaceTestConfig.class})
public class CommandPropertiesOnInterfaceTest extends BasicCommandPropertiesOnInterfaceTest {


    @Autowired
    private MainUserService mainUserService;

    @Autowired
    private SecondaryUserService secondaryUserService;

    @Override
    protected MainUserService createMainUserService() {
        return mainUserService;
    }

    @Override
    protected SecondaryUserService createSecondaryUserService() {
        return secondaryUserService;
    }

    @Configurable
    public static class CommandPropertiesOnInterfaceTestConfig {

        @Bean
        public BasicCommandPropertiesOnInterfaceTest.MainUserService mainUserService() {
            return new BasicCommandPropertiesOnInterfaceTest.MainUserService();
        }

        @Bean
        public BasicCommandPropertiesOnInterfaceTest.SecondaryUserService secondaryUserService() {
            return new BasicCommandPropertiesOnInterfaceTest.SecondaryUserService();
        }
    }
}
