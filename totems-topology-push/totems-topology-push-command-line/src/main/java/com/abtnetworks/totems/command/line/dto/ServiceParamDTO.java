package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/4/25
 */
@Data
public class ServiceParamDTO {

    /**
     * 协议
     */
    ProtocolTypeEnum protocol;

    /**
     * 协议附加type
     */
    String[] protocolAttachTypeArray;

    /**
     * 协议附加code
     */
    String[] protocolAttachCodeArray;

    //源端口

    /**
     * 源端口： 数字 单个端口类型
     */
    Integer[] srcSinglePortArray;

    /**
     * 源端口： Str 单个端口类型
     */
    String[] srcSinglePortStrArray;

    /**
     * 源端口：数字 范围端口类型
     */
    PortRangeDTO[] srcRangePortArray;

    //目的端口

    /**
     * 目的端口： 数字 单个端口类型
     */
    Integer[] dstSinglePortArray;

    /**
     * 目的端口： Str 单个端口类型
     */
    String[] dstSinglePortStrArray;

    /**
     * 目的端口：数字 范围端口类型
     */
    PortRangeDTO[] dstRangePortArray;

    //其他

    /**
     * 超时时间
     */
    String[] timeOutArray;


}
