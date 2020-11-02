package com.virtualFencingServer;


import com.google.common.annotations.VisibleForTesting;
import com.virtualFencingServer.model.ViolationRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Responsible for all data access related to violation records, which are stored in the {@code violation_records} table.
 */
@Component
public class ViolationDao {
    private final NamedParameterJdbcOperations namedJdbc;
    private final ViolationRowMapper violationRowMapper;
    private final Clock clock;

    @Autowired
    public ViolationDao(NamedParameterJdbcTemplate namedJdbc) {
        this(namedJdbc, Clock.systemUTC());
    }

    @VisibleForTesting
    ViolationDao(NamedParameterJdbcTemplate namedJdbc, Clock clock) {
        this.namedJdbc = requireNonNull(namedJdbc);
        this.violationRowMapper = new ViolationRowMapper();
        this.clock = clock;
    }

    /**
     * Note that the string building arguments must be matched with the respective key in the map.
     * This was done rather than simple string building to protect against sql injections.
     */
    public List<ViolationRecord> getViolationRecords(ViolationRecordQueryParameters queryParameters) {

        Map<String, Object> queryArgMatcher = new HashMap<>();
        List<String> queryArgs = new ArrayList<>();

        queryParameters.getName().ifPresent(val -> {
            queryArgs.add("name = :name");
            queryArgMatcher.put("name", val);
        });
        queryParameters.getId().ifPresent(val -> {
            queryArgs.add("id = :id");
            queryArgMatcher.put("id", val);
        });
        queryParameters.getLocation().ifPresent(val -> {
            queryArgs.add("location = :location");
            queryArgMatcher.put("location", val);
        });
        queryParameters.getTimestampFirst().ifPresent(val -> {
            queryArgs.add("timestamp > :firstTime");
            queryArgMatcher.put("firstTime", Timestamp.from(Instant.ofEpochSecond(val)));
        });
        queryParameters.getTimestampLast().ifPresent(val -> {
            queryArgs.add("timestamp < :lastTime");
            queryArgMatcher.put("lastTime", Timestamp.from(Instant.ofEpochSecond(val)));
        });

        final String filter = queryArgs.isEmpty() ? "" : " WHERE " + String.join(" AND ", queryArgs);

        return namedJdbc.query("SELECT * FROM violation_records" + filter, queryArgMatcher, violationRowMapper);
    }

//
//    /**
//     * Synchronised to avoid race conditions that may occur when multiple records are added at the same time.
//     * @throws org.springframework.dao.DuplicateKeyException when attempting to insert using a patch version
//     * that already exists. This would most likely happen when trying to insert multiple release builds for a
//     * given set of {projectName, majorVersion, minorVersion} combinations.
//     */
//    public synchronized VersionRecord addVersionRecord(
//            boolean isReleaseBuild,
//            Optional<String> author,
//            Optional<String> tentativeProjectName,
//            int majorVersion,
//            int minorVersion,
//            String commitHash,
//            boolean dirty
//    ) {
//
//        String projectName = tentativeProjectName.orElse(DEFAULT_PROJECT_NAME);
//        int patchVersion = isReleaseBuild ? 0 : getPatchVersion(projectName, majorVersion, minorVersion);
//
//        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
//        namedJdbc.update(
//                "INSERT INTO version_records (timestamp, author, project_name, major, minor, patch, commit_hash, dirty)" +
//                        " VALUES (:timestamp,:author,:projectName,:majorVersion,:minorVersion,:patchVersion,:commitHash,:dirty)",
//                new MapSqlParameterSource()
//                    .addValue("timestamp", Timestamp.from(Instant.now(clock)))
//                    .addValue("author", author.orElse(null))
//                    .addValue("projectName", projectName)
//                    .addValue("majorVersion", majorVersion)
//                    .addValue("minorVersion", minorVersion)
//                    .addValue("patchVersion", patchVersion)
//                    .addValue("commitHash", commitHash)
//                    .addValue("dirty", dirty)
//                    ,
//                generatedKeyHolder,
//                new String[]{"id"}
//        );
//
//        // Return the inserted version record.
//        return getVersionRecordById(generatedKeyHolder.getKey());
//    }
//
//    @VisibleForTesting
//    VersionRecord getVersionRecordById(Number id) {
//        return namedJdbc.queryForObject(
//                "SELECT * FROM version_records WHERE id = :id",
//                Map.of("id", id),
//                firmwareVersionRowMapper
//        );
//    }
//
//    /**
//     * Searches the database to find the highest patch version for a given project name + major + minor version combination.
//     * @return The next patch version or 1 if no entry exists.
//     * (0 is reserved for production releases).
//     */
//    @VisibleForTesting
//    int getPatchVersion(String projectName, int majorVersion, int minorVersion) {
//        Integer highestPatchNum = namedJdbc.queryForObject(
//                "SELECT MAX(patch) FROM version_records WHERE project_name = :projectName AND " +
//                        "major = :majorVersion AND minor = :minorVersion ",
//                Map.of(
//                        "projectName", projectName,
//                        "majorVersion", majorVersion,
//                        "minorVersion", minorVersion
//                ),
//                Integer.class
//        );
//        return highestPatchNum == null ? 1 : highestPatchNum + 1;
//    }

    /**
     * Converts SQL responses into VersionRecords.
     */
    private static class ViolationRowMapper implements RowMapper<ViolationRecord> {

        @Override
        public ViolationRecord mapRow(ResultSet resultSet, int i) throws SQLException {
            return new ViolationRecord(
                    resultSet.getTimestamp("timestamp").toInstant(),
                    resultSet.getString("name"),
                    resultSet.getString("id"),
                    resultSet.getString("location")
            );
        }
    }
}