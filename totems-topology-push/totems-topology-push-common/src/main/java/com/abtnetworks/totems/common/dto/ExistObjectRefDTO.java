package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 类似ExistsObjectDTO，当前仅思科8.6版本特殊处理
 * @author luwei
 * @date 2020/7/21
 */
@Data
@ApiModel("思科8.6特殊引用")
public class ExistObjectRefDTO {

    @ApiModelProperty("现有源地址对象（组）名称")
    RefObjectDTO srcAddressObjectName;

    @ApiModelProperty("转换后地址对象（组）名称")
    RefObjectDTO postSrcAddressObjectName;

    @ApiModelProperty("现有目的地址对象（组）名称")
    RefObjectDTO dstAddressObjectName;

    @ApiModelProperty("转换后目的地址对象（组）名称")
    RefObjectDTO postDstAddressObjectName;

    @ApiModelProperty("现有服务对象（组）名称")
    RefObjectDTO serviceObjectName;

    @ApiModelProperty("转换后服务对象（组）名称")
    RefObjectDTO postServiceObjectName;

    @ApiModelProperty("已存在源地址对象列表")
    List<RefObjectDTO> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后源地址对象列表")
    List<RefObjectDTO> existPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后地址对象列表")
    List<String> restPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<RefObjectDTO> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建目的地址对象列表")
    List<String> restDstAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后目的地址对象列表")
    List<RefObjectDTO> existPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后目的地址对象列表")
    List<String> restPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<RefObjectDTO> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();

    @ApiModelProperty("已存在转换后服务名称列表")
    List<RefObjectDTO> existPostServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建转换后的服务列表")
    List<ServiceDTO> restPostServiceList = new ArrayList<>();


}
