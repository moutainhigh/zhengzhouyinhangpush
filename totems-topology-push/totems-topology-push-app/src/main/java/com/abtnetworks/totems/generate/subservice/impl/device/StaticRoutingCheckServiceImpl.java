package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineBusinessInfoDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.whale.baseapi.dto.BatchDeviceDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaticRoutingCheckServiceImpl implements CmdService {

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Resource
    NodeMapper nodeMapper;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        StringBuilder sb = new StringBuilder();
        CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO = cmdDto.getCommandLineStaticRoutingInfoDTO();
        CommandLineBusinessInfoDTO businessInfoDTO = cmdDto.getBusinessInfoDTO();

        switch (cmdDto.getDevice().getModelNumber()) {
            case CISCO_IOS:
            case WESTONE:
                getErrorMessage(staticRoutingInfoDTO, sb);
                getErrorMessage1(staticRoutingInfoDTO, sb);
                break;
            case CISCO_ASA_99:
                getErrorMessage6(staticRoutingInfoDTO, sb);
                getErrorMessage7(staticRoutingInfoDTO, sb);
                getErrorMessage12(staticRoutingInfoDTO, sb , 1, 255L);
                break;
            case SRX:
            case SRX_NoCli:
                getErrorMessage(staticRoutingInfoDTO, sb);
                getErrorMessage1(staticRoutingInfoDTO, sb);
                getErrorMessage5(cmdDto, staticRoutingInfoDTO, sb);
                break;
            case USG6000:
                getErrorMessage(staticRoutingInfoDTO, sb);
                getErrorMessage2(staticRoutingInfoDTO, sb);
                getErrorMessage3(staticRoutingInfoDTO, sb);
                getErrorMessage4(cmdDto, staticRoutingInfoDTO, sb);
                cmdDto.getPolicy().setDescription(AliStringUtils.filterChinese(cmdDto.getPolicy().getDescription()));
                getErrorMessage9(staticRoutingInfoDTO, sb , 1, 255L);
                break;
            case H3CV7:
                getErrorMessage3(staticRoutingInfoDTO, sb);
                getErrorMessage2(staticRoutingInfoDTO, sb);
                getErrorMessage10(staticRoutingInfoDTO, sb);
                cmdDto.getPolicy().setDescription(AliStringUtils.filterChinese(cmdDto.getPolicy().getDescription()));
                cmdDto.getPolicy().setDescription(cmdDto.getPolicy().getDescription().replace("?", ""));
                cmdDto.getPolicy().setDescription(cmdDto.getPolicy().getDescription().replace("？", ""));
                cmdDto.getPolicy().setDescription(cmdDto.getPolicy().getDescription().replace(" ", ""));
                getErrorMessage9(staticRoutingInfoDTO, sb , 1, 255L);
                break;
            case TOPSEC_TOS_010_020:
                getErrorMessage(staticRoutingInfoDTO, sb);
                getErrorMessage9(staticRoutingInfoDTO, sb , 1, 65535L);
                getErrorMessage11(cmdDto, sb);
                break;
            case FORTINET:
                getErrorMessage7(staticRoutingInfoDTO, sb);
                getErrorMessage9(staticRoutingInfoDTO, sb , 0, 4294967295L);
                break;
            case HILLSTONE_V5:
//                getErrorMessage8(cmdDto, staticRoutingInfoDTO, sb);
                getErrorMessage9(staticRoutingInfoDTO, sb , 1, 255L);
                break;
            default:
                break;
        }
        businessInfoDTO.setOtherErrorMsg(sb.toString());
    }

    private void getErrorMessage(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isEmpty(staticRoutingInfoDTO.getNextHop()) && StringUtils.isEmpty(staticRoutingInfoDTO.getOutInterface())) {
            sb.append("下一跳和出接口不可同时为空，请选择一个配置").append(StringUtils.LF);
        }

    }

    private void getErrorMessage1(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getNextHop()) && StringUtils.isNotEmpty(staticRoutingInfoDTO.getOutInterface())) {
            sb.append("下一跳和出接口不可同时配置，只能选择一个配置").append(StringUtils.LF);
        }
    }

    private void getErrorMessage2(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isEmpty(staticRoutingInfoDTO.getNextHop()) && StringUtils.isNotEmpty(staticRoutingInfoDTO.getDstVirtualRouter())) {
            sb.append("当配置了目的VPN实例时，必须配置下一跳").append(StringUtils.LF);
        }
    }

    private void getErrorMessage3(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getOutInterface()) && StringUtils.isNotEmpty(staticRoutingInfoDTO.getDstVirtualRouter())) {
            sb.append("目的VPN实例和出接口不可同时配置").append(StringUtils.LF);
        }
    }

    private void getErrorMessage4(CmdDTO dto, CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {

        //所属虚拟路由器或者目的虚拟路由器不为空时
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getSrcVirtualRouter()) || StringUtils.isNotEmpty(staticRoutingInfoDTO.getDstVirtualRouter())) {
            //查找此设备下的所有墙
            List<NodeEntity> nodeEntities = nodeMapper.listVsysDetailByIp(dto.getDevice().getNodeEntity().getIp());

            if (CollectionUtils.isNotEmpty(nodeEntities)) {
                List<String> stringList = nodeEntities.stream().filter(p -> StringUtils.isNotEmpty(p.getUuid())).map(p -> p.getUuid()).collect(Collectors.toList());

                BatchDeviceDTO batchDeviceDTO = new BatchDeviceDTO();

                batchDeviceDTO.setDeviceUuids(stringList);
                batchDeviceDTO.setNeedInterface(false);
                batchDeviceDTO.setNeedUuidVersion(false);
                batchDeviceDTO.setSkipHost(true);

                List<String> vsysNameList = null;

                //查找此设备下的所有虚墙名称
                ResultRO<List<DeviceDataRO>> deviceROByUuidList = whaleDeviceObjectClient.getDeviceROByUuidList(batchDeviceDTO);
                if (deviceROByUuidList.getSuccess() && CollectionUtils.isNotEmpty(deviceROByUuidList.getData())) {
                    List<DeviceDataRO> data = deviceROByUuidList.getData();
                    vsysNameList = data.stream().filter(p -> StringUtils.isNotEmpty(p.getVsysName())).map(p -> p.getVsysName()).collect(Collectors.toList());
                }
                if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getSrcVirtualRouter())) {
                    if (CollectionUtils.isNotEmpty(vsysNameList) && vsysNameList.contains(staticRoutingInfoDTO.getSrcVirtualRouter())) {
                        sb.append("源虚拟路由器不可选择虚系统").append(StringUtils.LF);
                    }
                }
                if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getDstVirtualRouter())) {
                    if (CollectionUtils.isNotEmpty(vsysNameList) && vsysNameList.contains(staticRoutingInfoDTO.getDstVirtualRouter())) {
                        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getNextHop()) || StringUtils.isNotEmpty(staticRoutingInfoDTO.getOutInterface())) {
                            sb.append("目的虚拟路由器选择虚系统时，则既不允许配置下一跳，也不允许配置出接口").append(StringUtils.LF);
                        }
                    }
                }
            }
        }

    }

    private void getErrorMessage5(CmdDTO dto, CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        PolicyDTO policy = dto.getPolicy();
        if (IpTypeEnum.IPV6.getCode() == policy.getIpType().intValue() && StringUtils.isNotEmpty(staticRoutingInfoDTO.getOutInterface())) {
            sb.append("ipv6不允许配置出接口").append(StringUtils.LF);
        }
    }

    private void getErrorMessage6(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isEmpty(staticRoutingInfoDTO.getNextHop()) || StringUtils.isEmpty(staticRoutingInfoDTO.getOutInterface())) {
            sb.append("下一跳和出接口需要同时配置").append(StringUtils.LF);
        }
    }

    private void getErrorMessage7(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isEmpty(staticRoutingInfoDTO.getOutInterface())) {
            sb.append("出接口必须配置").append(StringUtils.LF);
        }
    }

    private void getErrorMessage8(CmdDTO dto, CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) throws Exception {
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getOutInterface())){
            ZoneRO zoneRO = whaleDeviceObjectClient.getDeviceObjectToZone(dto.getDevice().getDeviceUuid(), null, null,null);
            if (CollectionUtils.isNotEmpty(zoneRO.getData())){
                List<String> list = new ArrayList<>();
                String name = "";
                if (StringUtils.isEmpty(staticRoutingInfoDTO.getSrcVirtualRouter())){
                    name = "trust-vr__VROUTER_IF_GROUP";
                }else {
                    name = staticRoutingInfoDTO.getSrcVirtualRouter()+"__VROUTER_IF_GROUP";
                }
                String finalName = name;
                String zoneUuid = zoneRO.getData().stream().filter(p -> finalName.equals(p.getName())).map(p -> p.getUuid()).collect(Collectors.toList()).get(0);

                ZoneRO zoneRO1 = whaleDeviceObjectClient.getDeviceZoneRO(dto.getDevice().getDeviceUuid(), zoneUuid);
                if (CollectionUtils.isNotEmpty(zoneRO1.getData())){
                    List<ZoneDataRO> zoneRO1Data = zoneRO1.getData();
                    for (ZoneDataRO zoneDataRO : zoneRO1Data) {
                        List<InterfacesRO> interfacesROS = zoneDataRO.getInterfaces().stream().filter(p -> staticRoutingInfoDTO.getOutInterface().equals(p.getDeviceInterfaceName())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(interfacesROS)){
                            list.add(interfacesROS.get(0).getDeviceInterfaceName());
                        }
                    }
                }
                if (CollectionUtils.isEmpty(list)){
                    sb.append("所属虚拟路由器的安全域无此出接口");
                }

            }
        }

    }

    private void getErrorMessage9(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb,int min,Long max){
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getPriority())){
            long index = Long.parseLong(staticRoutingInfoDTO.getPriority());
            if (index > max || index < min){
                sb.append(String.format("优先级的值已经超过%d~%d限制",min,max));
            }
        }
    }

    private void getErrorMessage10(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb) {
        if (StringUtils.isEmpty(staticRoutingInfoDTO.getNextHop())) {
            sb.append("下一跳必须配置").append(StringUtils.LF);
        }
    }

    private void getErrorMessage11(CmdDTO dto, StringBuilder sb) {
        PolicyDTO policy = dto.getPolicy();
        if (IpTypeEnum.IPV6.getCode() == policy.getIpType().intValue()) {
            sb.append("不支持生成ipv6命令行").append(StringUtils.LF);
        }
    }

    private void getErrorMessage12(CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO, StringBuilder sb,int min,Long max){
        if (StringUtils.isNotEmpty(staticRoutingInfoDTO.getManagementDistance())){
            long index = Long.parseLong(staticRoutingInfoDTO.getManagementDistance());
            if (index > max || index < min){
                sb.append(String.format("管理距离的值已经超过%d~%d限制",min,max));
            }
        }
    }

}
