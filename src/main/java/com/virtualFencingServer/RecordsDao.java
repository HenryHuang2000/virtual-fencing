package com.virtualFencingServer;


import com.google.common.annotations.VisibleForTesting;
import com.virtualFencingServer.model.CheckInRequest;
import com.virtualFencingServer.model.RegistrationRequest;
import com.virtualFencingServer.model.UserRecord;
import com.virtualFencingServer.model.ViolationRecordQueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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
 * Responsible for all data access related to user records, which are stored in the {@code user_records} table.
 */
@Component
public class RecordsDao {
    private final NamedParameterJdbcOperations namedJdbc;
    private final RecordRowMapper recordRowMapper;
    private final Clock clock;

    @Autowired
    public RecordsDao(NamedParameterJdbcTemplate namedJdbc) {
        this(namedJdbc, Clock.systemUTC());
    }

    @VisibleForTesting
    RecordsDao(NamedParameterJdbcTemplate namedJdbc, Clock clock) {
        this.namedJdbc = requireNonNull(namedJdbc);
        this.recordRowMapper = new RecordRowMapper();
        this.clock = clock;
    }

    /**
     * Note that the string building arguments must be matched with the respective key in the map.
     * This was done rather than simple string building to protect against sql injections.
     */
    public List<UserRecord> getViolationRecords(ViolationRecordQueryParameters queryParameters) {

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

        return namedJdbc.query("SELECT * FROM user_records" + filter, queryArgMatcher, recordRowMapper);
    }

    public synchronized UserRecord register(RegistrationRequest registrationRequest) {

        Instant timestamp = Instant.now(clock);
        namedJdbc.update(
                "INSERT INTO user_records (last_check_in, number, password, bssid, vd_mac) VALUES " +
                        "(:timestamp, :number, :password, :bssid, :vd_mac)",
                new MapSqlParameterSource()
                        .addValue("timestamp", Timestamp.from(timestamp))
                        .addValue("number", registrationRequest.getPhoneNumber())
                        .addValue("password", registrationRequest.getPassword())
                        .addValue("bssid", registrationRequest.getBssid())
                        .addValue("vd_mac", registrationRequest.getMacAddress())
        );

        return getRecordFromNumber(registrationRequest.getPhoneNumber());

    }

    /**
     * Synchronised to avoid race conditions that may occur when multiple records are added at the same time.
     */
    public synchronized UserRecord checkIn(CheckInRequest checkInRequest) {

        String phoneNumber = checkInRequest.getPhoneNumber();
        // Verify BSSID.
        UserRecord userRecord = getRecordFromNumber(phoneNumber);
        if (!userRecord.getBssid().equals(checkInRequest.getBssid())) {
            // Password or BSSID incorrect.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The BSSID is incorrect"
            );
        }

        // Check if MAC Address exists. If it does not exist, set it.
        if (userRecord.getDeviceMac() == null) {
            namedJdbc.update(
                    "UPDATE user_records SET device_mac = :macAddress WHERE number = :number",
                    new MapSqlParameterSource()
                            .addValue("macAddress", checkInRequest.getMacAddress())
                            .addValue("number", phoneNumber)
            );
        } else if (!checkInRequest.getMacAddress().equals(userRecord.getDeviceMac())) {
            // Password or BSSID incorrect.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The MAC address is incorrect"
            );
        }

        namedJdbc.update(
                "UPDATE user_records SET last_check_in = :timestamp WHERE number = :number",
                new MapSqlParameterSource()
                        .addValue("timestamp", Timestamp.from(Instant.now(clock)))
                        .addValue("number", phoneNumber)
        );

        return getRecordFromNumber(phoneNumber);
    }

    private UserRecord getRecordFromNumber(String phoneNumber) {
        return namedJdbc.queryForObject(
                "SELECT * FROM user_records WHERE number = :number",
                new MapSqlParameterSource().addValue("number", phoneNumber),
                recordRowMapper);
    }

    /**
     * Converts SQL responses into UserRecords.
     */
    private static class RecordRowMapper implements RowMapper<UserRecord> {

        @Override
        public UserRecord mapRow(ResultSet resultSet, int i) throws SQLException {
            return new UserRecord(
                    resultSet.getTimestamp("last_check_in").toInstant(),
                    resultSet.getString("number"),
                    resultSet.getString("bssid"),
                    resultSet.getString("vd_mac"),
                    resultSet.getString("device_mac")
            );
        }
    }
}