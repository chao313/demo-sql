package demo.sql.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AwareUtil implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, MessageSourceAware, ApplicationEventPublisherAware, ResourceLoaderAware, EnvironmentAware {

    public static String beanName;
    public static BeanFactory beanFactory;
    public static ApplicationContext applicationContext;
    public static ApplicationEventPublisher applicationEventPublisher;
    public static MessageSource messageSource;
    public static ResourceLoader resourceLoader;
    public static Environment environment;

    /**
     * BeanNameAware
     *
     * @param s
     */
    @Override
    public void setBeanName(String s) {
        this.beanName = s;
    }


    /**
     * BeanFactoryAware
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * ApplicationContextAware
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * ApplicationEventPublisherAware
     *
     * @param applicationEventPublisher
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * MessageSourceAware
     *
     * @param messageSource
     */
    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * ResourceLoaderAware
     *
     * @param resourceLoader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
