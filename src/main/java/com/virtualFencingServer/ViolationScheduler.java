package com.virtualFencingServer;

import com.virtualFencingServer.model.CheckInRequest;
import com.virtualFencingServer.model.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Component
public class ViolationScheduler {

    private final RecordsDao recordsDao;

    private final RestTemplate restTemplate;

    @Autowired
    ViolationScheduler(RecordsDao recordsDao) {
        this.recordsDao = recordsDao;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(fixedRate = 1000)
    public void checkViolations() {
        List<UserRecord> records = recordsDao.getViolationRecords();
        for (UserRecord record : records) {
            // Notify authorities if: responseTime > 20s.
            if (Instant.now().getEpochSecond() - record.getLastCheckIn().getEpochSecond() > 300) {
                System.out.println("VIOLATION BY: " + record.getNumber());
                recordsDao.checkIn(
                        new CheckInRequest(
                                record.getNumber(),
                                "abc",
                                record.getBssid(),
                                record.getDeviceMac()
                        )
                );
            }
            // Send a buzzer if: 10s < responseTime < 20s.
            else if (Instant.now().getEpochSecond() - record.getLastCheckIn().getEpochSecond() > 5) {
                if (!record.getPendingViolation()) {
                    triggerVerificationDevice(record.getUrl());
                }
                recordsDao.changePendingViolation(true, record.getNumber());
            }
        }
    }

    private void triggerVerificationDevice(String url) {
        restTemplate.postForEntity(url, "test", String.class);
    }

}
