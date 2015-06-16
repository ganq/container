package com.gq.container;

import com.gq.container.log4j.Log4jContainer;
import com.gq.container.spring.SpringContainer;
import com.gq.container.utils.ConfigUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;



public class Main {

    public static final String CONTAINER_KEY = "container";

    public static final String APPLICATION_NAME = "application.name";

    public static final String SHUTDOWN_HOOK_KEY = "shutdown.hook";

    private static final Logger logger = Logger.getLogger(Main.class);

    private static volatile boolean running = true;

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String config = ConfigUtils.getProperty(CONTAINER_KEY, "");
                args = Pattern.compile("\\s*[,]+\\s*").split(config);
            }

            final List<Container> containers = new ArrayList<Container>();
            for (int i = 0; i < args.length; i ++) {
                if ("spring".equals(args[i])) {
                    containers.add(new SpringContainer());
                }
                if ("log4j".equals(args[i])) {
                    containers.add(new Log4jContainer());
                }
            }
            logger.info("Use container type(" + Arrays.toString(args) + ") to run serivce.");

            boolean shutdownHook = ("true".equals(System.getProperty(SHUTDOWN_HOOK_KEY)) || "".equals(System.getProperty(SHUTDOWN_HOOK_KEY)));
            if (shutdownHook) {
	            Runtime.getRuntime().addShutdownHook(new Thread() {
	                public void run() {
	                    for (Container container : containers) {
	                        try {
	                            container.stop();
	                            logger.info("container " + container.getClass().getSimpleName() + " stopped!");
	                        } catch (Throwable t) {
	                            logger.error(t.getMessage(), t);
	                        }
	                        synchronized (Main.class) {
	                            running = false;
	                            Main.class.notify();
	                        }
	                    }
	                }
	            });
            }

            for (Container container : containers) {
                container.start();
                logger.info("container " + container.getClass().getSimpleName() + " started!");
            }
            logger.info(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + ConfigUtils.getProperty(APPLICATION_NAME) +  " service server started!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }

}