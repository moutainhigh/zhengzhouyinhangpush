package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityH3cSecPathV7ForZZYH;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/8 9:36
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.EDIT_SECURITY)
public class EditH3cSecPathV7ForZZYH extends EditPolicyGenerator implements PolicyGenerator {

    Logger logger = LoggerFactory.getLogger(EditH3cSecPathV7ForZZYH.class);


    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        logger.info("EditH3cSecPathV7.generateCommandline...");


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
        SecurityH3cSecPathV7ForZZYH securityH3cSecPathV7 = new SecurityH3cSecPathV7ForZZYH();
        if(mergeProperty==0){
            if (dto.getRestSrcAddressList() != null && dto.getRestSrcAddressList().size() > 0) {
                List<String> restSrcAddressList = dto.getRestSrcAddressList();
                String restSrcAddressListStr = String.join(",", restSrcAddressList);
                PolicyObjectDTO policyObjectDTO = securityH3cSecPathV7.generateAddressObject(restSrcAddressListStr, dto.getName(), dto.getSrcAddressName(), "source-ip", dto.isCreateObjFlag(), dto.getSrcIpSystem(), dto.getDescription());
                objectStr.append(policyObjectDTO.getCommandLine());
                if(policyObjectDTO !=null && policyObjectDTO.getName() != null){
                    editPolicy.append(String.format("source-ip %s", policyObjectDTO.getName()));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }

            for (String existSrcName : dto.getExistSrcAddressList()) {
                editPolicy.append(String.format("source-ip %s", existSrcName));
                editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
            }
        }
        if(mergeProperty==1){
            if (dto.getRestDstAddressList() != null && dto.getRestDstAddressList().size() > 0) {
                List<String> restDstAddressList = dto.getRestDstAddressList();
                String restDstAddressListStr = String.join(",", restDstAddressList);
                PolicyObjectDTO policyObjectDTO = securityH3cSecPathV7.generateAddressObject(restDstAddressListStr, dto.getName(), dto.getDstAddressName(), "destination-ip", dto.isCreateObjFlag(), dto.getDstIpSystem(), dto.getDescription());
                objectStr.append(policyObjectDTO.getCommandLine());

                if(policyObjectDTO !=null && policyObjectDTO.getName() != null){
                    editPolicy.append(String.format("source-ip %s", policyObjectDTO.getName()));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }

            for (String existDstName : dto.getExistDstAddressList()) {
                editPolicy.append(String.format("source-ip %s", existDstName));
                editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
            }

        }
        if(mergeProperty==2){
            if (dto.getRestServiceList() != null && dto.getRestServiceList().size() > 0) {
                PolicyObjectDTO policyObjectDTO = securityH3cSecPathV7.generateServiceObject(dto.getRestServiceList(), dto.getServiceName(), dto.isCreateObjFlag(), dto.getIpType());
                objectStr.append(policyObjectDTO.getCommandLine());
                if(policyObjectDTO != null && policyObjectDTO.getName() != null){
                    editPolicy.append(String.format("service %s",policyObjectDTO.getName()));
                    editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
                }
            }

            for (String existServiceName : dto.getExistServiceNameList()) {
                editPolicy.append(String.format("service %s",existServiceName));
                editPolicy.append(SendCommandStaticAndConstants.LINE_BREAK);
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

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }

}
