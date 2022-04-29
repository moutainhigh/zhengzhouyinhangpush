package com.abtnetworks.totems.command.line.inf.ip;

import com.abtnetworks.totems.command.line.dto.IpAddressMacDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/3
 */
public interface MacAddressObjectGroupInterface extends BasicInterface {

    /**
     * 生成Mac地址组
     * @param statusTypeEnum
     * @param name
     * @param macIpArray
     * @param macObjectNameRefArray
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generateMacAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, String[] macObjectNameRefArray, Map<String, Object> map, String[] args)throws Exception{
        return "";
    }

}
