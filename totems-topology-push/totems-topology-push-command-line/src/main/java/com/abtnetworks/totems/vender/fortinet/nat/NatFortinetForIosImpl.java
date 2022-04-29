package com.abtnetworks.totems.vender.fortinet.nat;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;

import java.util.Map;

/**
 * @author lifei
 * @desc nat策略 命令行
 * @date 2021/7/1 20:46
 */
public class NatFortinetForIosImpl extends SecurityFortinetImpl {

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
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end\n";
    }
}
