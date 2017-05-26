package com.pcq.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by pcq on 2017/5/24.
 */
public class WandApplicationContextUtil implements ApplicationContextAware{
    private static  ApplicationContext applicationContext=null;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
