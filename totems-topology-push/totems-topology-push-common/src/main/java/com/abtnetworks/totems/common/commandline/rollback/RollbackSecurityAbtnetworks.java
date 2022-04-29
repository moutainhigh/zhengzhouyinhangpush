package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class RollbackSecurityAbtnetworks implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityAbtnetworks(){
        generatorBean = new SecurityAbtImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        return null;
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO dto = new PolicyGeneratorDTO();

        //判断是ipv4还是ipv6
        RuleIPTypeEnum type = RuleIPTypeEnum.IP4;
        //判断是ipv4还是ipv6
        PolicyDTO policy = cmdDTO.getPolicy();
        if(policy!=null){
            String srcIp = policy.getSrcIp();
            if(StringUtils.isNotEmpty(srcIp)){
                List<String> ipList = Arrays.asList(srcIp.split(","));
                for(int i=0;i<ipList.size();i++){
                    String address = ipList.get(i);
                    if(IpUtils.isIPRange(address) || IpUtils.isIPSegment(address) ||IpUtils.isIP(address) ){
                        type = RuleIPTypeEnum.IP4;
                        break;
                    }else if(address.contains(":")){
                        type = RuleIPTypeEnum.IP6;
                    }
                }
            }
        }

        if(cmdDTO.getSetting()==null || cmdDTO.getSetting().getPolicyId()==null){
            log.error("策略回滚命令行生成失败，找不到策略id");
            dto.setPolicyRollbackCommandLine("策略回滚命令行生成失败，找不到策略id");
            return dto;
        }

        StringBuffer sb = new StringBuffer("");
        sb.append("enable").append(StringUtils.LF);
        sb.append("configure terminal").append(StringUtils.LF);
        if(type == RuleIPTypeEnum.IP4){
            sb.append(String.format("no policy %s",cmdDTO.getSetting().getPolicyId())).append(StringUtils.LF);
        }else{
            sb.append(String.format("no policy6 %s",cmdDTO.getSetting().getPolicyId())).append(StringUtils.LF);
        }
        sb.append("end").append(StringUtils.LF);
        sb.append("save config").append(StringUtils.LF);
        dto.setPolicyRollbackCommandLine(sb.toString());
        return dto;
    }
}
