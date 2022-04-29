package com.abtnetworks.totems.commandLine.controller;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.commandLine.utils.ParamVOToDTOUtil;
import com.abtnetworks.totems.commandLine.vo.*;
import com.abtnetworks.totems.common.TotemsReturnT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: WangCan
 * @Description 对象原子化命令行接口
 * @Date: 2021/8/10
 */
@Slf4j
@Api(tags = {"原子化命令行>>对象"})
@RestController
@RequestMapping(value = "${startPath}/commandLine/object")
public class ObjectCommandLineController {

    @ApiOperation("生成时间对象命令行")
    @PostMapping("generateTimeCommandLine")
    public TotemsReturnT generateTimeCommandLine(@RequestBody TimeCommandLineParamVO timeCommandLineParamVO) {
        if (timeCommandLineParamVO == null || StringUtils.isBlank(timeCommandLineParamVO.getModelNumber()) ) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(timeCommandLineParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String timeObjectCommandLine = null;
        if(timeCommandLineParamVO.getTimeObjectType() == 0){
            try {
                timeObjectCommandLine = overAllGeneratorAbstractBean.generateAbsoluteTimeCommandLine(timeCommandLineParamVO.getName(),timeCommandLineParamVO.getAttachStr(),
                        new AbsoluteTimeParamDTO(timeCommandLineParamVO.getStartTime(),timeCommandLineParamVO.getEndTime()),timeCommandLineParamVO.getMap(),timeCommandLineParamVO.getArgs());
            } catch (Exception e) {
                log.error("时间对象命令行生成异常:",e);
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
            }
        } else {
            try {
                timeObjectCommandLine = overAllGeneratorAbstractBean.generatePeriodicTimeCommandLine(timeCommandLineParamVO.getName(),timeCommandLineParamVO.getAttachStr(),
                        new PeriodicTimeParamDTO(timeCommandLineParamVO.getCycle(),timeCommandLineParamVO.getCycleStart(),timeCommandLineParamVO.getCycleEnd()),timeCommandLineParamVO.getMap(),timeCommandLineParamVO.getArgs());
            } catch (Exception e) {
                log.error("时间对象命令行生成异常:",e);
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
            }
        }

        return new TotemsReturnT(timeObjectCommandLine);
    }

    @ApiOperation("生成删除时间对象命令行")
    @PostMapping("deleteTimeObjectCommandLine")
    public TotemsReturnT deleteTimeObjectCommandLine(@RequestBody DeleteTimeCommandLineParamVO deleteTimeCommandLineParamVO){
        if (deleteTimeCommandLineParamVO == null || StringUtils.isBlank(deleteTimeCommandLineParamVO.getModelNumber()) ) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteTimeCommandLineParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String deleteTimeObjectCommandLine = null;
        if(deleteTimeCommandLineParamVO.getTimeObjectType() != null && deleteTimeCommandLineParamVO.getTimeObjectType() == 1){
            try {
                deleteTimeObjectCommandLine = overAllGeneratorAbstractBean.deletePeriodicTimeCommandLine(deleteTimeCommandLineParamVO.getName(),deleteTimeCommandLineParamVO.getMap(),deleteTimeCommandLineParamVO.getArgs());
            } catch (Exception e) {
                log.error("删除时间对象命令行生成异常:",e);
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
            }
        } else {
            try {
                deleteTimeObjectCommandLine = overAllGeneratorAbstractBean.deleteAbsoluteTimeCommandLine(deleteTimeCommandLineParamVO.getName(),deleteTimeCommandLineParamVO.getMap(),deleteTimeCommandLineParamVO.getArgs());
            } catch (Exception e) {
                log.error("删除时间对象命令行生成异常:",e);
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
            }
        }

        return new TotemsReturnT(deleteTimeObjectCommandLine);
    }

    @ApiOperation("生成地址对象命令行")
    @PostMapping("generateIpAddressObjectCommandLine")
    public TotemsReturnT generateIpAddressObjectCommandLine(@RequestBody IpAddressObjectParamVO ipAddressObjectParamVO) {
        if (ipAddressObjectParamVO == null || StringUtils.isBlank(ipAddressObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(ipAddressObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String ipAddressObjectCommandLine = null;
        try {
            List<IpAddressRangeDTO> rangeDTOList = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getRangeIpArray())){
                for (IpAddressRangeVO ipAddressRangeVO : ipAddressObjectParamVO.getRangeIpArray()) {
                    rangeDTOList.add(ParamVOToDTOUtil.ipAddressRangeVOToDTO(ipAddressRangeVO));
                }
            }

            List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getSubnetIntIpArray())){
                for (IpAddressSubnetIntVO ipAddressSubnetIntVO : ipAddressObjectParamVO.getSubnetIntIpArray()) {
                    subnetIntDTOS.add(ParamVOToDTOUtil.ipAddressSubnetIntVOToDTO(ipAddressSubnetIntVO));
                }
            }

            List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getSubnetStrIpArray())){
                for (IpAddressSubnetStrVO ipAddressSubnetStrVO : ipAddressObjectParamVO.getSubnetStrIpArray()) {
                    subnetStrDTOS.add(ParamVOToDTOUtil.ipAddressSubnetStrVOToDTO(ipAddressSubnetStrVO));
                }
            }
            ipAddressObjectCommandLine = overAllGeneratorAbstractBean.generateIpAddressObjectCommandLine(StatusTypeEnum.getByCode(String.valueOf(ipAddressObjectParamVO.getStatusTypeEnumCode())), RuleIPTypeEnum.getByCode(String.valueOf(ipAddressObjectParamVO.getRuleIpTypeEnumCode())),
                    ipAddressObjectParamVO.getName(), ipAddressObjectParamVO.getId(),
                    ipAddressObjectParamVO.getSingleIpArray(), rangeDTOList.toArray(new IpAddressRangeDTO[0]),subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[0]),subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[0]),
                    ipAddressObjectParamVO.getInterfaceArray(), ipAddressObjectParamVO.getFqdnArray(), ipAddressObjectParamVO.getObjectNameRefArray(),
                    ipAddressObjectParamVO.getDescription(), ipAddressObjectParamVO.getAttachStr(), ipAddressObjectParamVO.getDelStr(),
                    ipAddressObjectParamVO.getMap(), ipAddressObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("地址对象对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(ipAddressObjectCommandLine);
    }

    @ApiOperation("生成删除地址对象命令行")
    @PostMapping("deleteIpAddressObjectCommandLine")
    public TotemsReturnT deleteIpAddressObjectCommandLine(@RequestBody DeleteIpAddressObjectParamVO deleteIpAddressObjectParamVO) {
        if (deleteIpAddressObjectParamVO == null || StringUtils.isBlank(deleteIpAddressObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteIpAddressObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String deleteIpAddressObjectCommandLine = null;
        try {
            deleteIpAddressObjectCommandLine = overAllGeneratorAbstractBean.deleteIpAddressObjectCommandLine(RuleIPTypeEnum.getByCode(String.valueOf(deleteIpAddressObjectParamVO.getRuleIpTypeEnumCode())),
                    deleteIpAddressObjectParamVO.getDelStr(),deleteIpAddressObjectParamVO.getName(),
                    deleteIpAddressObjectParamVO.getMap(), deleteIpAddressObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("删除地址对象对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(deleteIpAddressObjectCommandLine);
    }

    @ApiOperation("生成地址组对象命令行")
    @PostMapping("generateIpAddressObjectGroupCommandLine")
    public TotemsReturnT generateIpAddressObjectGroupCommandLine(@RequestBody IpAddressObjectParamVO ipAddressObjectParamVO) {
        if (ipAddressObjectParamVO == null || StringUtils.isBlank(ipAddressObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(ipAddressObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String ipAddressObjectGroupCommandLine = null;
        try {
            List<IpAddressRangeDTO> rangeDTOList = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getRangeIpArray())){
                for (IpAddressRangeVO ipAddressRangeVO : ipAddressObjectParamVO.getRangeIpArray()) {
                    rangeDTOList.add(ParamVOToDTOUtil.ipAddressRangeVOToDTO(ipAddressRangeVO));
                }
            }

            List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getSubnetIntIpArray())){
                for (IpAddressSubnetIntVO ipAddressSubnetIntVO : ipAddressObjectParamVO.getSubnetIntIpArray()) {
                    subnetIntDTOS.add(ParamVOToDTOUtil.ipAddressSubnetIntVOToDTO(ipAddressSubnetIntVO));
                }
            }

            List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(ipAddressObjectParamVO.getSubnetStrIpArray())){
                for (IpAddressSubnetStrVO ipAddressSubnetStrVO : ipAddressObjectParamVO.getSubnetStrIpArray()) {
                    subnetStrDTOS.add(ParamVOToDTOUtil.ipAddressSubnetStrVOToDTO(ipAddressSubnetStrVO));
                }
            }
            ipAddressObjectGroupCommandLine = overAllGeneratorAbstractBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.getByCode(String.valueOf(ipAddressObjectParamVO.getStatusTypeEnumCode())), RuleIPTypeEnum.getByCode(String.valueOf(ipAddressObjectParamVO.getRuleIpTypeEnumCode())),
                    ipAddressObjectParamVO.getName(), ipAddressObjectParamVO.getId(),
                    ipAddressObjectParamVO.getSingleIpArray(), rangeDTOList.toArray(new IpAddressRangeDTO[0]),subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[0]),subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[0]),
                    ipAddressObjectParamVO.getInterfaceArray(), ipAddressObjectParamVO.getFqdnArray(), ipAddressObjectParamVO.getObjectNameRefArray(),ipAddressObjectParamVO.getObjectGroupNameRefArray(),
                    ipAddressObjectParamVO.getDescription(), ipAddressObjectParamVO.getAttachStr(), ipAddressObjectParamVO.getDelStr(),
                    ipAddressObjectParamVO.getMap(), ipAddressObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("地址组对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(ipAddressObjectGroupCommandLine);
    }

    @ApiOperation("生成删除地址组对象命令行")
    @PostMapping("deleteIpAddressObjectGroupCommandLine")
    public TotemsReturnT deleteIpAddressObjectGroupCommandLine(@RequestBody DeleteIpAddressObjectParamVO deleteIpAddressObjectParamVO) {
        if (deleteIpAddressObjectParamVO == null || StringUtils.isBlank(deleteIpAddressObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteIpAddressObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String deleteIpAddressObjectGroupCommandLine = null;
        try {
            deleteIpAddressObjectGroupCommandLine = overAllGeneratorAbstractBean.deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum.getByCode(String.valueOf(deleteIpAddressObjectParamVO.getRuleIpTypeEnumCode())),
                     deleteIpAddressObjectParamVO.getDelStr(),deleteIpAddressObjectParamVO.getName(),
                    deleteIpAddressObjectParamVO.getMap(), deleteIpAddressObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("删除地址组对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(deleteIpAddressObjectGroupCommandLine);
    }

    @ApiOperation("生成服务对象命令行")
    @PostMapping("generateServiceObjectCommandLine")
    public TotemsReturnT generateServiceObjectCommandLine(@RequestBody ServiceObjectParamVO serviceObjectParamVO) {
        if (serviceObjectParamVO == null || StringUtils.isBlank(serviceObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(serviceObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String serviceOjectCommandLine = null;
        try {
            List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(serviceObjectParamVO.getServiceParamVOList())){
                for (ServiceParamVO serviceParamVO : serviceObjectParamVO.getServiceParamVOList()) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    BeanUtils.copyProperties(serviceParamVO,serviceParamDTO);
                    serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(serviceParamVO.getProtocolTypeEnumCode()));
                    if(ArrayUtils.isNotEmpty(serviceParamVO.getSrcRangePortVoArray())) {
                        PortRangeVO[] srcRangePortVoArray = serviceParamVO.getSrcRangePortVoArray();
                        PortRangeDTO[] srcRangePortArray = new PortRangeDTO[srcRangePortVoArray.length];
                        for (int i = 0; i < srcRangePortVoArray.length; i++) {
                            srcRangePortArray[i] = new PortRangeDTO(srcRangePortVoArray[i].getStart(),srcRangePortVoArray[i].getEnd());
                        }
                        serviceParamDTO.setSrcRangePortArray(srcRangePortArray);
                    }
                    if(ArrayUtils.isNotEmpty(serviceParamVO.getDstRangePortVoArray())) {
                        PortRangeVO[] dstRangePortVoArray = serviceParamVO.getDstRangePortVoArray();
                        PortRangeDTO[] dstRangePortArray = new PortRangeDTO[dstRangePortVoArray.length];
                        for (int i = 0; i < dstRangePortVoArray.length; i++) {
                            dstRangePortArray[i] = new PortRangeDTO(dstRangePortVoArray[i].getStart(),dstRangePortVoArray[i].getEnd());
                        }
                        serviceParamDTO.setDstRangePortArray(dstRangePortArray);
                    }
                    serviceParamDTOList.add(serviceParamDTO);
                }
            }
            serviceOjectCommandLine = overAllGeneratorAbstractBean.generateServiceObjectCommandLine(StatusTypeEnum.getByCode(String.valueOf(serviceObjectParamVO.getStatusTypeEnumCode())),
                    serviceObjectParamVO.getName(), serviceObjectParamVO.getId(),serviceObjectParamVO.getAttachStr(),serviceParamDTOList,
                    serviceObjectParamVO.getDescription(), serviceObjectParamVO.getMap(), serviceObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("服务对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(serviceOjectCommandLine);
    }

    @ApiOperation("生成删除服务对象命令行")
    @PostMapping("deleteServiceObjectCommandLine")
    public TotemsReturnT deleteServiceObjectCommandLine(@RequestBody DeleteServiceObjectParamVO deleteServiceObjectParamVO) {
        if (deleteServiceObjectParamVO == null || StringUtils.isBlank(deleteServiceObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteServiceObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String deleteServiceOjectCommandLine = null;
        try {
            deleteServiceOjectCommandLine = overAllGeneratorAbstractBean.deleteServiceObjectCommandLine(deleteServiceObjectParamVO.getDelStr(),deleteServiceObjectParamVO.getAttachStr(),
                    deleteServiceObjectParamVO.getName(), deleteServiceObjectParamVO.getMap(), deleteServiceObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("删除服务对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(deleteServiceOjectCommandLine);
    }

    @ApiOperation("生成服务组对象命令行")
    @PostMapping("generateServiceObjectGroupCommandLine")
    public TotemsReturnT generateServiceObjectGroupCommandLine(@RequestBody ServiceObjectParamVO serviceObjectParamVO) {
        if (serviceObjectParamVO == null || StringUtils.isBlank(serviceObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(serviceObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String serviceOjectGroupCommandLine = null;
        try {
            List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(serviceObjectParamVO.getServiceParamVOList())){
                for (ServiceParamVO serviceParamVO : serviceObjectParamVO.getServiceParamVOList()) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    BeanUtils.copyProperties(serviceParamVO,serviceParamDTO);
                    serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(serviceParamVO.getProtocolTypeEnumCode()));
                    if(ArrayUtils.isNotEmpty(serviceParamVO.getSrcRangePortVoArray())) {
                        PortRangeVO[] srcRangePortVoArray = serviceParamVO.getSrcRangePortVoArray();
                        PortRangeDTO[] srcRangePortArray = new PortRangeDTO[srcRangePortVoArray.length];
                        for (int i = 0; i < srcRangePortVoArray.length; i++) {
                            srcRangePortArray[i] = new PortRangeDTO(srcRangePortVoArray[i].getStart(),srcRangePortVoArray[i].getEnd());
                        }
                        serviceParamDTO.setSrcRangePortArray(srcRangePortArray);
                    }
                    if(ArrayUtils.isNotEmpty(serviceParamVO.getDstRangePortVoArray())) {
                        PortRangeVO[] dstRangePortVoArray = serviceParamVO.getDstRangePortVoArray();
                        PortRangeDTO[] dstRangePortArray = new PortRangeDTO[dstRangePortVoArray.length];
                        for (int i = 0; i < dstRangePortVoArray.length; i++) {
                            dstRangePortArray[i] = new PortRangeDTO(dstRangePortVoArray[i].getStart(),dstRangePortVoArray[i].getEnd());
                        }
                        serviceParamDTO.setDstRangePortArray(dstRangePortArray);
                    }
                    serviceParamDTOList.add(serviceParamDTO);
                }
            }

            serviceOjectGroupCommandLine = overAllGeneratorAbstractBean.generateServiceObjectGroupCommandLine(StatusTypeEnum.getByCode(String.valueOf(serviceObjectParamVO.getStatusTypeEnumCode())),
                    serviceObjectParamVO.getName(), serviceObjectParamVO.getId(),serviceObjectParamVO.getAttachStr(),serviceParamDTOList,
                    serviceObjectParamVO.getDescription(),serviceObjectParamVO.getServiceObjectNameRefArray(),serviceObjectParamVO.getServiceObjectGroupNameRefArray(),
                    serviceObjectParamVO.getMap(), serviceObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("服务组对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(serviceOjectGroupCommandLine);
    }

    @ApiOperation("生成删除服务组对象命令行")
    @PostMapping("deleteServiceObjectGroupCommandLine")
    public TotemsReturnT deleteServiceObjectGroupCommandLine(@RequestBody DeleteServiceObjectParamVO deleteServiceObjectParamVO) {
        if (deleteServiceObjectParamVO == null || StringUtils.isBlank(deleteServiceObjectParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteServiceObjectParamVO.getModelNumber());
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        String deleteServiceOjectCommandLine = null;
        try {
            deleteServiceOjectCommandLine = overAllGeneratorAbstractBean.deleteServiceObjectGroupCommandLine(deleteServiceObjectParamVO.getDelStr(),deleteServiceObjectParamVO.getAttachStr(),
                    deleteServiceObjectParamVO.getName(), deleteServiceObjectParamVO.getMap(), deleteServiceObjectParamVO.getArgs());
        } catch (Exception e) {
            log.error("删除服务组对象命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(deleteServiceOjectCommandLine);
    }
}
