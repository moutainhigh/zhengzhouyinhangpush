package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseDto;

import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 10:01 2019/11/28
 */
public class DisposalNodeCommandLineRecordDTO extends BaseDto {

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * push_command_task_editable表的主键id
     */
    private Integer taskEditableId;

    /**
     * 工单UUID
     */
    private String centerUuid;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 0封堵，1解封，2回滚
     */
    private Integer type;

    /**
     * 源ip
     */
    private String srcIp;

    /**
     * 目的ip
     */
    private String dstIp;

    /**
     * ip类型（0：ipv4，1：ipv6）
     */
    private Integer ipType;

    /**
     * 服务json
     */
    private String serviceList;

    /**
     * 黑洞路由ip
     */
    private String routingIp;

    /**
     * 是否虚墙
     */
    private Integer vsys;

    /**
     * 虚墙的主设备UUID
     */
    private String pDeviceUuid;

    /**
     * 管理IP
     */
    private String deviceIp;

    /**
     * 节点类型
     */
    private String deviceType;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 厂商名称
     */
    private String vendorName;

    /**
     * 厂商id 名称标识
     */
    private String vendorId;

    /**
     * 设备来源：1手工导入2采集
     */
    private Boolean origin;

    /**
     * 状态：0无状态，1成功，2失败，-1采集中，-2解析中
     */
    private Boolean state;

    /**
     * 型号
     */
    private String modelNumber;

    /**
     * 任务id
     */
    private Integer taskId;

    /**
     * 主题（工单号）
     */
    private String theme;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 修改用户
     */
    private String editUserName;

    /**
     * 任务类型
     */
    private Boolean taskType;

    /**
     * 设备UUID
     */
    private String deviceUuid;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 下发时间
     */
    private Date pushTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    /**
     * 是否自动下发
     */
    private Boolean autoPush;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 下发结果
     */
    private String pushResult;

    /**
     * 命令行
     */
    private Object commandline;

    /**
     * 命令行回滚命令行
     */
    private Object commandlineRevert;

    /**
     * 命令行下发结果
     */
    private Object commandlineEcho;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getTaskEditableId() {
        return taskEditableId;
    }

    public void setTaskEditableId(Integer taskEditableId) {
        this.taskEditableId = taskEditableId;
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public Integer getIpType() {
        return ipType;
    }

    public void setIpType(Integer ipType) {
        this.ipType = ipType;
    }

    public String getServiceList() {
        return serviceList;
    }

    public void setServiceList(String serviceList) {
        this.serviceList = serviceList;
    }

    public String getRoutingIp() {
        return routingIp;
    }

    public void setRoutingIp(String routingIp) {
        this.routingIp = routingIp;
    }

    public Integer getVsys() {
        return vsys;
    }

    public void setVsys(Integer vsys) {
        this.vsys = vsys;
    }

    public String getpDeviceUuid() {
        return pDeviceUuid;
    }

    public void setpDeviceUuid(String pDeviceUuid) {
        this.pDeviceUuid = pDeviceUuid;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
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

    public Boolean getOrigin() {
        return origin;
    }

    public void setOrigin(Boolean origin) {
        this.origin = origin;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEditUserName() {
        return editUserName;
    }

    public void setEditUserName(String editUserName) {
        this.editUserName = editUserName;
    }

    public Boolean getTaskType() {
        return taskType;
    }

    public void setTaskType(Boolean taskType) {
        this.taskType = taskType;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Boolean getAutoPush() {
        return autoPush;
    }

    public void setAutoPush(Boolean autoPush) {
        this.autoPush = autoPush;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPushResult() {
        return pushResult;
    }

    public void setPushResult(String pushResult) {
        this.pushResult = pushResult;
    }

    public Object getCommandline() {
        return commandline;
    }

    public void setCommandline(Object commandline) {
        this.commandline = commandline;
    }

    public Object getCommandlineRevert() {
        return commandlineRevert;
    }

    public void setCommandlineRevert(Object commandlineRevert) {
        this.commandlineRevert = commandlineRevert;
    }

    public Object getCommandlineEcho() {
        return commandlineEcho;
    }

    public void setCommandlineEcho(Object commandlineEcho) {
        this.commandlineEcho = commandlineEcho;
    }
}
