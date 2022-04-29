package com.abtnetworks.totems.command.line.abs.service;

import com.abtnetworks.totems.command.line.inf.service.PortInterface;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:12'.
 */
public abstract class GenericPortBean extends GenericProtocolBean implements PortInterface {

    /**
     * 端口 any
     * @return
     */
    public static String generatePortAny() {
        return "any";
    }

    /**
     * 0-65535
     * @return
     */
    public static String generatePortRangeAny() {
        return "0-65535";
    }

}
