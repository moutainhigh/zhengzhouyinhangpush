package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/14 9:56
 */
public class WhaleUtils {

    private static Logger logger = LoggerFactory.getLogger(WhaleUtils.class);

    /**
     * 根据起始数字获取SrcDstStringDTO对象
     * TODO:后续优化到工具类
     *
     * @param start 开始数据
     * @param end 终止数据
     * @return SrcDstStringDTO对象
     */
    public static SrcDstStringDTO getSrcDstStringDTO(String start, String end) {
        SrcDstStringDTO srcDstStringDTO = new SrcDstStringDTO();
        srcDstStringDTO.setStart(start);
        srcDstStringDTO.setEnd(end);
        return srcDstStringDTO;
    }


    /**
     * 转换端口为SrcDstIntegerDTO对象列表
     * TODO:后续优化到工具类
     *
     * @param ports 端口
     * @return SrcDstIntegerDTO对象列表
     */
    public static List<SrcDstIntegerDTO> getSrcDstIntegerDTOList(String ports) {

        if(AliStringUtils.isEmpty(ports)) {
            ports = PolicyConstants.POLICY_STR_VALUE_ANY;
        }

        List<SrcDstIntegerDTO> srcDstIntegerList = new ArrayList<>();
        if (ports.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            int start = 0;
            int end = 65535;
            SrcDstIntegerDTO srcDstIntegerDTO = new SrcDstIntegerDTO();
            srcDstIntegerDTO.setStart(start);
            srcDstIntegerDTO.setEnd(end);
            srcDstIntegerList.add(srcDstIntegerDTO);
            return srcDstIntegerList;
        }

        String[] portList = ports.split(",");

        for (String port : portList) {

            String[] pair = port.split("-");
            if (pair.length > 2) {
                logger.error("invalid port format:" + port);
                return null;
            }
            if(pair.length == 2) {
                try {
                    int start = Integer.parseInt(pair[0]);
                    int end = Integer.parseInt(pair[1]);
                    SrcDstIntegerDTO srcDstIntegerDTO = new SrcDstIntegerDTO();
                    srcDstIntegerDTO.setStart(start);
                    srcDstIntegerDTO.setEnd(end);
                    srcDstIntegerList.add(srcDstIntegerDTO);
                } catch (Exception e) {
                    logger.error("parse ports failed:" + e.getMessage());
                    return null;
                }
            } else if (pair.length == 1) {
                int start = Integer.parseInt(pair[0]);
                int end = start;
                SrcDstIntegerDTO srcDstIntegerDTO = new SrcDstIntegerDTO();
                srcDstIntegerDTO.setStart(start);
                srcDstIntegerDTO.setEnd(end);
                srcDstIntegerList.add(srcDstIntegerDTO);
            }
        }
        return srcDstIntegerList;
    }


    /**
     * 解析协议号
     * TODO:后续优化到工具类
     *
     * @param protocol 协议号字符串
     * @return 协议号数字，若解析失败，则返回-2;
     */
    public static int getProtocolNum(String protocol) {
        int num = -2;
        try {
            num = Integer.parseInt(protocol);
        } catch (Exception e) {
            logger.error(String.format("解析协议号(%s)失败", protocol), e);
        }
        return num;
    }
}
