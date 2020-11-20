package com.virtualFencingServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ViolationScheduler {

    RecordsDao recordsDao;

    @Autowired
    ViolationScheduler(RecordsDao recordsDao) {
        this.recordsDao = recordsDao;
    }

    @Scheduled(fixedRate = 1000)
    public void checkViolations() {
        System.out.println("test");
    }

}
