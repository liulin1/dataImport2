package com.genomics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(2);
        final int[] times = {0};
        final LocalDateTime[] dateTimes = {
                LocalDateTime.now(),
                LocalDateTime.now()
        };
        final ScheduledFuture[] futures = new ScheduledFuture[]{
                cleanTempData(scheduExec, dateTimes),
                insertTempData(scheduExec, dateTimes)
        };
        ScheduledExecutorService scheduDeamon =  Executors.newSingleThreadScheduledExecutor();
        scheduDeamon.scheduleWithFixedDelay(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                if (futures[0].isCancelled() || now.isAfter(dateTimes[0].plusMinutes(maxLifetime))) {
                    LocalDateTime lastActiveTime = dateTimes[0];
                    if (!futures[0].isCancelled()) {
                        log.info("trying to cancel clean service ... {}", futures[0].cancel(true));
                    }
                    log.info("trying to rescue clean service ...");
                    futures[0] = cleanTempData(scheduExec, dateTimes);
//                    String logMsg = MessageFormat.format(messageByLocaleService.getMessage("log.error.backup-schedule"),
//                            lastActiveTime, now, !futures[0].isCancelled());
//                    log.info(logMsg);
//                    mailService.sendThreadBrokenWarning(logMsg);
                }
                if (futures[1].isCancelled() || now.isAfter(dateTimes[1].plusMinutes(maxLifetime))) {
                    LocalDateTime lastActiveTime = dateTimes[1];
                    if (!futures[1].isCancelled()) {
                        log.info("trying to cancel insert service ... {}", futures[1].cancel(true));
                    }
                    log.info("trying to rescue insert service ...");
                    futures[1] = insertTempData(scheduExec, dateTimes);
//                    String errorMsg = MessageFormat.format(
//                            messageByLocaleService.getMessage("log.error.truncate-schedule"),
//                            lastActiveTime, now, !futures[1].isCancelled());
//                    log.error(errorMsg);
//                    mailService.sendThreadBrokenWarning(errorMsg);
                }
                log.info("Threads' health check ... done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, frequency * 10, TimeUnit.MILLISECONDS);
    }

    /**
     * 插入excel数据
     * @param scheduExec
     * @param dateTimes
     * @return
     */
    private ScheduledFuture insertTempData(ScheduledExecutorService scheduExec, LocalDateTime[] dateTimes) {
        return scheduExec.scheduleWithFixedDelay(()->{
            log.info("insert service begin ...");
            dateTimes[0] = LocalDateTime.now();
//            backupService.run();
            log.info("insert service end.");
        }, 0, frequency, TimeUnit.MILLISECONDS);
    }

    /**
     * 删除数据库temp数据
     * @param scheduExec
     * @param dateTimes
     * @return
     */
    private ScheduledFuture cleanTempData(ScheduledExecutorService scheduExec, LocalDateTime[] dateTimes) {
        return scheduExec.scheduleWithFixedDelay(()->{
            log.info("clean service begin ...");
            dateTimes[0] = LocalDateTime.now();
//            backupService.run();
            log.info("clean service end.");
        }, 10000, frequency, TimeUnit.MILLISECONDS);
    }

}
