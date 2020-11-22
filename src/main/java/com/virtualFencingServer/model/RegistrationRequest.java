package com.virtualFencingServer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * POST request bodies are stored in this class. Handles the
 * deserialization of an POST request body.
 *
 */
public class RegistrationRequest {

    private final String bssid;
    private final String macAddress;
    private final String url;

    @JsonCreator
    public RegistrationRequest(
            String bssid,
            String macAddress,
            String url
    ) {
        this.bssid = bssid;
        this.macAddress = macAddress;
        this.url = url + "/notify";
    }

    @JsonProperty("bssid")
    public String getBssid() {
        return bssid;
    }

    @JsonProperty("macAddress")
    public String getMacAddress() {
        return macAddress;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ViolationRecordRequest {" +
                "bssid=" + bssid +
                "macAddress=" + macAddress +
                "url=" + url +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationRequest that = (RegistrationRequest) o;
        return macAddress.equals(that.macAddress) &&
                url.equals(that.url) &&
                bssid.equals(that.bssid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bssid, macAddress, url);
    }
}