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
    private final String id;
    private final String location;

    @JsonCreator
    public ViolationRecord(
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("name") String name,
            @JsonProperty("id") String id,
            @JsonProperty("location") String location
    ) {
        this.timestamp = requireNonNull(timestamp);
        this.name = requireNonNull(name);
        this.id = requireNonNull(id);
        this.location = requireNonNull(location);
    }

    @JsonProperty("timestamp")
    public Instant getTimestamp() { return timestamp; }

    @JsonProperty("name")
    public String getName() { return name; }

    @JsonProperty("id")
    public String getId() { return id; }

    @JsonProperty("location")
    public String getLocation() { return location; }


    @Override
    public String toString() {
        return "ViolationRecord {" +
                "timestamp=" + timestamp +
                "name=" + name +
                "id=" + id +
                "location=" + location +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViolationRecord that = (ViolationRecord) o;
        return timestamp.equals(that.timestamp) &&
                name.equals(that.name) &&
                id.equals(that.id) &&
                location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, id, location);
    }
}
