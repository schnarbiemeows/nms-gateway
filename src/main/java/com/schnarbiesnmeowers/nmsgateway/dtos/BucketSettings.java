package com.schnarbiesnmeowers.nmsgateway.dtos;

import java.io.Serializable;

public class BucketSettings implements Serializable {

    private String url;

    private String domain;
    private int totalNumberOfRequestsPerSecond;
    private int totalWindowForRateLimitInMilliseconds;
    private int totalMaxBucketSize;

    private int numberOfRequestsPerSecondPerIpAddress;
    private int windowForRateLimitInMillisecondsPerIpAddress;
    private int maxBucketSizePerIpAddress;

    public BucketSettings(String url, String domain, int totalNumberOfRequestsPerSecond,
                          int totalWindowForRateLimitInMilliseconds, int totalMaxBucketSize,
                          int numberOfRequestsPerSecondPerIpAddress,
                          int windowForRateLimitInMillisecondsPerIpAddress, int maxBucketSizePerIpAddress) {
        this.url = url;
        this.domain = domain;
        this.totalNumberOfRequestsPerSecond = totalNumberOfRequestsPerSecond;
        this.totalWindowForRateLimitInMilliseconds = totalWindowForRateLimitInMilliseconds;
        this.totalMaxBucketSize = totalMaxBucketSize;
        this.numberOfRequestsPerSecondPerIpAddress = numberOfRequestsPerSecondPerIpAddress;
        this.windowForRateLimitInMillisecondsPerIpAddress = windowForRateLimitInMillisecondsPerIpAddress;
        this.maxBucketSizePerIpAddress = maxBucketSizePerIpAddress;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getTotalNumberOfRequestsPerSecond() {
        return totalNumberOfRequestsPerSecond;
    }

    public void setTotalNumberOfRequestsPerSecond(int totalNumberOfRequestsPerSecond) {
        this.totalNumberOfRequestsPerSecond = totalNumberOfRequestsPerSecond;
    }

    public int getTotalWindowForRateLimitInMilliseconds() {
        return totalWindowForRateLimitInMilliseconds;
    }

    public void setTotalWindowForRateLimitInMilliseconds(int totalWindowForRateLimitInMilliseconds) {
        this.totalWindowForRateLimitInMilliseconds = totalWindowForRateLimitInMilliseconds;
    }

    public int getTotalMaxBucketSize() {
        return totalMaxBucketSize;
    }

    public void setTotalMaxBucketSize(int totalMaxBucketSize) {
        this.totalMaxBucketSize = totalMaxBucketSize;
    }

    public int getNumberOfRequestsPerSecondPerIpAddress() {
        return numberOfRequestsPerSecondPerIpAddress;
    }

    public void setNumberOfRequestsPerSecondPerIpAddress(int numberOfRequestsPerSecondPerIpAddress) {
        this.numberOfRequestsPerSecondPerIpAddress = numberOfRequestsPerSecondPerIpAddress;
    }

    public int getWindowForRateLimitInMillisecondsPerIpAddress() {
        return windowForRateLimitInMillisecondsPerIpAddress;
    }

    public void setWindowForRateLimitInMillisecondsPerIpAddress(int windowForRateLimitInMillisecondsPerIpAddress) {
        this.windowForRateLimitInMillisecondsPerIpAddress = windowForRateLimitInMillisecondsPerIpAddress;
    }

    public int getMaxBucketSizePerIpAddress() {
        return maxBucketSizePerIpAddress;
    }

    public void setMaxBucketSizePerIpAddress(int maxBucketSizePerIpAddress) {
        this.maxBucketSizePerIpAddress = maxBucketSizePerIpAddress;
    }
}
