package com.abtnetworks.totems.common.atomcommandline.edit.common;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/9 18:29
 */
public class EditTopsecCommon {


    public static String generateCommandline(EditCommandlineDTO dto, OverAllGeneratorAbstractBean generatorBean) {

        Integer mergeProperty = dto.getMergeProperty();
        StringBuffer editPolicy = new StringBuffer();
        StringBuilder objectStr = new StringBuilder();
        editPolicy.append(String.format("firewall policy modify id %s",dto.getSecurityPolicy().getRuleId()));

        List<String> objectNameList = new ArrayList<>();
        if(mergeProperty==0){
            List<String> srcRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), dto.getSrcIpSystem(), objectStr, dto.isCreateObjFlag(),generatorBean);
            editPolicy.append(" src '");
            if(srcRefIpAddressNames!=null && srcRefIpAddressNames.size()>0){
                objectNameList.addAll(srcRefIpAddressNames);
            }
        }
        if(mergeProperty==1){
            List<String> dstRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(), dto.getDstIpSystem(), objectStr, dto.isCreateObjFlag(),generatorBean);
            editPolicy.append(" dst '");
            if(dstRefIpAddressNames!=null && dstRefIpAddressNames.size()>0){
                objectNameList.addAll(dstRefIpAddressNames);
            }
        }
        if(mergeProperty==2){
            List<String> refServiceNames = Param4CommandLineUtils.getRefServiceNames(dto.getServiceName(), dto.getExistServiceNameList(), dto.getRestServiceList(),objectStr,generatorBean);
            editPolicy.append(" service '");
            if(refServiceNames!=null && refServiceNames.size()>0){
                objectNameList.addAll(refServiceNames);
            }
        }
        if(objectNameList!=null && objectNameList.size()>0){
            int i = 0;
            for(String objectName : objectNameList){
                i++;
                editPolicy.append(objectName);
                if(i<objectNameList.size()){
                    editPolicy.append(" ");
                }
            }
        }
        editPolicy.append("'");
        objectStr.append(editPolicy);
        return objectStr.toString();
    }
}
