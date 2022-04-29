package com.abtnetworks.totems.disposal.dto;

/**
 * @Author hw
 * @Description
 * @Date 15:44 2019/11/21
 */
public class DisposalNodeCredentialDTO {
    
    private String deviceUuid;
    private String deviceManagerIp;
    private String deviceType;
    private String deviceName;
    private String credentialUuid;
    private String pushCredentialUuid;
    private String vendorName;
    private String vendorId;
    private String credentialName;
    private String username;
    private String password;
    private String enableUserName;
    private String enablePassword;
    private Integer port;
    private String controllerId;

    public DisposalNodeCredentialDTO() {
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getDeviceManagerIp() {
        return deviceManagerIp;
    }

    public void setDeviceManagerIp(String deviceManagerIp) {
        this.deviceManagerIp = deviceManagerIp;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCredentialUuid() {
        return credentialUuid;
    }

    public void setCredentialUuid(String credentialUuid) {
        this.credentialUuid = credentialUuid;
    }

    public String getPushCredentialUuid() {
        return pushCredentialUuid;
    }

    public void setPushCredentialUuid(String pushCredentialUuid) {
        this.pushCredentialUuid = pushCredentialUuid;
    }



    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEnableUserName() {
        return enableUserName;
    }

    public void setEnableUserName(String enableUserName) {
        this.enableUserName = enableUserName;
    }

    public String getEnablePassword() {
        return enablePassword;
    }

    public void setEnablePassword(String enablePassword) {
        this.enablePassword = enablePassword;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
}
