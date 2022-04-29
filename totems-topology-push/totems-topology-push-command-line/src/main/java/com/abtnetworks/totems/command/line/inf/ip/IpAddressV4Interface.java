package com.abtnetworks.totems.command.line.inf.ip;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:27'.
 */
public interface IpAddressV4Interface extends BasicInterface {

    /**
     * 单个 Ip
     * 192.168.215.192
     * @param singleIpArray 单个ip 集合
     * @param map
     * @param args
     * @return
     */
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception;

    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception;

    /**
     * 范围 Ip
     * 192.68.2.23-192.68.2.190
     * @param rangIpArray 范围ip 集合
     * @param map
     * @param args
     * @return
     */
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception;


    /**
     * 子网 IP mask 掩码 int 数字类型
     * @param subnetIpArray 子网ip 集合
     * @param sub sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception;

    /**
     * 子网 IP mask 掩码 str ip类型
     * @param subnetIpArray 子网ip 集合
     * @param sub sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception;

}
