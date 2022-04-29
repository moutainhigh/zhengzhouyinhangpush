package com.abtnetworks.totems.issued.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
@Data
public class PushCommandRegularParamDTO implements Serializable {
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
    /**
     * 正则
     */
    private GlobAndRegexElementDTO linuxPromptRegEx;



    @Override
    public String toString() {
        return super.toString();
    }
}
