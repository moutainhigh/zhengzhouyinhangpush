package com.abtnetworks.totems.common.commandline.forbid;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/9/11
 */
@Slf4j
@Service
public class ForbidHillStoneR5 implements PolicyGenerator {


    @Override
    public String generate(CmdDTO cmdDTO) {

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        TaskDTO taskDTO = cmdDTO.getTask();

        SettingDTO settingDTO = cmdDTO.getSetting();

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        String policyName = taskDTO.getTheme();
        generatedObjectDTO.setPolicyName(policyName);

        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddress = generateAddressObject(policyDTO.getSrcIp(), taskDTO.getTheme(), "src");

        sb.append("configure\n");
        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
            generatedObjectDTO.setSrcObjectName(srcAddress.getName());
        }
        sb.append("rule ");
        int moveSeatCode = settingDTO.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("top \n");
        } else {
            sb.append("\n");
        }
        sb.append(String.format("name %s\n", policyName));
        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("src-zone %s\n", policyDTO.getSrcZone()));
        }
        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("dst-zone %s\n", policyDTO.getDstZone()));
        }
        sb.append(srcAddress.getJoin());

        //封禁目的是any
        sb.append("dst-addr any \n");
        sb.append("service any \n");
        sb.append(String.format("action %s\n", policyDTO.getAction().getKey().toLowerCase()));
        sb.append("exit\n");
        sb.append("end\n");

        return sb.toString();
    }


    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin(ipPrefix + "-addr any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
        }

        dto.setObjectFlag(true);

        StringBuilder sb = new StringBuilder();

        String name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        sb.append(String.format("address %s\n", name));

        String[] arr = ipAddress.split(",");

        String fullStr = "";

        for (String address : arr) {
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/32\n", address);
            }
            sb.append(fullStr);
        }

        sb.append("exit\n");
        dto.setCommandLine(sb.toString());
        dto.setName(name);
        dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
        return dto;
    }


    public static void main(String[] args) {
        String srcIp = "1.1.1.1,2.2.2.2,3.3.3.3";
        ForbidHillStoneR5 forbidHillStoneR5 = new ForbidHillStoneR5();
        PolicyObjectDTO srcObject = forbidHillStoneR5.generateAddressObject(srcIp, "AC101", "src");
        System.out.println(JSONObject.toJSONString(srcObject, true));

    }

}
