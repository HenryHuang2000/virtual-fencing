package com.virtualFencingServer;


import com.google.common.annotations.VisibleForTesting;
import com.virtualFencingServer.model.ViolationRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import com.virtualFencingServer.model.ViolationRecordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
        queryParameters.getUserId().ifPresent(val -> {
            queryArgs.add("userId = :userId");
            queryArgMatcher.put("userId", val);
        });
        queryParameters.getLocation().ifPresent(val -> {
            queryArgs.add("location = :location");
            queryArgMatcher.put("location", val);
        });
        queryParameters.getResolved().ifPresent(val -> {
            queryArgs.add("resolved = :resolved");
            queryArgMatcher.put("resolved", val);
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


    /**
     * Synchronised to avoid race conditions that may occur when multiple records are added at the same time.
     * @throws org.springframework.dao.DuplicateKeyException when attempting to insert using a patch version
     * that already exists. This would most likely happen when trying to insert multiple release builds for a
     * given set of {projectName, majorVersion, minorVersion} combinations.
     */
    public synchronized ViolationRecord addViolationRecord(ViolationRecordRequest violationRecordRequest) {

        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        namedJdbc.update(
                "INSERT INTO violation_records (timestamp, name, user_id, location, resolved)" +
                        " VALUES (:timestamp,:name,:userId,:location,:resolved)",
                new MapSqlParameterSource()
                        .addValue("timestamp", Timestamp.from(Instant.now(clock)))
                        .addValue("name", violationRecordRequest.getName())
                        .addValue("userId", violationRecordRequest.getUserId())
                        .addValue("location", violationRecordRequest.getLocation())
                        .addValue("resolved", false),
                generatedKeyHolder,
                new String[]{"id"}
        );

        // Return the inserted version record.
        return getVersionRecordById(generatedKeyHolder.getKey());
    }

    @VisibleForTesting
    ViolationRecord getVersionRecordById(Number id) {
        return namedJdbc.queryForObject(
                "SELECT * FROM violation_records WHERE id = :id",
                Map.of("id", id),
                violationRowMapper
        );
    }

    /**
     * Converts SQL responses into VersionRecords.
     */
    private static class ViolationRowMapper implements RowMapper<ViolationRecord> {

        @Override
        public ViolationRecord mapRow(ResultSet resultSet, int i) throws SQLException {
            return new ViolationRecord(
                    resultSet.getTimestamp("timestamp").toInstant(),
                    resultSet.getString("name"),
                    resultSet.getString("user_id"),
                    resultSet.getString("location"),
                    resultSet.getBoolean("resolved")
            );
        }
    }
}