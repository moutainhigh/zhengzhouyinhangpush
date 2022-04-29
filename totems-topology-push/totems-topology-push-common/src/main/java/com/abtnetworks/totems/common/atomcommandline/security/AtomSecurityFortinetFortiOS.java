package com.abtnetworks.totems.common.atomcommandline.security;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AtomSecurityFortinetFortiOS extends SecurityPolicyGenerator implements PolicyGenerator {

    private SecurityFortinetImpl generatorBean;

    public AtomSecurityFortinetFortiOS() {
        generatorBean = new SecurityFortinetImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("原子化命令行 cmdDTO is " + cmdDTO);
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);

        SpecialNatDTO specialNatDTO = dto.getSpecialNatDTO();
        BeanUtils.copyProperties(policyDTO.getSpecialNatDTO(), specialNatDTO);
        specialNatDTO.setExistVipName(policyDTO.getExistVirtualIpName());
        specialNatDTO.setPostSrcIp(policyDTO.getPostSrcIp());

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
        }

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        specialNatDTO.setExistPoolName(existObjectDTO.getPostSrcAddressObjectName());


        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());
        
        if(ObjectUtils.isNotEmpty(taskDTO.getTheme())){
            String theme=taskDTO.getTheme();
            String policyName=theme+"_AO_"+ RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
            dto.setBusinessName(policyName);
        }
        //dto.setGroupName();
        // ip类型默认为ipv4
        if (ObjectUtils.isEmpty(dto.getIpType())) {
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setVsys(dto.isVsys());
        generatedDto.setVsysName(dto.getVsysName());
        generatedDto.setHasVsys(dto.isHasVsys());
        log.info("原子化命令行dto:{}", JSONObject.toJSONString(dto));
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        Map<String,Object> map = new HashMap();
        map.put("hasVsys",dto.isHasVsys());
        return generatorBean.generatePreCommandline(dto.isVsys(),dto.getVsysName(),map,null);
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String createCommandLine(CommandlineDTO dto) {
        String securityCl = null;
        StringBuilder sb = new StringBuilder();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        SpecialNatDTO specialNatDTO = dto.getSpecialNatDTO();

        // 是否创建pool池
        String poolName = null;
        String createPoolCommandLine = null;
        if (StringUtils.isNotBlank(specialNatDTO.getExistPoolName())) {
            poolName = specialNatDTO.getExistPoolName();

        } else {
            if (StringUtils.isNotBlank(specialNatDTO.getPostSrcIp())) {
                // 构建pool
                Map<String, Object> nameMap = new HashMap<>();
                nameMap.put("theme", dto.getName());
                IpAddressParamDTO postSrcIpParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(specialNatDTO.getPostSrcIp());
                Map<String, String> resultMap = buildNatPoolAddress(postSrcIpParamDTO, null, null, nameMap);
                poolName = resultMap.get("poolName");
                createPoolCommandLine = resultMap.get("poolCl");
            }
        }


        // 是否创建vip
        String vipName = null;
        String createVipCommandLine = null;
        StringBuilder serviceSb = new StringBuilder();
        List<String> vipPostServiceNames = null;
        if (StringUtils.isNotBlank(specialNatDTO.getExistVipName())) {
            vipName = specialNatDTO.getExistVipName();
        } else {
            if (StringUtils.isNotBlank(specialNatDTO.getPostDstIp())) {
                StringBuilder vipSb = new StringBuilder();
                String name = String.format("mip_%s_%s", dto.getName(), IdGen.getRandomNumberString());
                vipSb.append("config firewall vip\n");
                vipSb.append(String.format("edit \"%s\"",name)).append("\n");
                Param4CommandLineUtils.setAddressCommandLine(specialNatDTO.getDstIp(), vipSb, "extip ");
                if (StringUtils.isNotEmpty(specialNatDTO.getSrcItf())) {
                    vipSb.append(String.format("set extintf \"%s\"", specialNatDTO.getSrcItf())).append("\n");
                } else {
                    vipSb.append("set extintf any\n");
                }
                Param4CommandLineUtils.setAddressCommandLine(specialNatDTO.getPostDstIp(), vipSb, "mappedip ");

                String protocol = CollectionUtils.isNotEmpty(specialNatDTO.getServiceList()) ? specialNatDTO.getServiceList().get(0).getProtocol() : PolicyConstants.POLICY_STR_VALUE_ANY;
                String dstPorts = CollectionUtils.isNotEmpty(specialNatDTO.getServiceList()) ? specialNatDTO.getServiceList().get(0).getDstPorts() : PolicyConstants.POLICY_STR_VALUE_ANY;
                String postPort = CollectionUtils.isNotEmpty(specialNatDTO.getPostServiceList()) ? specialNatDTO.getPostServiceList().get(0).getDstPorts() : PolicyConstants.POLICY_STR_VALUE_ANY;
                String protocolString = ProtocolUtils.getProtocolByString(protocol);

                vipSb.append(generatorBean.generateProtocolCommandline(protocol, dstPorts, postPort, protocolString));

                vipSb.append("next\n");
                vipSb.append("end\n\n");
                createVipCommandLine = vipSb.toString();

                if (CollectionUtils.isNotEmpty(specialNatDTO.getPostServiceList())) {
                    // 不为空，且不为any的时候才新建转换后的服务对象，然后安全策略引用。否则直接用策略建议中的服务
                    if (StringUtils.isNotBlank(specialNatDTO.getPostServiceList().get(0).getProtocol()) &&
                            !PolicyConstants.POLICY_STR_VALUE_ANY.equals(specialNatDTO.getPostServiceList().get(0).getProtocol()) &&
                            !PolicyConstants.POLICY_NUM_VALUE_ANY.equals(specialNatDTO.getPostServiceList().get(0).getProtocol())) {
                        for (ServiceDTO serviceDTO : specialNatDTO.getPostServiceList()) {
                            serviceDTO.setProtocol(ProtocolUtils.getProtocolNumberByName(serviceDTO.getProtocol()));
                        }
                        vipPostServiceNames = Param4CommandLineUtils.getRefServiceNames(null, null, specialNatDTO.getPostServiceList(), serviceSb, generatorBean);
                    }
                }

                vipName = name;
            }
        }
        sb.append(serviceSb.toString()).append(StringUtils.LF);
        boolean createObjFlag = dto.isCreateObjFlag();  //true:需要生成地址对象然后引用  false:直接使用IP地址
        List<String> srcRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), dto.getSrcIpSystem(), sb, createObjFlag,generatorBean);
        List<String> dstRefIpAddressNames = null;
        if(StringUtils.isBlank(vipName)){
            dstRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(), dto.getDstIpSystem(), sb, createObjFlag,generatorBean);
        }

        List<String> refServiceNames = null;
        if (CollectionUtils.isNotEmpty(vipPostServiceNames)) {
            refServiceNames = vipPostServiceNames;
        } else {
            refServiceNames = Param4CommandLineUtils.getRefServiceNames(dto.getServiceName(), dto.getExistServiceNameList(), dto.getRestServiceList(), sb, generatorBean);
        }
        String refTimeName = Param4CommandLineUtils.getRefTimeName(startTime, endTime, sb,generatorBean,null);
        try {
//            List<String> srcRefGroupNames = Param4CommandLineUtils.getAddressGroup(StatusTypeEnum.ADD,srcRefIpAddressNames,dto.getRestSrcAddressList(),sb,generatorBean);
//            List<String> dstRefGroupNames = Param4CommandLineUtils.getAddressGroup(StatusTypeEnum.ADD,dstRefIpAddressNames,dto.getRestDstAddressList(),sb,generatorBean);
//            List<String> serviceGroupNames = Param4CommandLineUtils.getServiceGroup(refServiceNames,dto.getRestSrcAddressList(),sb,generatorBean);


            StatusTypeEnum type = StatusTypeEnum.ADD_IPV4;
            if (dto.getIpType() == 1) {
                type = StatusTypeEnum.ADD_IPV6;
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("poolName", poolName);
            paramMap.put("createPoolCommandLine", createPoolCommandLine);
            paramMap.put("vipName", vipName);
            paramMap.put("createVipCommandLine", createVipCommandLine);
            paramMap.put("convertOutItf", specialNatDTO.getConvertOutItf());

            String[] vipNames = StringUtils.isNotBlank(vipName) ? vipName.split(",") : new String[0];
            securityCl = generatorBean.generateSecurityPolicyCommandLine(type, dto.getName(),dto.getBusinessName(), "#1", dto.getAction().toLowerCase(),
                    dto.getDescription(), null, null, null, com.abtnetworks.totems.command.line.enums.MoveSeatEnum.getByCode(dto.getMoveSeatEnum().getCode()),
                    dto.getSwapRuleNameId(),null, null, null, null, null,
                    StringUtils.isNoneEmpty(dto.getSrcZone()) ? new ZoneParamDTO(dto.getSrcZone()) : null, StringUtils.isNoneEmpty(dto.getDstZone()) ?
                            new ZoneParamDTO(dto.getDstZone()) : null , StringUtils.isNoneEmpty(dto.getSrcItf()) ? new InterfaceParamDTO(dto.getSrcItf()) : null,
                    StringUtils.isNoneEmpty(dto.getDstItf()) ? new InterfaceParamDTO(dto.getDstItf()) : null, srcRefIpAddressNames.toArray(new String[0]),
                    null, StringUtils.isNotBlank(vipName) ? vipNames : null == dstRefIpAddressNames ? new String[0] : dstRefIpAddressNames.toArray(new String[0]),null, refServiceNames.toArray(new String[0]),
                    null, new String[]{refTimeName}, paramMap, null);
        } catch (Exception e) {
            log.error("原子化命令行创建安全策略异常",e);
        }
        sb.append(securityCl);
        return sb.toString();
    }

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        String aa= (String) paramMap.get("hhah");
        System.out.println(aa);
    }

    private Map<String, String> buildNatPoolAddress(IpAddressParamDTO postSrcIpAddress, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map) {
        Map poolMap = new HashMap();
        StringBuilder commandLine = new StringBuilder();
        commandLine.append("config firewall ippool").append(StringUtils.LF);;
        String poolObjectName = createPoolObjectName(map, null);
        commandLine.append(String.format("edit \"%s\"",poolObjectName)).append(StringUtils.LF);
        IpAddressRangeDTO[] rangIpArray = postSrcIpAddress.getRangIpArray();
        String[] singleIpArray = postSrcIpAddress.getSingleIpArray();
        IpAddressSubnetIntDTO[] subnetIntIpArray = postSrcIpAddress.getSubnetIntIpArray();
        if(ObjectUtils.isNotEmpty(rangIpArray)){
            IpAddressRangeDTO dto = rangIpArray[0];
            commandLine.append(String.format("set startip %s",dto.getStart())).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",dto.getEnd())).append(StringUtils.LF);
        }else if(ObjectUtils.isNotEmpty(subnetIntIpArray)){
            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIntIpArray[0];
            long[] ipStartEnd= TotemsIpUtils.getIpStartEndBySubnetMask(ipAddressSubnetIntDTO.getIp(), String.valueOf(ipAddressSubnetIntDTO.getMask()));
            String startIp =TotemsIpUtils.IPv4NumToString(ipStartEnd[0]);
            String endIp =TotemsIpUtils.IPv4NumToString(ipStartEnd[1]);
            commandLine.append(String.format("set startip %s",startIp)).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",endIp)).append(StringUtils.LF);
        }else if(ObjectUtils.isNotEmpty(singleIpArray)){
            String ip = singleIpArray[0];
            commandLine.append(String.format("set startip %s",ip)).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",ip)).append(StringUtils.LF);
        }
        commandLine.append("next").append(StringUtils.LF);;
        commandLine.append("end").append(StringUtils.LF).append(StringUtils.LF);
        poolMap.put("poolName",poolObjectName);
        poolMap.put("poolCl",commandLine.toString());
        return poolMap;
    }


    String createPoolObjectName(Map<String, Object> map ,String[] args){
        String name =(String) map.get("name");
        if(ObjectUtils.isEmpty(name)){
            name= (String) map.get("theme");
            name+="_pool_"+RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
        }
        return name;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return generatorBean.generatePostCommandline(null,null);
    }

//    public static void main(String[] args) {
//        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
//        AtomSecurityFortinetFortiOS fortinetFortiOS = new AtomSecurityFortinetFortiOS();
//        String commandLine = fortinetFortiOS.composite(dto);
//        System.out.println("commandline:\n" + commandLine);
//    }

}
