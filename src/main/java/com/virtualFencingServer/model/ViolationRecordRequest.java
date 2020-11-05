package com.virtualFencingServer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * POST request bodies are stored in this class. Handles the
 * deserialization of an POST request body.
 *
 */
public class ViolationRecordRequest {

    private final String name;
    private final String userId;
    private final String location;

    @JsonCreator
    public ViolationRecordRequest(
            String name,
            String userId,
            String location
    ) {
        this.name = name;
        this.userId = userId;
        this.location = location;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "ViolationRecordRequest {" +
                "name=" + name +
                "userId=" + userId +
                "location=" + location +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViolationRecordRequest that = (ViolationRecordRequest) o;
        return name.equals(that.name) &&
                userId.equals(that.userId) &&
                location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, userId, location);
    }
}