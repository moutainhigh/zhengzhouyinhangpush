package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.constants.FieldConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.vender.Juniper.nat.NatJuniperSRXImpl;
import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author luwei
 * @date 2020/7/22
 */
@Service
@Log4j2
public class RollbackSecurityJuniperSrx implements PolicyGenerator {

    protected OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityJuniperSrx(){
        generatorBean = new SecurityJuniperSRXImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();

        String srcZoneJoin = String.format("from-zone %s", StringUtils.isBlank(policyDTO.getSrcZone()) ? "any" : policyDTO.getSrcZone());
        String dstZoneJoin = String.format("to-zone %s", StringUtils.isBlank(policyDTO.getDstZone()) ? "any" : policyDTO.getDstZone());

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuffer sb = new StringBuffer();
        sb.append("configure\n");

        sb.append(String.format("delete security policies %s %s policy %s\n", srcZoneJoin, dstZoneJoin, generatedObjectDTO.getPolicyName()));

        sb.append("commit\n");
        sb.append("exit\n");
        return sb.toString();
    }


    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        String srcZoneJoin = String.format("from-zone %s", StringUtils.isBlank(policyDTO.getSrcZone()) ? "any" : policyDTO.getSrcZone());
        String dstZoneJoin = String.format("to-zone %s", StringUtils.isBlank(policyDTO.getDstZone()) ? "any" : policyDTO.getDstZone());

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuffer sb = new StringBuffer();
        sb.append("configure\n");

        sb.append(String.format("delete security policies %s %s policy %s\n", srcZoneJoin, dstZoneJoin, generatedObjectDTO.getPolicyName()));
        sb.append("commit\n");
        sb.append("exit\n");


        StringBuffer  objRollbackCommandLine = new StringBuffer();
        try {
            log.info("??????????????????????????? ?????????:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressGroupNames = generatedObjectDTO.getAddressObjectGroupNameList();
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();

            if(CollectionUtils.isEmpty(addressGroupNames) && CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames)){
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }
            objRollbackCommandLine.append(StringUtils.LF);
            objRollbackCommandLine.append("configure\n");

            // ??????????????????????????????
            if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                for (String addressGroupName : addressGroupNames) {
                    // ??????????????????cmd
                    if (StringUtils.isBlank(addressGroupName)) {
                        continue;
                    }
                    // ?????????groupNames????????????   ??????,???????????????,????????????
                    String[] groupNames = addressGroupName.split(",");
                    Map<String, Object> map = new HashMap<>();
                    map.put(FieldConstants.JUNIPER_ZONE_NAME ,groupNames[0]);
                    map.put(FieldConstants.JUNIPER_OBJECT_GROUP_NAME,groupNames[1]);
                    map.put(FieldConstants.JUNIPER_OBJECT_NAME,groupNames[2]);
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectGroupCommandLine(null, null, null, map, null));
                }
            }

            // ???????????????????????????
            if (CollectionUtils.isNotEmpty(addressNames)) {
                objRollbackCommandLine.append(StringUtils.LF);
                for (String addressName : addressNames) {
                    // ??????????????????cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    // ?????????addressName????????????   ??????,????????????,????????????
                    String[] groupNames = addressName.split(",");
                    Map<String, Object> map = new HashMap<>();
                    map.put(FieldConstants.JUNIPER_ZONE_NAME ,groupNames[0]);
                    map.put(FieldConstants.JUNIPER_OBJECT_NAME,groupNames[1]);
                    map.put(FieldConstants.JUNIPER_IP_NAME,groupNames[2]);
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectCommandLine(null, null, null, map, null));
                }
            }

            // ???????????????????????????
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                for (String serviceName : serviceNames) {
                    // ??????????????????cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
            }
            objRollbackCommandLine.append("commit\n");
            objRollbackCommandLine.append("exit\n");
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????,????????????:{}", e);
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
