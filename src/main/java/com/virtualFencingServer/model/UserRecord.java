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
public class UserRecord {

    private final Instant lastCheckIn;
    private final String number;
    private final String bssid;
    private final String vdMac;
    private final String deviceMac;
    private final String url;
    private final Boolean pendingViolation;

    @JsonCreator
    public UserRecord(
            @JsonProperty("last_check_in") Instant lastCheckIn,
            @JsonProperty("number") String number,
            @JsonProperty("bssid") String bssid,
            @JsonProperty("vd_mac") String vdMac,
            @JsonProperty("device_mac") String deviceMac,
            @JsonProperty("url") String url,
            @JsonProperty("pending_violation") Boolean pendingViolation
    ) {
        this.lastCheckIn = requireNonNull(lastCheckIn);
        this.number = number;
        this.bssid = requireNonNull(bssid);
        this.vdMac = requireNonNull(vdMac);
        this.deviceMac = deviceMac;
        this.url = requireNonNull(url);
        this.pendingViolation = pendingViolation;
    }

    @JsonProperty("last_check_in")
    public Instant getLastCheckIn() { return lastCheckIn; }

    @JsonProperty("number")
    public String getNumber() { return number; }

    @JsonProperty("bssid")
    public String getBssid() { return bssid; }

    @JsonProperty("vd_mac")
    public String getVdMac() { return vdMac; }

    @JsonProperty("device_mac")
    public String getDeviceMac() { return deviceMac; }

    @JsonProperty("url")
    public String getUrl() { return url; }

    @JsonProperty("pending_violation")
    public Boolean getPendingViolation() { return pendingViolation; }


    @Override
    public String toString() {
        return "ViolationRecord {" +
                "last_check_in=" + lastCheckIn +
                "number=" + number +
                "bssid=" + bssid +
                "vd_mac=" + vdMac +
                "device_mac=" + deviceMac +
                "url=" + url +
                "pending_violation=" + pendingViolation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecord that = (UserRecord) o;
        return lastCheckIn.equals(that.lastCheckIn) &&
                number.equals(that.number) &&
                bssid.equals(that.bssid) &&
                deviceMac.equals(that.deviceMac) &&
                pendingViolation.equals(that.pendingViolation) &&
                url.equals(that.url) &&
                vdMac.equals(that.vdMac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastCheckIn, number, bssid, vdMac, deviceMac, pendingViolation, url);
    }
}
