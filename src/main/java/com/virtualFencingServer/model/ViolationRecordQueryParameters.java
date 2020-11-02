package com.virtualFencingServer.model;

import java.util.Optional;

/**
 * The fields in this class store all the possible query parameters of GET requests.
 * Does not handle deserializing from JSON.
 */
public class ViolationRecordQueryParameters {
    private final Optional<String> name;
    private final Optional<String> id;
    private final Optional<String> location;
    private final Optional<Long> timestampFirst;
    private final Optional<Long> timestampLast;

    public ViolationRecordQueryParameters(
            Optional<String> name,
            Optional<String> id,
            Optional<String> location,
            Optional<Long> timestampFirst,
            Optional<Long> timestampLast
    ) {
        this.name = name;
        this.id = id;
        this.location = location;
        this.timestampFirst = timestampFirst;
        this.timestampLast = timestampLast;
    }

    public Optional<String> getName() { return name; }

    public Optional<String> getId() { return id; }

    public Optional<String> getLocation() { return location; }

    public Optional<Long> getTimestampFirst() {
        return timestampFirst;
    }

    public Optional<Long> getTimestampLast() {
        return timestampLast;
    }

    @Override
    public String toString() {
        return "ViolationRecordQueryParameters{" +
                ", timestampFirst=" + timestampFirst +
                ", timestampLast=" + timestampLast +
                ", name=" + name +
                ", id=" + id +
                ", location=" + location +
                '}';
    }
}
