package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityTopsecTos010020ForZZYH;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/9 17:39
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.TOPSEC_TOS_010_020, type = PolicyEnum.EDIT_SECURITY)
public class EditTopsec010ForZZYH extends EditPolicyGenerator implements PolicyGenerator {
    Logger logger = LoggerFactory.getLogger(EditTopsec010ForZZYH.class);


    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        logger.info("EditTopsec010.generateCommandline...");
        Integer mergeProperty = dto.getMergeProperty();
        StringBuffer editPolicy = new StringBuffer();
        StringBuilder objectStr = new StringBuilder();
        editPolicy.append(String.format("firewall policy modify id %s",dto.getSecurityPolicy().getRuleId()));

        List<String> objectNameList = new ArrayList<>();
        SecurityTopsecTos010020ForZZYH securityTopsecTos010020 = new SecurityTopsecTos010020ForZZYH();
        if(mergeProperty==0){
            if (dto.getRestSrcAddressList() != null && dto.getRestSrcAddressList().size() > 0) {
                List<String> restSrcAddressList = dto.getRestSrcAddressList();
                String restSrcAddressListStr = String.join(",", restSrcAddressList);
                PolicyObjectDTO srcAddress = securityTopsecTos010020.generateAddressObject(restSrcAddressListStr, "", dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType(),null);
                if (StringUtils.isNotEmpty(srcAddress.getCommandLine())) {
                    objectStr.append("define\n");
                    objectStr.append(srcAddress.getCommandLine());
                    objectStr.append("end\n");
                }
                if (StringUtils.isNotEmpty(srcAddress.getName())) {
                    List<String> nameList = Arrays.asList(srcAddress.getName().split(" "));

                    if(nameList!=null && nameList.size()>0){
                        objectNameList.addAll(nameList);
                    }
                }
            }
            editPolicy.append(" src '");
            for (String existSrcAddress : dto.getExistSrcAddressList()) {
                objectNameList.add(existSrcAddress);
            }
        }
        if(mergeProperty==1){
            if (dto.getRestDstAddressList() != null && dto.getRestDstAddressList().size() > 0) {
                List<String> restDstAddressList = dto.getRestDstAddressList();
                String restDstAddressListStr = String.join(",", restDstAddressList);
                PolicyObjectDTO dstAddress = securityTopsecTos010020.generateAddressObject(restDstAddressListStr, "", dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType(),null);
                if (StringUtils.isNotEmpty(dstAddress.getCommandLine())) {
                    objectStr.append("define\n");
                    objectStr.append(dstAddress.getCommandLine());
                    objectStr.append("end\n");
                }
                if (StringUtils.isNotEmpty(dstAddress.getName())) {
                    List<String> nameList = Arrays.asList(dstAddress.getName().split(" "));
                    if(nameList!=null && nameList.size()>0){
                        objectNameList.addAll(nameList);
                    }
                }
            }
            editPolicy.append(" dst '");
            for (String existDstAddress : dto.getExistDstAddressList()) {
                objectNameList.add(existDstAddress);
            }

        }
        if(mergeProperty==2){
            if (dto.getRestServiceList() != null && dto.getRestServiceList().size() > 0) {
                PolicyObjectDTO service = securityTopsecTos010020.generateServiceObject(dto.getRestServiceList(), dto.getServiceName(),null);
                if (StringUtils.isNotEmpty(service.getCommandLine())) {
                    objectStr.append("define\n");
                    objectStr.append(service.getCommandLine());
                    objectStr.append("end\n");
                }
                if(service !=null && service.getJoin() != null){
                    objectNameList.add(service.getJoin());
                }
            }
            editPolicy.append(" service '");
            for (String existServiceName : dto.getExistServiceNameList()) {
                objectNameList.add(existServiceName);
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



    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }
}
