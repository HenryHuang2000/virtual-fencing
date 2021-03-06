package com.virtualFencingServer;


import com.google.common.annotations.VisibleForTesting;
import com.virtualFencingServer.model.CheckInRequest;
import com.virtualFencingServer.model.RegistrationRequest;
import com.virtualFencingServer.model.UserRecord;
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
import java.util.List;

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

    public List<UserRecord> getViolationRecords() {

        return namedJdbc.query("SELECT * FROM user_records", recordRowMapper);
    }

    public synchronized UserRecord register(RegistrationRequest registrationRequest) {

        Instant timestamp = Instant.now(clock);
        namedJdbc.update(
                "INSERT INTO user_records (last_check_in, first_check_in, bssid, vd_mac, url) VALUES " +
                        "(:timestamp, :timestamp, :bssid, :vd_mac, :url)",
                new MapSqlParameterSource()
                        .addValue("timestamp", Timestamp.from(timestamp))
                        .addValue("bssid", registrationRequest.getBssid())
                        .addValue("vd_mac", registrationRequest.getMacAddress())
                        .addValue("url", registrationRequest.getUrl())
        );

        return getRecordFromBssid(registrationRequest.getBssid());

    }

    /**
     * Synchronised to avoid race conditions that may occur when multiple records are added at the same time.
     */
    public synchronized UserRecord checkIn(CheckInRequest checkInRequest) {

        String phoneNumber = checkInRequest.getPhoneNumber();
        // Verify BSSID.
        UserRecord userRecord = getRecordFromBssid(checkInRequest.getBssid());
        if (!userRecord.getBssid().equals(checkInRequest.getBssid())) {
            //BSSID incorrect.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The BSSID is incorrect"
            );
        }

        // Check if MAC Address exists. If it does not exist, set the MAC and number.
        if (userRecord.getDeviceMac() == null) {
            namedJdbc.update(
                    "UPDATE user_records SET (device_mac, number) = (:macAddress, :number) WHERE bssid = :bssid",
                    new MapSqlParameterSource()
                            .addValue("macAddress", checkInRequest.getMacAddress())
                            .addValue("number", checkInRequest.getPhoneNumber())
                            .addValue("bssid", checkInRequest.getBssid())
            );
        } else if (
                !checkInRequest.getMacAddress().equals(userRecord.getDeviceMac()) &&
                !checkInRequest.getMacAddress().equals(userRecord.getVdMac())
        ) {
            // MAC address incorrect.
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

        changePendingViolation(false, phoneNumber);

        return getRecordFromBssid(checkInRequest.getBssid());
    }

    public synchronized void changePendingViolation(boolean isPending, String phoneNumber) {
        namedJdbc.update(
            "UPDATE user_records SET pending_violation = :isPending WHERE number = :number",
                new MapSqlParameterSource()
                        .addValue("isPending", isPending)
                        .addValue("number", phoneNumber)
        );
    }

    private UserRecord getRecordFromBssid(String bssid) {
        return namedJdbc.queryForObject(
                "SELECT * FROM user_records WHERE bssid = :bssid",
                new MapSqlParameterSource().addValue("bssid", bssid),
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
                    resultSet.getString("device_mac"),
                    resultSet.getString("url"),
                    resultSet.getBoolean("pending_violation")
            );
        }
    }
}