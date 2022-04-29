package com.abtnetworks.totems.commandLine.controller;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.commandLine.utils.ParamVOToDTOUtil;
import com.abtnetworks.totems.commandLine.vo.*;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.mapper.TotemsJsonMapper;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@Slf4j
@Api(tags = {"原子化命令行>>策略"})
@RestController
@RequestMapping(value = "${startPath}/commandLine/policy")
public class PolicyCommandLineController {

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @ApiOperation("生成安全策略")
    @PostMapping("generateSecurityPolicyCommandLine")
    public TotemsReturnT generateSecurityPolicyCommandLine(@RequestBody SecurityPolicyParamVO securityPolicyParamVO) {
        if (securityPolicyParamVO == null || StringUtils.isBlank(securityPolicyParamVO.getModelNumber()) ) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(securityPolicyParamVO.getModelNumber());
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
        String securityPolicyCommandLine = null;
        try {
            IpAddressParamDTO srcIp = null;
            if(securityPolicyParamVO.getSrcIp() != null){
                IpAddressObjectParamVO srcIpVO = securityPolicyParamVO.getSrcIp();
                srcIp = new IpAddressParamDTO();
                srcIp.setSingleIpArray(srcIpVO.getSingleIpArray());

                if ( null != srcIpVO.getRuleIpTypeEnumCode()  ){
                    srcIp.setIpTypeEnum(  RuleIPTypeEnum.getByCode( srcIpVO.getRuleIpTypeEnumCode().toString() )  );
                }

                List<IpAddressRangeDTO> rangeDTOList = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(srcIpVO.getRangeIpArray())){
                    for (IpAddressRangeVO ipAddressRangeVO : srcIpVO.getRangeIpArray()) {
                        rangeDTOList.add(ParamVOToDTOUtil.ipAddressRangeVOToDTO(ipAddressRangeVO));
                    }
                }
                srcIp.setRangIpArray(rangeDTOList.toArray(new IpAddressRangeDTO[0]));

                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(srcIpVO.getSubnetIntIpArray())){
                    for (IpAddressSubnetIntVO ipAddressSubnetIntVO : srcIpVO.getSubnetIntIpArray()) {
                        subnetIntDTOS.add(ParamVOToDTOUtil.ipAddressSubnetIntVOToDTO(ipAddressSubnetIntVO));
                    }
                }
                srcIp.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[0]));

                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(srcIpVO.getSubnetStrIpArray())){
                    for (IpAddressSubnetStrVO ipAddressSubnetStrVO : srcIpVO.getSubnetStrIpArray()) {
                        subnetStrDTOS.add(ParamVOToDTOUtil.ipAddressSubnetStrVOToDTO(ipAddressSubnetStrVO));
                    }
                }
                srcIp.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[0]));
                srcIp.setHosts(srcIpVO.getFqdnArray());
                srcIp.setObjectNameRefArray(srcIpVO.getObjectNameRefArray());
                srcIp.setObjectGroupNameRefArray(srcIpVO.getObjectGroupNameRefArray());
            }

            IpAddressParamDTO dstIp = null;
            if(securityPolicyParamVO.getDstIp() != null){
                IpAddressObjectParamVO dstIpVO = securityPolicyParamVO.getDstIp();
                dstIp = new IpAddressParamDTO();
                dstIp.setSingleIpArray(dstIpVO.getSingleIpArray());

                if ( null != dstIpVO.getRuleIpTypeEnumCode()  ){
                    dstIp.setIpTypeEnum(  RuleIPTypeEnum.getByCode( dstIpVO.getRuleIpTypeEnumCode().toString() )  );
                }

                List<IpAddressRangeDTO> rangeDTOList = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(dstIpVO.getRangeIpArray())){
                    for (IpAddressRangeVO ipAddressRangeVO : dstIpVO.getRangeIpArray()) {
                        rangeDTOList.add(ParamVOToDTOUtil.ipAddressRangeVOToDTO(ipAddressRangeVO));
                    }
                }
                dstIp.setRangIpArray(rangeDTOList.toArray(new IpAddressRangeDTO[0]));

                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(dstIpVO.getSubnetIntIpArray())){
                    for (IpAddressSubnetIntVO ipAddressSubnetIntVO : dstIpVO.getSubnetIntIpArray()) {
                        subnetIntDTOS.add(ParamVOToDTOUtil.ipAddressSubnetIntVOToDTO(ipAddressSubnetIntVO));
                    }
                }
                dstIp.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[0]));

                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                if(ArrayUtils.isNotEmpty(dstIpVO.getSubnetStrIpArray())){
                    for (IpAddressSubnetStrVO ipAddressSubnetStrVO : dstIpVO.getSubnetStrIpArray()) {
                        subnetStrDTOS.add(ParamVOToDTOUtil.ipAddressSubnetStrVOToDTO(ipAddressSubnetStrVO));
                    }
                }
                dstIp.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[0]));
                dstIp.setHosts(dstIpVO.getFqdnArray());
                dstIp.setObjectNameRefArray(dstIpVO.getObjectNameRefArray());
                dstIp.setObjectGroupNameRefArray(dstIpVO.getObjectGroupNameRefArray());
            }
            List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
            if(ArrayUtils.isNotEmpty(securityPolicyParamVO.getService())){
                for (ServiceParamVO serviceParamVO : securityPolicyParamVO.getService()) {
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

            AbsoluteTimeParamDTO absoluteTimeParamDTO = null;
            PeriodicTimeParamDTO periodicTimeParamDTO = null;
            if(securityPolicyParamVO.getTimeCommandLineParamVO() != null){
                TimeCommandLineParamVO timeCommandLineParamVO = securityPolicyParamVO.getTimeCommandLineParamVO();
                if(timeCommandLineParamVO.getTimeObjectType() == 0){
                    absoluteTimeParamDTO = new AbsoluteTimeParamDTO(timeCommandLineParamVO.getStartTime(),timeCommandLineParamVO.getEndTime());
                } else {
                    periodicTimeParamDTO = new PeriodicTimeParamDTO(timeCommandLineParamVO.getCycle(),timeCommandLineParamVO.getCycleStart(),timeCommandLineParamVO.getCycleEnd());
                }
            }

            ZoneParamDTO srcZone = null;
            ZoneParamDTO dstZone = null;
            if(ArrayUtils.isNotEmpty(securityPolicyParamVO.getSrcZoneArray())){
                srcZone = new ZoneParamDTO(securityPolicyParamVO.getSrcZoneArray()[0],securityPolicyParamVO.getSrcZoneArray()  );
            }
            if(ArrayUtils.isNotEmpty(securityPolicyParamVO.getDstZoneArray())){
                dstZone = new ZoneParamDTO(securityPolicyParamVO.getDstZoneArray()[0],securityPolicyParamVO.getDstZoneArray());
            }
            InterfaceParamDTO inInterface = null;
            InterfaceParamDTO outInterface = null;
            if (ArrayUtils.isNotEmpty(securityPolicyParamVO.getInInterfaceArray())) {
                inInterface = new InterfaceParamDTO(securityPolicyParamVO.getInInterfaceArray()[0]);
            }
            if (ArrayUtils.isNotEmpty(securityPolicyParamVO.getOutInterfaceArray())) {
                outInterface = new InterfaceParamDTO(securityPolicyParamVO.getOutInterfaceArray()[0]);
            }

            // 思科特殊处理 获取思科接口上策略集名称
            Map<String, Object> map = securityPolicyParamVO.getMap();
            if ( "Cisco ASA 9.9".equals( deviceModelNumberEnumExtended.getKey()) ){
                CmdDTO cmdDTO = new CmdDTO();
                if ( ObjectUtils.isNotEmpty( map.get("deviceUUid") ) ){
                    cmdDTO.getDevice().setDeviceUuid(map.get("deviceUUid").toString());
                }
                if ( ObjectUtils.isNotEmpty( map.get("srcItfAlias") )){
                    cmdDTO.getPolicy().setSrcItfAlias( map.get("srcItfAlias").toString() );
                }
                if ( ObjectUtils.isNotEmpty( map.get("dstItfAlias") ) ){
                    cmdDTO.getPolicy().setDstItfAlias( map.get("dstItfAlias").toString() );
                }
                SubServiceEnum subService = SubServiceEnum.valueOf(100);
                String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
                CmdService service = cmdServiceMap.get(serviceName);
                service.modify(cmdDTO);
                String policyName = cmdDTO.getSetting().getCiscoItfRuleListName();
                if ( StringUtils.isNotBlank( policyName ) ){
                    securityPolicyParamVO.getMap().put( "policyName",policyName );
                    securityPolicyParamVO.getMap().put( "ciscoInterfacePolicyName",policyName );
                }else if( ObjectUtils.isNotEmpty( map.get("name") ) ){
                    securityPolicyParamVO.getMap().put( "policyName", map.get("name") );
                    securityPolicyParamVO.getMap().put( "ciscoInterfacePolicyName", map.get("name") );
                }
            }


            log.info("安全策略命令行参数：{}", TotemsJsonMapper.toJson(securityPolicyParamVO));
            securityPolicyCommandLine = overAllGeneratorAbstractBean.generateSecurityPolicyCommandLine(StatusTypeEnum.getByCode(String.valueOf(securityPolicyParamVO.getStatusTypeEnumCode())),securityPolicyParamVO.getGroupName(),securityPolicyParamVO.getName(),
                    securityPolicyParamVO.getId(),securityPolicyParamVO.getAction(),securityPolicyParamVO.getDescription(),securityPolicyParamVO.getLogFlag(),securityPolicyParamVO.getAgeingTime(),securityPolicyParamVO.getRefVirusLibrary(),
                    MoveSeatEnum.getByCode(securityPolicyParamVO.getMoveSeatEnumCode()),securityPolicyParamVO.getSwapRuleNameId(),
                    srcIp,dstIp,serviceParamDTOList.toArray(new ServiceParamDTO[0]),absoluteTimeParamDTO,periodicTimeParamDTO,
                    srcZone,dstZone,inInterface,outInterface,
                    securityPolicyParamVO.getSrcRefIpAddressObject(),securityPolicyParamVO.getSrcRefIpAddressObjectGroup(),
                    securityPolicyParamVO.getDstRefIpAddressObject(),securityPolicyParamVO.getDstRefIpAddressObjectGroup(),
                    securityPolicyParamVO.getRefServiceObject(),securityPolicyParamVO.getRefServiceObjectGroup(),
                    securityPolicyParamVO.getRefTimeObject(),securityPolicyParamVO.getMap(),securityPolicyParamVO.getArgs());

        } catch (Exception e) {
            log.error("安全策略命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(securityPolicyCommandLine);
    }

    @ApiOperation("删除安全策略")
    @PostMapping("deleteSecurityPolicyByIdOrName")
    public TotemsReturnT deleteSecurityPolicyByIdOrName(@RequestBody DeleteSecurityPolicyParamVO deleteSecurityPolicyParamVO){
        if (deleteSecurityPolicyParamVO == null || StringUtils.isBlank(deleteSecurityPolicyParamVO.getModelNumber()) ) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(deleteSecurityPolicyParamVO.getModelNumber());
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
        String deleteSecurityPolicyCommandLine = null;
        try {
            deleteSecurityPolicyCommandLine = overAllGeneratorAbstractBean.deleteSecurityPolicyByIdOrName(RuleIPTypeEnum.getByCode(String.valueOf(deleteSecurityPolicyParamVO.getRuleIPTypeEnumCode())),
                    deleteSecurityPolicyParamVO.getId(),deleteSecurityPolicyParamVO.getName(),
                    deleteSecurityPolicyParamVO.getMap(),deleteSecurityPolicyParamVO.getArgs());
        } catch (Exception e) {
            log.error("删除安全策略命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }
        return new TotemsReturnT(deleteSecurityPolicyCommandLine);
    }
}
