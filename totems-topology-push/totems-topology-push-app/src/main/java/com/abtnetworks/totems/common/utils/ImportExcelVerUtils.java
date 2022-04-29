package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.ProtocolEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入excel数据校验工具类
 * @author luwei
 * @date 2020/1/8
 */
@Slf4j
public class ImportExcelVerUtils {

    public final static String IP_TYPE_IPV4 = "IPV4";
    public final static String IP_TYPE_IPV6 = "IPV6";
    public final static String IP_TYPE_IPV46 = "IPV46";


    /**
     * 检测协议字符串是否为tcp，udp，icmp或者any之一
     * @param protocol 协议字符串
     * @return 是则返回true，否则返回false
     */
    public static boolean isValidProtocol(String protocol) {
        if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            return true;
        }
        return false;
    }

    /***导入时，服务正则校验**/
    public static boolean serviceReg(String content) {
        content = content.toUpperCase();
        String regex = "^(TCP|UDP):([\\d]*[\\d\\,\\-]*[\\d])$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        return m.matches();
    }

    /**将本来以换行标识的IP改为以逗号关联的字符串**/
    public static String ipJoin(String ip) {
        String[] ipAddresses = ip.split("\n");
        StringBuilder ipSb = new StringBuilder();
        for (String address : ipAddresses) {
            address = address.trim();
            if (StringUtils.isBlank(address)) {
                continue;
            }
            ipSb.append(",");
            ipSb.append(address);
        }
        if (ipSb.length() > 0) {
            ipSb.deleteCharAt(0);
        }

        ip = ipSb.toString();
        return ip;
    }

    /**
     * 校验IP v4、v6
     * @param ip  详细IP地址
     * @param ipTitle  标题，用来提示错误信息区分的
     * @return
     */
    public static ResultRO<String> checkIpByIpv46(String ip, String ipTitle, String ipType){
        if (StringUtils.isBlank(ip)) {
            return new ResultRO<>(false, ipTitle + "不能为空！");
        }

        if (StringUtils.isBlank(ipType)) {
            return new ResultRO<>(false, "IP类型不能为空！");
        }

        ip = ipJoin(ip);
        boolean success = true;
        String errorMsg = "";

        //验证通过的IP
        String verIp ="";
        Integer rc = null;
        if (IP_TYPE_IPV4.equals(ipType)) {
            rc = InputValueUtils.checkIp(ip);
            //若出IP范围起始地址大于终止地址错误，则自动纠正
            if (rc == ReturnCode.INVALID_IP_RANGE) {
                verIp = InputValueUtils.autoCorrect(ip);
            } else if (rc != ReturnCode.POLICY_MSG_OK) {
                success = false;
                errorMsg = "IP类型为：" + ipType + "，" + ipTitle + "格式错误，只能为IP、IP范围或者IP网段！";
            }
        } else if (IP_TYPE_IPV6.equals(ipType)) {
            rc = InputValueUtils.checkIpV6(ip);
            if (rc == ReturnCode.INVALID_IP_RANGE) {
                String[] ipv6Arr = ip.split("-");
                verIp = ipv6Arr[1]+"-"+ipv6Arr[0];
            } else if (rc != ReturnCode.POLICY_MSG_OK) {
                success = false;
                errorMsg = "IP类型为：" + ipType + "，" + ipTitle + "格式错误，只能为IP、IP范围或者IP网段！";
            }
        } else if (IP_TYPE_IPV46.equals(ipType)) {
            rc = InputValueUtils.checkIp(ip);
            //若出IP范围起始地址大于终止地址错误，则自动纠正
            if (rc == ReturnCode.INVALID_IP_RANGE) {
                verIp = InputValueUtils.autoCorrect(ip);
            } else if (rc != ReturnCode.POLICY_MSG_OK) {
                rc = InputValueUtils.checkIpV6(ip);
                if (rc == ReturnCode.INVALID_IP_RANGE) {
                    String[] ipv6Arr = ip.split("-");
                    verIp = ipv6Arr[1] + "-" + ipv6Arr[0];
                } else if (rc != ReturnCode.POLICY_MSG_OK) {
                    success = false;
                    errorMsg = "IP类型为：" + ipType + "，" + ipTitle + "格式错误，只能为IP、IP范围或者IP网段！";
                }
            }
        } else {
            return new ResultRO<>(false, "IP类型未知");
        }

        if (StringUtils.isBlank(verIp)) {
            verIp = ip;
        }
        //修正无用的空字符
        verIp = InputValueUtils.formatIpAddress(verIp);
        ResultRO<String> resultRO = new ResultRO<>(success);
        if(StringUtils.isNoneBlank(verIp)){
            resultRO.setData(verIp);
        }
        if(StringUtils.isNoneBlank(errorMsg)){
            resultRO.setMessage(errorMsg);
        }
        return resultRO;
    }


    /**
     * 服务做处理、转换，返回转换后的信息，有错误则返回错误信息
     **/
    public static ResultRO<List<ServiceDTO>> getServiceList(String service,String isipv6) {
        if (StringUtils.isBlank(service)) {
            return new ResultRO<>(false);
        }

        boolean success = true;
        List<String> errorMsg = new ArrayList<>();

        //以换行分割
        String[] serviceArr = service.split("\n");

        List<ServiceDTO> serviceList = new ArrayList<>();
        Map<String, String> serviceMap = new HashMap<>();
        for (String ser : serviceArr) {
            ser = ser.trim();
            if (StringUtils.isBlank(ser)) {
                continue;
            }
            ServiceDTO serviceDTO = new ServiceDTO();
            String[] protocolPortArr = ser.split(":");
            String serviceName = protocolPortArr[0].trim();
            if (!isValidProtocol(serviceName)) {
                log.error("服务错误：" + serviceName);
                success = false;
                errorMsg.add("服务格式错误！只能为TCP，UDP，或者ICMP！");
                break;
            }
            if (serviceMap.get(serviceName) != null) {
                log.error("服务错误，存在重复的协议,protocol:{},service:{} ", serviceName, service);
                success = false;
                errorMsg.add("协议类型不能重复！");
                break;
            } else {
                serviceMap.put(serviceName, "");
            }
            if(isipv6.equalsIgnoreCase(IP_TYPE_IPV6)&&serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
                serviceDTO.setProtocol(ProtocolEnum.ICMPV6.getCode());
            }
            else if(serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
            } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
            } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
            }
            if (protocolPortArr.length > 1) {
                String ports = protocolPortArr[1].trim();
                //校验
                if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) || serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    boolean serviceFlag = serviceReg(ser);
                    if (!serviceFlag) {
                        log.error("服务格式错误:" + ser);
                        success = false;
                        errorMsg.add("服务格式错误！");
                        break;
                    }
                } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    log.error("服务格式错误:" + ser + ",icmp不需要端口信息");
                    success = false;
                    errorMsg.add("服务格式错误！icmp不需要端口信息");
                    break;
                }
                if (!PortUtils.isValidPortString(ports)) {
                    success = false;
                    errorMsg.add("端口不合法，只能为0-65535的纯数字，或者数字范围！");
                    break;
                }
                serviceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(ports));
            }
            serviceList.add(serviceDTO);
        }


        ResultRO<List<ServiceDTO>> resultRO = new ResultRO<>(success);
        if (errorMsg.size() > 0) {
            resultRO.setMessage(JSONObject.toJSONString(errorMsg));
        }
        if (serviceList.size() > 0) {
            resultRO.setData(serviceList);
        }
        return resultRO;
    }
}
