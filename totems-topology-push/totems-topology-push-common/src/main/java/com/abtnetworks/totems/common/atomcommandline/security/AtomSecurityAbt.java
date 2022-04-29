package com.abtnetworks.totems.common.atomcommandline.security;

import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
/**
 * @Author: wangxinghui
 * @Date:2021-07-19
 */
@Service
@Slf4j
public class AtomSecurityAbt extends SecurityPolicyGenerator implements PolicyGenerator {

    private SecurityAbtImpl generatorBean;

    public AtomSecurityAbt() {
        generatorBean = new SecurityAbtImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("原子化命令行 cmdDTO is " + cmdDTO);
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
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

        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        if(ObjectUtils.isNotEmpty(taskDTO.getTheme())){
            String theme=taskDTO.getTheme();
            String policyName=theme+"_AO_"+ RandomStringUtils.random(4, '0', '1', '2', '3', '4', '5', '6', '7', '8','9');
            dto.setBusinessName(policyName);
        }

        // ip类型默认为ipv4
        if (ObjectUtils.isEmpty(dto.getIpType())) {
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setVsys(dto.isVsys());
        generatedDto.setVsysName(dto.getVsysName());
        generatedDto.setHasVsys(dto.isHasVsys());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return generatorBean.generatePreCommandline(dto.isVsys(),dto.getVsysName(),null,null);
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
        HashMap<String,Object> map = new HashMap<>();
        String securityCl = null;
        StringBuilder sb = new StringBuilder();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();  //true:需要生成地址对象然后引用  false:直接使用IP地址

        List<String> createAddressObjectNames = new ArrayList<>();
        List<String> createServiceObjectNames = new ArrayList<>();
        List<String> createTimeObjectNames = new ArrayList<>();
        List<String> srcRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), dto.getSrcIpSystem(), sb, createObjFlag,generatorBean);
        createAddressObjectNames.addAll(srcRefIpAddressNames);
        List<String> dstRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(), dto.getDstIpSystem(), sb, createObjFlag,generatorBean);
        createAddressObjectNames.addAll(dstRefIpAddressNames);
        List<String> refServiceNames = Param4CommandLineUtils.getRefServiceNames(dto.getServiceName(), dto.getExistServiceNameList(), dto.getRestServiceList(),sb,generatorBean);
        createServiceObjectNames.addAll(refServiceNames);

        String refTimeName = "";
        if(StringUtils.isNotBlank(endTime)){
            // 结束时间不能为空
            refTimeName = Param4CommandLineUtils.getRefTimeName(startTime, endTime, sb,generatorBean);
            createTimeObjectNames.add(refTimeName);
        }
        // 统计创建地址名称
        dto.setAddressObjectNameList(createAddressObjectNames);
        dto.setServiceObjectNameList(createServiceObjectNames);
        dto.setTimeObjectNameList(createTimeObjectNames);

        try {
            StatusTypeEnum type = StatusTypeEnum.ADD_IPV4;
            //判断是ipv4还是ipv6
            if(dto.getIpType()!=null && dto.getIpType()==2){
                String srcIp = dto.getSrcIp();
                if(StringUtils.isNotEmpty(srcIp)){
                    List<String> ipList = Arrays.asList(srcIp.split(","));
                    for(int i=0;i<ipList.size();i++){
                        String address = ipList.get(i);
                        if(IpUtils.isIPRange(address) || IpUtils.isIPSegment(address) ||IpUtils.isIP(address) ){
                            type = StatusTypeEnum.ADD_IPV4;
                            break;
                        }else if(address.contains(":")){
                            type = StatusTypeEnum.ADD_IPV6;
                        }
                    }
                }
            }else if(dto.getIpType()!=null && dto.getIpType()==0){
                type = StatusTypeEnum.ADD_IPV4;
            }else if(dto.getIpType()!=null && dto.getIpType()==1){
                type = StatusTypeEnum.ADD_IPV6;
            }

            map.put("description",dto.getDescription());

            securityCl = generatorBean.generateSecurityPolicyCommandLine(type, null,dto.getBusinessName(), dto.getPolicyId(), dto.getAction().toLowerCase(), dto.getDescription(),
                    null, null, null, com.abtnetworks.totems.command.line.enums.MoveSeatEnum.getByCode(dto.getMoveSeatEnum().getCode()), dto.getSwapRuleNameId(),
                    null, null, null, null, null, new ZoneParamDTO(dto.getSrcZone()), new ZoneParamDTO(dto.getDstZone()),
                    null, null, srcRefIpAddressNames.toArray(new String[0]), null, dstRefIpAddressNames.toArray(new String[0]),null,
                    refServiceNames.toArray(new String[0]), null, new String[]{refTimeName}, map, null);
        } catch (Exception e) {
            log.error("原子化命令行创建安全策略异常",e);
        }
        sb.append(securityCl);
        return sb
                .toString();
    }


    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return generatorBean.generatePostCommandline(null,null);
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        AtomSecurityAbt abtcomandEntity = new AtomSecurityAbt();
        String commandLine = abtcomandEntity.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
