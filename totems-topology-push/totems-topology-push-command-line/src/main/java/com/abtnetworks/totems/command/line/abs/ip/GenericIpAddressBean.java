package com.abtnetworks.totems.command.line.abs.ip;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:12'.
 */
public abstract class GenericIpAddressBean extends GenericIpAddressObjectGroup {

    /**
     * ipv4 any
     * @return
     */
    public static String generateIpV4any() {
        return "any";
    }

    /**
     * ipv4 Any
     * @return
     */
    public static String generateIpV4Any() {
        return "Any";
    }

    /**
     * ipv4 ANY
     * @return
     */
    public static String generateIpV4ANY() {
        return "ANY";
    }

    /**
     * ipv4 0.0.0.0/0
     * @return
     */
    public static String generateIpV4Str0Any() {
        return "0.0.0.0/0";
    }

    /**
     * ipv6 any6
     * @return
     */
    public static String generateIpV6any() {
        return "any6";
    }

    /**
     * ipv6 ::/0
     * @return
     */
    public static String generateIpV6Str0Any() {
        return "::/0";
    }

}
