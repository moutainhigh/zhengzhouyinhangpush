package com.abtnetworks.totems.command.line.basic;

import com.abtnetworks.totems.command.line.abs.CommonPolicyGenerator;
import com.abtnetworks.totems.command.line.enums.SymbolsEnum;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/6 17:19'.
 */
public class CommonBasic extends CommonPolicyGenerator {

    /**
     * static 静态实现
     * @param name 原名称
     * @return 原名称
     */
    public static String createName(String name) {
        return name;
    }

    /**
     * static 静态实现
     * @param name 原名称
     * @param number 序号
     * @return 原名称_序号
     */
    public static String createName(String name, Integer number) {
        return String.format("%s_%s", name, number);
    }

    /**
     * static 静态实现
     * @param name 原名称
     * @param str 文本
     * @return 原名称 符号 时间戳
     */
    public static String createName(String name, SymbolsEnum symbolsEnum, String str) {
        return String.format("%s%s%s", name, symbolsEnum.getValue(), str);
    }

    /**
     * static 静态实现
     * @param name 原名称
     * @param str 文本
     * @return 原名称_AO_文本
     */
    public static String createName(String name, String str) {
        return String.format("%s_AO_%s", name, str);
    }

    /**
     * 创建名称
     * @param name 原名称
     * @param map 附加参数
     * @return 最终命名
     */
    public String createName(String name, Map<String, Object> map) {
        return name;
    }

    /**
     * 创建名称
     * @param name 原名称
     * @param args 附加参数
     * @return 最终命名
     */
    public String createName(String name, String[] args) {
        return name;
    }

}
