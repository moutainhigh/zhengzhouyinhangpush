package com.abtnetworks.totems.common.commandline.rollback.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.topsec.TOS_010.SecurityTopsec010Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.commandline.NatPolicyGenerator.DO_NOT_SUPPORT;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Slf4j
@Service
public class RollbackTopsecTos010020 implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;

    public RollbackTopsecTos010020() {
        generatorBean = new SecurityTopsec010Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("天融信010020开始回滚---");
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        Integer ipType = generatedObject.getIpType();
        if (StringUtils.isBlank(rollbackShowCmd)) {
            return DO_NOT_SUPPORT;
        }else{
            StringBuffer stringBuffer = new StringBuffer();
            //            ipv6
            if(ipType!=null&&ipType==1){
                stringBuffer.append("firewall6\n");
            }else {
                stringBuffer.append("firewall\n");
            }
            String policyId = cmdDTO.getSetting().getPolicyId();
            if (StringUtils.isBlank(policyId)) {
                stringBuffer.append(CommonConstants.POLICY_SHOW_TOP_SEC) .append(" ")
                        .append(rollbackShowCmd).append("\n");
                stringBuffer.append("policy delete id ").append(CommonConstants.POLICY_ID).append("\n");
            } else {
                stringBuffer.append("policy delete id ").append(policyId).append("\n");
            }

            stringBuffer.append("end\n");
            return stringBuffer.toString();
        }

    }


    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        log.info("天融信010020开始回滚---");
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        Integer ipType = generatedObject.getIpType();
        if (StringUtils.isBlank(rollbackShowCmd)) {
            policyGeneratorDTO.setPolicyRollbackCommandLine(DO_NOT_SUPPORT);
            return policyGeneratorDTO;
        }else{
            StringBuffer stringBuffer = new StringBuffer();
            //            ipv6
            if(ipType!=null&&ipType==1){
                stringBuffer.append("firewall6\n");
            }else {
                stringBuffer.append("firewall\n");
            }
            String policyId = cmdDTO.getSetting().getPolicyId();
            if (StringUtils.isBlank(policyId)) {
                stringBuffer.append(CommonConstants.POLICY_SHOW_TOP_SEC) .append(" ")
                        .append(rollbackShowCmd).append("\n");
                stringBuffer.append("policy delete id ").append(CommonConstants.POLICY_ID).append("\n");
            } else {
                stringBuffer.append("policy delete id ").append(policyId).append("\n");
            }
            stringBuffer.append("end\n");




            StringBuffer objRollbackCommandLine = new StringBuffer();
            try{
                log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedObject));
                Map<String,String> addressMap = generatedObject.getAddressTypeMap();
                List<String> serviceNames = generatedObject.getServiceObjectNameList();
                List<String> timeObjectNameList = generatedObject.getTimeObjectNameList();
                if(addressMap.isEmpty() && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeObjectNameList)){
                    policyGeneratorDTO.setPolicyRollbackCommandLine(stringBuffer.toString());
                    return policyGeneratorDTO;
                }

                // 在天融信设备上测试过，不用重新进入试图 就能执行删除对象命令行操作

                RuleIPTypeEnum ipTypeEnum = null;
                if (IpTypeEnum.IPV6.getCode().equals(generatedObject.getIpType())) {
                    ipTypeEnum = RuleIPTypeEnum.IP6;
                } else {
                    ipTypeEnum = RuleIPTypeEnum.IP4;
                }

                // 地址对象拼接对象回滚命令行
                if(!addressMap.isEmpty()){
                    for (String key :addressMap.keySet()){
                        objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectCommandLine(ipTypeEnum,addressMap.get(key),key,null,null));
                    }
                    objRollbackCommandLine.append(StringUtils.LF);
                }


                // 服务对象拼接对象回滚命令行
                if (CollectionUtils.isNotEmpty(serviceNames)) {
                    for (String serviceName : serviceNames) {
                        // 拼接对象回滚cmd
                        if (StringUtils.isBlank(serviceName)) {
                            continue;
                        }
                        objRollbackCommandLine
                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                    }
                    objRollbackCommandLine.append(StringUtils.LF);
                }

                // 时间对象拼接对象回滚命令行
                if (CollectionUtils.isNotEmpty(timeObjectNameList)) {
                    for (String timeName : timeObjectNameList) {
                        // 拼接对象回滚cmd
                        if (StringUtils.isBlank(timeName)) {
                            continue;
                        }
                        objRollbackCommandLine.append(generatorBean.deleteAbsoluteTimeCommandLine(timeName, null, null));
                    }
                    objRollbackCommandLine.append(StringUtils.LF);
                }
                objRollbackCommandLine.append("end\n");
            }catch (Exception e){
                log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
            }
            policyGeneratorDTO.setPolicyRollbackCommandLine(stringBuffer.toString());
            policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
            return policyGeneratorDTO;
        }

    }
}
