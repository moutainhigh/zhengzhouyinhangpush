package com.abtnetworks.totems.issued.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
public class CommandRegularUpdateDTO implements Serializable {

    private static final long serialVersionUID = -4001208147603432789L;
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
    @Max(value = 255, message = "命令终止符长度需小于255")
    @NotBlank(message = "终止符命令参数不能为空")
    private String promptRegCommand;
    /**
     * 提示正则错误命令行
     */
    @Max(value = 6144, message = "提示正则错误匹配命令长度需小于6144")
    private String promptErrorInfo;
    /**
     * 修改人
     */
    private String updateEmp;

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
