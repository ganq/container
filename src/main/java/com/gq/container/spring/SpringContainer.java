package com.gq.container.spring;

import com.gq.container.Container;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringContainer implements Container {

    private static final Logger logger = Logger.getLogger(SpringContainer.class);


    public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

    static ClassPathXmlApplicationContext context;

    public static ClassPathXmlApplicationContext getContext() {
        return context;
    }

    public void start() {
        context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
        context.start();
    }

    public void stop() {
        try {
            if (context != null) {
                context.stop();
                context.close();
                context = null;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

}