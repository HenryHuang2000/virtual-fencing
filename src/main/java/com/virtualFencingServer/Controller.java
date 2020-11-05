package com.virtualFencingServer;


import com.virtualFencingServer.model.ViolationRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import com.virtualFencingServer.model.ViolationRecordRequest;
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

    private final ViolationDao violationDao;

    public Controller(ViolationDao violationDao) {
        this.violationDao = Objects.requireNonNull(violationDao);
    }

    /**
     * @param name: the name of the quarantined individual.
     * @param userId: the userId of the quarantined individual.
     * @param location: the location of the quarantined individual.
     * @param timestampFirst the earliest timestamp to return
     * @param timestampLast  the latest timestamp to return
     * @return A list of violation records that are filtered by the query parameters.
     */
    @GetMapping("/records")
    public List<ViolationRecord> getVersionRecords(
            @RequestParam("name") Optional<String> name,
            @RequestParam("userId") Optional<String> userId,
            @RequestParam("location") Optional<String> location,
            @RequestParam("resolved") Optional<Boolean> resolved,
            @RequestParam("timestampFirst") Optional<Long> timestampFirst,
            @RequestParam("timestampLast") Optional<Long> timestampLast
    ) {
        ViolationRecordQueryParameters queryParameters = new ViolationRecordQueryParameters(
                name,
                userId,
                location,
                resolved,
                timestampFirst,
                timestampLast
        );
        return violationDao.getViolationRecords(queryParameters);
    }

    /**
     * Used to generate a violation record.
     * @param violationRecordRequest is used to build the violation record.
     * @return A copy of the record saved in the database.
     */
    @PostMapping("/records")
    public ViolationRecord addVersionRecord(@RequestBody ViolationRecordRequest violationRecordRequest) {

        return violationDao.addViolationRecord(violationRecordRequest);
    }
}