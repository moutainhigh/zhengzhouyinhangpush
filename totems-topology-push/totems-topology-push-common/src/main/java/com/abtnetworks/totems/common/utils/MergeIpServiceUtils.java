package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.IP6ValueDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.push.dto.policy.IpValueDTO;
import com.abtnetworks.totems.push.dto.policy.PortValueDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/7/13
 */
public class MergeIpServiceUtils {

    /**
     * 合并ipv46
     *
     * @param ipSet
     * @param taskType
     * @return
     */
    public static Set<String> mergeIp(Set<String> ipSet, Integer taskType) {
        Set<String> mergedIpSet = new HashSet<>();

        for (String newIp : ipSet) {
            if (mergedIpSet.size() == 0) {
                mergedIpSet.add(newIp);
                continue;
            }

            Set<String> removeSet = new HashSet<>();
            boolean addIp = true;
            for (String ip : mergedIpSet) {
                if (IpTypeEnum.IPV6.getCode().equals(taskType)) {
                    IP6ValueDTO newIp6Value = new IP6ValueDTO(newIp);
                    IP6ValueDTO mergedIp6Value = new IP6ValueDTO(ip);

                    if (mergedIp6Value.getStart().compareTo(newIp6Value.getStart()) <= 0 && mergedIp6Value.getEnd().compareTo(newIp6Value.getEnd()) >= 0) {
                        addIp = false;
                    } else if (mergedIp6Value.getStart().compareTo(newIp6Value.getStart()) >= 0 && newIp6Value.getEnd().compareTo(mergedIp6Value.getEnd()) >= 0) {
                        removeSet.add(ip);
                    } else {

                    }
                } else {
                    IpValueDTO newIpValue = new IpValueDTO(newIp);
                    IpValueDTO mergedIpValue = new IpValueDTO(ip);

                    if (mergedIpValue.getStart() <= newIpValue.getStart() && mergedIpValue.getEnd() >= newIpValue.getEnd()) {
                        addIp = false;
                    } else if (mergedIpValue.getStart() >= newIpValue.getStart() && newIpValue.getEnd() >= mergedIpValue.getEnd()) {
                        removeSet.add(ip);
                    } else {

                    }
                }
            }

            if (addIp) {
                mergedIpSet.removeAll(removeSet);
                mergedIpSet.add(newIp);
            }
        }

        return mergedIpSet;
    }


    /**
     * 合并服务端口
     *
     * @param portListString
     * @param newPortListString
     * @return
     */
    public static String mergePort(String portListString, String newPortListString) {
        //若有一个端口为null，则说明端口为any，即包含所有端口
        if (portListString == null || newPortListString == null) {
            return null;
        }

        String[] portStrings = portListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        String[] newPortStrings = newPortListString.split(PolicyConstants.ADDRESS_SEPERATOR);

        List<PortValueDTO> portValueList = new ArrayList<>();
        List<PortValueDTO> newPortValueList = new ArrayList<>();

        for (String portString : portStrings) {
            PortValueDTO portValue = new PortValueDTO(portString);
            portValueList.add(portValue);
        }

        for (String newPortString : newPortStrings) {
            PortValueDTO portValue = new PortValueDTO(newPortString);
            newPortValueList.add(portValue);
        }

        for (PortValueDTO newPortValue : newPortValueList) {
            List<PortValueDTO> mergedPortList = new ArrayList<>();
            for (PortValueDTO portValue : portValueList) {
                if (newPortValue.getEnd() < (portValue.getStart() - 1)) {
                    //新端口的终止值小于源端口的起始值，不合并
                } else if ((newPortValue.getStart() - 1) > portValue.getEnd()) {
                    //新端口的起始值大于源端口的终止值，不合并
                } else {
                    //需要合并

                    //起始值谁小用谁
                    if (portValue.getStart() < newPortValue.getStart()) {
                        newPortValue.setStart(portValue.getStart());
                    }

                    //终止值谁大用谁
                    if (portValue.getEnd() > newPortValue.getEnd()) {
                        newPortValue.setEnd(portValue.getEnd());
                    }

                    //被合并值加入到移除队列
                    mergedPortList.add(portValue);
                }
            }
            //移除被合并过的端口
            portValueList.removeAll(mergedPortList);
            //添加新端口
            portValueList.add(newPortValue);
        }

        StringBuilder sb = new StringBuilder();
        for (PortValueDTO port : portValueList) {
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(port.toString());
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }


}
