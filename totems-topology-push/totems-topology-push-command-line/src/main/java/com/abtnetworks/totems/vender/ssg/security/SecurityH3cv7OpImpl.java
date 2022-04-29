package com.abtnetworks.totems.vender.ssg.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;

import java.util.Map;

/**
 * @author lifei
 * @desc juniper-ssg 原子化实现类
 * @date 2021/11/17 18:35
 */
public class SecurityH3cv7OpImpl extends OverAllGeneratorAbstractBean {

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (isVsys) {
            sb.append("switchto context " + vsysName + "\n");
            sb.append("\n");
            sb.append("system-view\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }


    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        StringBuffer sb =new StringBuffer();
        sb.append(String.format("display object-policy ip %s \n",name));
        sb.append(String.format("object-policy ip %s \n",name));
        sb.append(String.format("undo rule %s \n",id));
        return sb.toString();
    }
}
