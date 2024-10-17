package com.schnarbiesnmeowers.nmsgateway.dtos;

import java.io.Serializable;
import java.util.List;

public class RequestCheck implements Serializable {

    private String url;
    private String ipAddress;

    private List<String> permissions;

    public RequestCheck(String url, String ipAddress, List<String> permissions) {
        this.url = url;
        this.ipAddress = ipAddress;
        this.permissions = permissions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
