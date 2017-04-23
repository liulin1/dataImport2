package com.genomics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
public class DataImport2Application implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(DataImport2Application.class);
    @Value("${frequency}")
    private Integer frequency;
    @Value("${maxSingleThreadLifetime}")
    private Long maxLifetime;
    @Value("${dataBasePath}")
    private String dataBasePath;

    /**
     * 程序主入口
     * @param args
     */
	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataImport2Application.class);
        app.setWebEnvironment(false);
        app.run(args);
	}

    /**
     * 实现CommandLineRunner接口的run函数，会在应用程序启动后首先被调用
     * @param arg0
     * @throws Exception
     */
    public void run(String... arg0) throws Exception{
        log.info("initializing configuration..." +
                "\nfrequency:{}" +
                "\nmaxSingleThreadLifetime:{}" +
                "\ndataBasePath:{}"
                , frequency, maxLifetime, dataBasePath);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    }


}
