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
public interface MacAddressObjectInterface extends BasicInterface {

    /**
     * mac
     * @param macIpArray mac 集合
     * @param map
     * @param args
     * @return
     */
    default String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception{
        return "";
    }


}
