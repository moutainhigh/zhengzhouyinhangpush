package com.abtnetworks.totems.common.utils;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.Introspector;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/30 19:03
 */
public class NameUtils {

    /**
     * 首字母大写
     * @param realName
     * @return
     */
    public static String firstUpperCase(String realName) {
        return StringUtils.replaceChars(realName, realName.substring(0, 1),realName.substring(0, 1).toUpperCase());
    }

    /**
     * 首字母小写
     * @param realName
     * @return
     */
    public static String firstLowerCase(String realName) {
        return StringUtils.replaceChars(realName, realName.substring(0, 1),realName.substring(0, 1).toLowerCase());
    }

    public static String getServiceDefaultName(Class className) {
        String name = ClassUtils.getShortClassName(className);
        return Introspector.decapitalize(name);
    }
}
