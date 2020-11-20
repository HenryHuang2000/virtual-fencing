package com.virtualFencingServer;

import com.virtualFencingServer.model.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ViolationScheduler {

    RecordsDao recordsDao;

    @Autowired
    ViolationScheduler(RecordsDao recordsDao) {
        this.recordsDao = recordsDao;
    }

    @Scheduled(fixedRate = 1000)
    public void checkViolations() {
        List<UserRecord> records = recordsDao.getViolationRecords();
        for (UserRecord record : records) {
            // Notify authorities if: responseTime > 20s.
            if (Instant.now().getEpochSecond() - record.getLastCheckIn().getEpochSecond() > 20) {
                System.out.println("VIOLATION BY: " + record.getNumber());
            }
            // Send a buzzer if: 10s < responseTime < 20s.
            else if (Instant.now().getEpochSecond() - record.getLastCheckIn().getEpochSecond() > 10) {
                System.out.println(record.getNumber());
            }
        }
    }

}
