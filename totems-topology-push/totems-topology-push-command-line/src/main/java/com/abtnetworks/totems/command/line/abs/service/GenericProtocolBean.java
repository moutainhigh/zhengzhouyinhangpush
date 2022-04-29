package com.abtnetworks.totems.command.line.abs.service;

import com.abtnetworks.totems.command.line.abs.ip.GenericIpAddressBean;
import com.abtnetworks.totems.command.line.inf.service.ProtocolInterface;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:12'.
 */
public abstract class GenericProtocolBean extends GenericIpAddressBean implements ProtocolInterface {

    /**
     * 协议 any
     * @return
     */
    public static String generateProtocolAny() {
        return "any";
    }

    /**
     * 协议 any 范围 0-255
     * @return
     */
    public static String generateProtocolRangeAny() {
        return "0-255";
    }



}
