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

    private final String phoneNumber;
    private final String password;
    private final String bssid;
    private final String macAddress;

    @JsonCreator
    public RegistrationRequest(
            String phoneNumber,
            String password,
            String bssid,
            String macAddress
    ) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.bssid = bssid;
        this.macAddress = macAddress;
    }

    @JsonProperty("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("bssid")
    public String getBssid() {
        return bssid;
    }

    @JsonProperty("macAddress")
    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return "ViolationRecordRequest {" +
                "phoneNumber=" + phoneNumber +
                "password=" + password +
                "bssid=" + bssid +
                "macAddress=" + macAddress +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationRequest that = (RegistrationRequest) o;
        return phoneNumber.equals(that.phoneNumber) &&
                password.equals(that.password) &&
                macAddress.equals(that.macAddress) &&
                bssid.equals(that.bssid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, bssid, password, macAddress);
    }
}