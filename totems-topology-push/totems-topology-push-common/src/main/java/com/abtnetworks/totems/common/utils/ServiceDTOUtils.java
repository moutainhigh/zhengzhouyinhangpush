package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServiceDTOUtils {

    /**
     * 将服务对象JSON字符串转换成ServiceDTO数组
     * @param serviceString 服务JSON字符串
     * @return ServiceDTO数组
     */
    public static List<ServiceDTO> toList(String serviceString) {
        if(AliStringUtils.isEmpty(serviceString)) {
            return null;
        }

        JSONArray array = JSONArray.parseArray(serviceString);
        return array.toJavaList(ServiceDTO.class);
    }

    /**
     * 将ServiceDTO数组转换成字符串形式存储
     * @param list ServiceDTO数组
     * @return JSON格式字符串
     */
    public static String toString(List<ServiceDTO> list) {
        return JSONObject.toJSONString(list);
    }

    public static void main(String[] args){
        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO tcpServiceDTO = new ServiceDTO();
        tcpServiceDTO.setProtocol("6");
        tcpServiceDTO.setSrcPorts("80,90,100-110");
        tcpServiceDTO.setDstPorts("8080,8090,8100-8110");

        ServiceDTO udpServiceDTO = new ServiceDTO();
        udpServiceDTO.setProtocol("17");
        udpServiceDTO.setSrcPorts("5060,5070,5080-5090");
        udpServiceDTO.setDstPorts("54000,54100,54200-54211");

        ServiceDTO icmpServiceDTO = new ServiceDTO();
        icmpServiceDTO.setProtocol("1");

        serviceList.add(tcpServiceDTO);
        serviceList.add(udpServiceDTO);
        serviceList.add(icmpServiceDTO);
        String serviceString = ServiceDTOUtils.toString(serviceList);
        System.out.println(serviceString);

        List<ServiceDTO> list = ServiceDTOUtils.toList(serviceString);
        System.out.println(JSONObject.toJSONString(list));

    }
}
