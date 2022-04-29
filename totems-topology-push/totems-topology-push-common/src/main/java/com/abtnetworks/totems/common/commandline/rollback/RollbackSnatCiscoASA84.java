package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 思科ASA和ASA8.4 支持snat策略回滚
 * @date 2021/7/27 16:41
 */
@Service
public class RollbackSnatCiscoASA84 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n");

        String srcItf = StringUtils.isNotEmpty(policyDTO.getSrcZone()) ? policyDTO.getSrcZone() : policyDTO.getSrcItf();
        String dstItf = StringUtils.isNotEmpty(policyDTO.getDstZone()) ? policyDTO.getDstZone() : policyDTO.getDstItf();

        sb.append(String.format("no nat (%s,%s) source dynamic PAT %s", srcItf, dstItf, policyDTO.getPostSrcIp()));

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();

    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        // 这里 cisco ASA老的设备 snat创建的时候没有地址对象和服务对象的创建
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n");

        String srcItf = StringUtils.isNotEmpty(policyDTO.getSrcZone()) ? policyDTO.getSrcZone() : policyDTO.getSrcItf();
        String dstItf = StringUtils.isNotEmpty(policyDTO.getDstZone()) ? policyDTO.getDstZone() : policyDTO.getDstItf();

        sb.append(String.format("no nat (%s,%s) source dynamic PAT %s", srcItf, dstItf, policyDTO.getPostSrcIp()));

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        return policyGeneratorDTO;
    }
}
