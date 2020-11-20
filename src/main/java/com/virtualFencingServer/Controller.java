package com.virtualFencingServer;


import com.virtualFencingServer.model.CheckInRequest;
import com.virtualFencingServer.model.RegistrationRequest;
import com.virtualFencingServer.model.UserRecord;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Handles incoming requests for everything relating to violation records.
 */
@RestController
@RequestMapping("/api")
public class Controller {

    private final RecordsDao recordsDao;

    public Controller(RecordsDao recordsDao) {
        this.recordsDao = Objects.requireNonNull(recordsDao);
    }

    /**
     * @return A list of user records.
     */
    @GetMapping("/records")
    public List<UserRecord> getUserRecords() {
        return recordsDao.getViolationRecords();
    }

    /**
     * Registers a verification device.
     */
    @PostMapping("/register")
    public UserRecord register(@RequestBody RegistrationRequest registrationRequest) {
        return recordsDao.register(registrationRequest);
    }

    /**
     * Used to generate a violation record.
     */
    @PostMapping("/check-in")
    public UserRecord checkIn(@RequestBody CheckInRequest checkInRequest) {
        return recordsDao.checkIn(checkInRequest);
    }
}