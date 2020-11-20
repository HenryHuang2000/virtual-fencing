package com.virtualFencingServer;


import com.virtualFencingServer.model.CheckInRequest;
import com.virtualFencingServer.model.RegistrationRequest;
import com.virtualFencingServer.model.UserRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * @param name: the name of the quarantined individual.
     * @param id: the id of the quarantined individual.
     * @param location: the location of the quarantined individual.
     * @param timestampFirst the earliest timestamp to return
     * @param timestampLast  the latest timestamp to return
     * @return A list of violation records that are filtered by the query parameters.
     */
    @GetMapping("/records")
    public List<UserRecord> getVersionRecords(
            @RequestParam("name") Optional<String> name,
            @RequestParam("id") Optional<String> id,
            @RequestParam("location") Optional<String> location,
            @RequestParam("timestampFirst") Optional<Long> timestampFirst,
            @RequestParam("timestampLast") Optional<Long> timestampLast
    ) {
        ViolationRecordQueryParameters queryParameters = new ViolationRecordQueryParameters(
                name,
                id,
                location,
                timestampFirst,
                timestampLast
        );
        return recordsDao.getViolationRecords(queryParameters);
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