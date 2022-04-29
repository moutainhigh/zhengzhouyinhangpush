package com.abtnetworks.totems.command.line.abs.service;

import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.inf.service.ServiceObjectGroupInterface;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:12'.
 */
public abstract class GenericServiceObjectGroup extends GenericServiceObject implements ServiceObjectGroupInterface {

    /**
     * 生成服务组名
     * @param serviceParamList
     * @param args
     * @return
     */
    public String createServiceObjectName(List<ServiceParamDTO> serviceParamList,Map<String, Object> map, String[] args) {
        if(CollectionUtils.isNotEmpty(serviceParamList) && serviceParamList.size() == 1){
            return this.createServiceObjectName(serviceParamList.get(0),null,null);
        }
        if(CollectionUtils.isEmpty(serviceParamList)){
            return "any";
        }
        return this.createServiceObjectGroupName(serviceParamList,null,null,null,null);
    }

    /**
     * 生成服务组名
     * @param serviceParamList
     * @param serviceObjectNameRefArray
     * @param serviceObjectGroupNameRefArray
     * @param args
     * @return
     */
    public String createServiceObjectGroupName(List<ServiceParamDTO> serviceParamList,
                                               String[] serviceObjectNameRefArray,
                                               String[] serviceObjectGroupNameRefArray,Map<String, Object> map, String[] args) {
        if(CollectionUtils.isEmpty(serviceParamList)){
            if(ArrayUtils.getLength(serviceObjectNameRefArray) + ArrayUtils.getLength(serviceObjectGroupNameRefArray) == 1){
                if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
                    return serviceObjectNameRefArray[0];
                } else {
                    return serviceObjectGroupNameRefArray[0];
                }
            }
        }
        StringBuffer serviceGroupName = new StringBuffer("sg_");
        int num = 0;
        if(serviceParamList != null){
            num += serviceParamList.hashCode();
        }
        if(serviceObjectNameRefArray != null){
            num += Arrays.hashCode(serviceObjectNameRefArray);
        }
        if(serviceObjectGroupNameRefArray != null){
            num += Arrays.hashCode(serviceObjectGroupNameRefArray);
        }
        return "sg_"+Math.abs(num);
    }
}
