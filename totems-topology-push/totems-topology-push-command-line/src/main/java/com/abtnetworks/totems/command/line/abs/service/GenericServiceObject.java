package com.abtnetworks.totems.command.line.abs.service;

import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.inf.service.ServiceObjectInterface;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:12'.
 */
public abstract class GenericServiceObject extends GenericPortBean implements ServiceObjectInterface {

    public String createServiceObjectName(ServiceParamDTO serviceParamDTO, Map<String, Object> map, String[] args) {
        if(serviceParamDTO == null || serviceParamDTO.getProtocol() == null){
            return StringUtils.EMPTY;
        }
        StringBuffer serviceObjectName = new StringBuffer();
        if(serviceParamDTO.getProtocol().getType().equalsIgnoreCase("icmp") || serviceParamDTO.getProtocol().getType().equalsIgnoreCase("icmp6") || serviceParamDTO.getProtocol().getType().equalsIgnoreCase("any")){
            return serviceParamDTO.getProtocol().getType();
        }
        serviceObjectName.append(serviceParamDTO.getProtocol().getType());
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortArray())){
            for (Integer dstPort : serviceParamDTO.getDstSinglePortArray()) {
                serviceObjectName.append(dstPort);
            }
        } else if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortStrArray())){
            for (String dstPortStr : serviceParamDTO.getDstSinglePortStrArray()) {
                serviceObjectName.append(dstPortStr);
            }
        } else if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstRangePortArray())){
            for (PortRangeDTO portRangeDTO : serviceParamDTO.getDstRangePortArray()) {
                serviceObjectName.append(String.format("%s-%s",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        } else {
            serviceObjectName.append(String.format("_%s", getServiceParamHashCode(serviceParamDTO)));
        }
        return serviceObjectName.toString();
    }

    public String createServiceObjectName(ServiceParamDTO serviceParamDTO, Map<String, Object> map) {
        if(serviceParamDTO == null || serviceParamDTO.getProtocol() == null){
            return StringUtils.EMPTY;
        }
        StringBuffer serviceObjectName = new StringBuffer();
        serviceObjectName.append(serviceParamDTO.getProtocol().getType().toUpperCase());
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortArray())){
            for (Integer dstPort : serviceParamDTO.getDstSinglePortArray()) {
                serviceObjectName.append(dstPort);
            }
        } else if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortStrArray())){
            for (String dstPortStr : serviceParamDTO.getDstSinglePortStrArray()) {
                serviceObjectName.append(dstPortStr);
            }
        } else if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstRangePortArray())){
            for (PortRangeDTO portRangeDTO : serviceParamDTO.getDstRangePortArray()) {
                serviceObjectName.append(String.format("%s-%s",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        } else {
            serviceObjectName.append(String.format("_%s", getServiceParamHashCode(serviceParamDTO)));
        }
        return serviceObjectName.toString();
    }

    public int getServiceParamHashCode(ServiceParamDTO serviceParamDTO){
        if(serviceParamDTO == null){
            return 0;
        }
        int hashNum = 0;
        if(serviceParamDTO.getProtocol() != null){
            hashNum += serviceParamDTO.getProtocol().name().hashCode();
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getProtocolAttachTypeArray())){
            for (String protocolType : serviceParamDTO.getProtocolAttachTypeArray()) {
                hashNum += protocolType.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getProtocolAttachCodeArray())){
            for (String protocolCode : serviceParamDTO.getProtocolAttachTypeArray()) {
                hashNum += protocolCode.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getSrcSinglePortArray())){
            for (Integer singlePort : serviceParamDTO.getSrcSinglePortArray()) {
                hashNum += singlePort;
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getSrcSinglePortStrArray())){
            for (String singlePortStr : serviceParamDTO.getSrcSinglePortStrArray()) {
                hashNum += singlePortStr.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getSrcRangePortArray())){
            for (PortRangeDTO portRangeDTO : serviceParamDTO.getSrcRangePortArray()) {
                hashNum += (portRangeDTO.getStart() + portRangeDTO.getEnd());
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortArray())){
            for (Integer singlePort : serviceParamDTO.getDstSinglePortArray()) {
                hashNum += singlePort;
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortStrArray())){
            for (String singlePortStr : serviceParamDTO.getDstSinglePortStrArray()) {
                hashNum += singlePortStr.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstRangePortArray())){
            for (PortRangeDTO portRangeDTO : serviceParamDTO.getDstRangePortArray()) {
                hashNum += (portRangeDTO.getStart() + portRangeDTO.getEnd());
            }
        }
        return Math.abs(hashNum);
    }

}
