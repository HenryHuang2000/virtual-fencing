package com.virtualFencingServer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * This model stores the format of all violation records. It also handles
 * serializing/deserializing to a JSON object.
 */
public class ViolationRecord {

    private final Instant timestamp;
    private final String name;
    private final String userId;
    private final String location;
    private final Boolean resolved;

    @JsonCreator
    public ViolationRecord(
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("name") String name,
            @JsonProperty("userId") String userId,
            @JsonProperty("location") String location,
            @JsonProperty("resolved") Boolean resolved
    ) {
        this.timestamp = requireNonNull(timestamp);
        this.name = requireNonNull(name);
        this.userId = requireNonNull(userId);
        this.location = requireNonNull(location);
        this.resolved = requireNonNull(resolved);
    }

    @JsonProperty("timestamp")
    public Instant getTimestamp() { return timestamp; }

    @JsonProperty("name")
    public String getName() { return name; }

    @JsonProperty("userId")
    public String getUserId() { return userId; }

    @JsonProperty("location")
    public String getLocation() { return location; }

    @JsonProperty("resolved")
    public Boolean getResolved() { return resolved; }


    @Override
    public String toString() {
        return "ViolationRecord {" +
                "timestamp=" + timestamp +
                "name=" + name +
                "userId=" + userId +
                "location=" + location +
                "resolved=" + resolved +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViolationRecord that = (ViolationRecord) o;
        return timestamp.equals(that.timestamp) &&
                name.equals(that.name) &&
                userId.equals(that.userId) &&
                location.equals(that.location) &&
                resolved.equals(that.resolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, userId, location, resolved);
    }
}
