package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("已存在对象数据")
public class ExistObjectDTO {

    @ApiModelProperty("现有源地址对象（组）名称")
    String srcAddressObjectName;

    @ApiModelProperty("转换后地址对象（组）名称")
    String postSrcAddressObjectName;

    @ApiModelProperty("现有目的地址对象（组）名称")
    String dstAddressObjectName;

    @ApiModelProperty("转换后目的地址对象（组）名称")
    String postDstAddressObjectName;

    @ApiModelProperty("现有服务对象（组）名称")
    String serviceObjectName;

    @ApiModelProperty("转换后服务对象（组）名称")
    String postServiceObjectName;

    @ApiModelProperty("已存在源地址对象列表")
    List<String> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后源地址对象列表")
    List<String> existPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后地址对象列表")
    List<String> restPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<String> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建目的地址对象列表")
    List<String> restDstAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后目的地址对象列表")
    List<String> existPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后目的地址对象列表")
    List<String> restPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<String> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();

    @ApiModelProperty("已存在转换后服务名称列表")
    List<String> existPostServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建转换后的服务列表")
    List<ServiceDTO> restPostServiceList = new ArrayList<>();
    @ApiModelProperty("已存在源地址对象列表")
    List<ExistAddressObjectDTO> existSrcAddressName;
    @ApiModelProperty("已存在目的地址对象列表")

    List<ExistAddressObjectDTO> existDstAddressName;
    @ApiModelProperty("已存在源转换后地址对象列表")
    List<ExistAddressObjectDTO> existPostSrcAddressName;
    @ApiModelProperty("已存在目的转换后地址对象列表")
    List<ExistAddressObjectDTO> existPostDstAddressName;




}
