package com.abtnetworks.totems.command.line.inf.service;

import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 *
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:27'.
 */
public interface ProtocolInterface extends BasicInterface {

    /**
     * 生成 ICMP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                          Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                          String[] timeOutArray, String[] objectNameRefArray,
                                          Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成 ICMP6 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成 TCP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray,
                                         Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成 UDP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray,
                                         Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成 TCP_UDP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateTCP_UDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                             Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                             Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                             String[] timeOutArray, String[] objectNameRefArray,
                                             Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成 其他协议 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

}
