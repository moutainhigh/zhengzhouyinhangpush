package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityVenustechVSOS;
import com.abtnetworks.totems.common.commandline.security.SecurityVenustechVSOSV263;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EditVenustechVSOSV263 extends EditPolicyGenerator implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO dto = new EditCommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        //入接口出接口
        dto.setSrcZone(policyDTO.getSrcZone());
        dto.setDstZone(policyDTO.getDstZone());
        //获取策略名
        if (!AliStringUtils.isEmpty(policyDTO.getEditPolicyName())) {
            dto.setName(policyDTO.getEditPolicyName());
        }
        //获取原策略数据
        dto.setSecurityPolicy(policyDTO.getSecurityPolicy());
        //获取已存在和还需创建数据
        PolicyMergeDTO mergeDTO = new PolicyMergeDTO();
        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        TaskDTO task = cmdDTO.getTask();
        String theme = task.getTheme();
        dto.setBusinessName(theme);
        if (ObjectUtils.isNotEmpty(policyDTO.getMergeProperty())) {
            if (policyDTO.getMergeProperty() == 0) {
                dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
                dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
                mergeDTO.setMergeField(PolicyMergePropertyEnum.MERGE_SRC_IP.getKey());
                dto.setMergeDTO(mergeDTO);
            }
            if (policyDTO.getMergeProperty() == 1) {
                dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
                dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
                mergeDTO.setMergeField(PolicyMergePropertyEnum.MERGE_DST_IP.getKey());

                dto.setMergeDTO(mergeDTO);
            }
            if (policyDTO.getMergeProperty() == 2) {
                dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
                dto.setRestServiceList(existObjectDTO.getRestServiceList());
                mergeDTO.setMergeField(PolicyMergePropertyEnum.MERGE_SERVICE.getKey());
                dto.setMergeDTO(mergeDTO);
            }
            DeviceDTO device = cmdDTO.getDevice();
            if (device.isVsys()) {
                dto.setVsys(true);
            }
        } else {
            log.error("无需合并的值");
            return null;
        }
        return composite(dto);
    }

    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    private String editCommandLine(EditCommandlineDTO dto) {
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        SecurityVenustechVSOSV263 securityVenustechVSOSV263 = new SecurityVenustechVSOSV263();
        return securityVenustechVSOSV263.generatePreCommandline(dto1) + securityVenustechVSOSV263.createMergeCommandLine(dto1);
    }

    private String createCommandLine(EditCommandlineDTO dto) {
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        dto1.setPolicyId(dto.getSecurityPolicy().getRuleId());
        SecurityVenustechVSOSV263 securityVenustechVSOSV263 = new SecurityVenustechVSOSV263();
        return securityVenustechVSOSV263.generatePreCommandline(dto1) + securityVenustechVSOSV263.generateCommandline(dto1);
    }
}
