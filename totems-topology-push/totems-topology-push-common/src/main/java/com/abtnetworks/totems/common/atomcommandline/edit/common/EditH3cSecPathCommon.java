package com.abtnetworks.totems.common.atomcommandline.edit.common;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;

import java.util.List;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/9 18:34
 */
public class EditH3cSecPathCommon {

    public static String generateCommandline(EditCommandlineDTO dto, OverAllGeneratorAbstractBean generatorBean) {

        Integer mergeProperty = dto.getMergeProperty();
        StringBuffer editPolicy = new StringBuffer();
        StringBuilder objectStr = new StringBuilder();
        if(dto.getModelNumber().getCode()== DeviceModelNumberEnum.H3CV5.getCode()){
            editPolicy.append("interzone source "+dto.getSrcZone()+" destination "+dto.getDstZone());
        }else{
            editPolicy.append(SendCommandStaticAndConstants.H3V7_SECURITY_POLICY);
        }
        editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
        editPolicy.append("rule "+dto.getSecurityPolicy().getRuleId());
        editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
        //虚墙
        if(dto.isVsys()){
            objectStr.append("system-view");
            objectStr.append(SendCommandStaticAndConstants.LINE_BREAK);
            objectStr.append("switchto context "+dto.getVsysName());
            objectStr.append(SendCommandStaticAndConstants.LINE_BREAK);
        }
        objectStr.append("system-view");//头部
        objectStr.append(SendCommandStaticAndConstants.LINE_BREAK);
        if(mergeProperty==0){

            List<String> srcRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), dto.getSrcIpSystem(), objectStr, dto.isCreateObjFlag(),generatorBean);
            if(srcRefIpAddressNames!=null && srcRefIpAddressNames.size()>0){
                for(String addrName : srcRefIpAddressNames){
                    editPolicy.append(String.format("source-ip %s",addrName));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }
        }
        if(mergeProperty==1){
            List<String> dstRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(), dto.getDstIpSystem(), objectStr, dto.isCreateObjFlag(),generatorBean);

            if(dstRefIpAddressNames!=null && dstRefIpAddressNames.size()>0){
                for(String addrName : dstRefIpAddressNames){
                    editPolicy.append(String.format("destination-ip %s",addrName));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }
        }
        if(mergeProperty==2){
            List<String> refServiceNames = Param4CommandLineUtils.getRefServiceNames(dto.getServiceName(), dto.getExistServiceNameList(), dto.getRestServiceList(),objectStr,generatorBean);
            if(refServiceNames!=null && refServiceNames.size()>0){
                for(String serviceName : refServiceNames){
                    editPolicy.append(String.format("service %s",serviceName));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }
        }
        editPolicy.append("quit");
        editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
        if(dto.getModelNumber().getCode()!= DeviceModelNumberEnum.H3CV5.getCode()) {
            //v7 quit三次
            editPolicy.append("return");
            objectStr.append(SendCommandStaticAndConstants.LINE_BREAK);
        }
        objectStr.append(SendCommandStaticAndConstants.LINE_BREAK);
        objectStr.append(editPolicy);
        return objectStr.toString();
    }
}
