package com.abtnetworks.totems.issued.business.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
public class PushCommandRegularParamEntity implements Serializable {
    private static final long serialVersionUID = -3279965250964934711L;
    /**
     * 主键
     */
    private Integer id;
    /**
     * 型号
     */
    private String modelNumber;
    /**
     * 命令正则匹配输出
     */
    private String promptRegCommand;
    /**
     * 提示正则错误命令行
     */
    private String promptErrorInfo;
    /**
     * 创建用户
     */
    private String createEmp;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 提示正则错误命令行
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 提示正则错误命令行
     */
    private String updateEmp;
    /**
     * 厂商名称
     */

    private String vendorName;
    /**
     * 设备类型
     */

    private String type;
    /**
     * 超时时间
     **/
    private Integer timeOut;


    /**
     * 提示支持正则错误命令行
     */
    private String promptErrorRegInfo;
    /**
     * 提示支持正则命令行
     */
    private String promptRegExCommand;
    /**
     * 间隔时间
     */
    private Integer intervalTime;

    public String getPromptErrorRegInfo() {
        return promptErrorRegInfo;
    }

    public void setPromptErrorRegInfo(String promptErrorRegInfo) {
        this.promptErrorRegInfo = promptErrorRegInfo;
    }

    public String getPromptRegExCommand() {
        return promptRegExCommand;
    }

    public void setPromptRegExCommand(String promptRegExCommand) {
        this.promptRegExCommand = promptRegExCommand;
    }

    public Integer getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
        this.intervalTime = intervalTime;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getPromptRegCommand() {
        return promptRegCommand;
    }

    public void setPromptRegCommand(String promptRegCommand) {
        this.promptRegCommand = promptRegCommand;
    }

    public String getPromptErrorInfo() {
        return promptErrorInfo;
    }

    public void setPromptErrorInfo(String promptErrorInfo) {
        this.promptErrorInfo = promptErrorInfo;
    }

    public String getCreateEmp() {
        return createEmp;
    }

    public void setCreateEmp(String createEmp) {
        this.createEmp = createEmp;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateEmp() {
        return updateEmp;
    }

    public void setUpdateEmp(String updateEmp) {
        this.updateEmp = updateEmp;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
