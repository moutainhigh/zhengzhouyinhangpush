package com.abtnetworks.totems.issued.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 请写类注释
 */
public class AddCommandRegularParamDTO {

    /**
     * 型号
     */
    @Max(value = 20, message = "型号长度需要小于20")
    @NotBlank(message = "参数不能为空")
    private String modelNumber;
    /**
     * 命令正则匹配输出
     */
    @Max(value = 255, message = "命令终止符长度需要小于255")
    @NotBlank(message = "终止符命令参数不能为空")
    private String promptRegCommand;
    /**
     * 提示正则错误命令行
     */
    @Max(value = 6144, message = "提示正则错误匹配命令长度要小于6144")
    private String promptErrorInfo;

    /**
     * 厂商名称
     */
    @Max(value = 255, message = "厂商名称长度需要小于255")
    @NotBlank(message = "厂商名称不能为空")
    private String vendorName;
    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    private String type;
    /**
     * 超时时间
     **/
    private Integer timeOut;

    private String codeName;

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

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
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

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setType(String type) {
        this.type = type;
    }
}
