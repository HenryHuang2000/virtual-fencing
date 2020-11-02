package com.virtualFencingServer;


import com.virtualFencingServer.model.ViolationRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * @param id: the id of the quarantined individual.
     * @param location: the location of the quarantined individual.
     * @param timestampFirst the earliest timestamp to return
     * @param timestampLast  the latest timestamp to return
     * @return A list of violation records that are filtered by the query parameters.
     */
    @GetMapping("/records")
    public List<ViolationRecord> getVersionRecords(
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
        return violationDao.getViolationRecords(queryParameters);
    }

//    /**
//     * Used to generate a debug (non-release) version record. These versions are not intended to be released to the field.
//     * @param versionRecordRequest is used to build the version record.
//     * @return A copy of the record saved in the database.
//     */
//    @PostMapping("/records")
//    public VersionRecord addDebugVersionRecord(@RequestBody Optional<VersionRecordRequest> versionRecordRequest) {
//        return addVersionRecord(versionRecordRequest, false);
//    }
//
//    /**
//     * Used to generate a release build version record.
//     * @param versionRecordRequest is used to build the version record.     *
//     * @return A copy of the record saved in the database.
//     */
//    @PostMapping("/records/release")
//    public VersionRecord addReleaseVersionRecord(@RequestBody Optional<VersionRecordRequest> versionRecordRequest) {
//        return addVersionRecord(versionRecordRequest, true);
//    }
//
//    private VersionRecord addVersionRecord(Optional<VersionRecordRequest> versionRecordRequest, boolean isReleaseBuild) {
//        try {
//            // Check that the incoming version record request has valid fields.
//            VersionRecordRequest request = versionRecordRequest
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commit hash can not be null"));
//            return versionDao.addVersionRecord(
//                    isReleaseBuild,
//                    request.getAuthor(),
//                    request.getProjectName(),
//                    request.getMajorVersion(),
//                    request.getMinorVersion(),
//                    request.getCommitHash(),
//                    request.getDirty()
//            );
//        } catch (DuplicateKeyException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested patch already exists.");
//        }
//    }
}