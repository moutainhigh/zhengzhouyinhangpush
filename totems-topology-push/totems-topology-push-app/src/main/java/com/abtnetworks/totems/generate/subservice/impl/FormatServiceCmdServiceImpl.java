package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 该类用于格式化服务数据，使其能在命令行生成中不报空指针错误
 * @Author Wen Jiachang
 */
@Service
public class FormatServiceCmdServiceImpl implements CmdService {

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        policyDTO.setServiceList(format(policyDTO.getServiceList()));
        policyDTO.setPostServiceList(format(policyDTO.getPostServiceList()));
    }

    List<ServiceDTO> format(List<ServiceDTO> serviceList) {
        if(serviceList != null && serviceList.size() > 0) {
            for (ServiceDTO serviceDTO : serviceList) {
                if (AliStringUtils.isEmpty(serviceDTO.getProtocol())) {
                    serviceDTO.setProtocol("0");
                }

                if (AliStringUtils.isEmpty(serviceDTO.getDstPorts()) || serviceDTO.getDstPorts().equals(PolicyConstants.PORT_ANY)) {
                    serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }

                if (AliStringUtils.isEmpty(serviceDTO.getSrcPorts()) || serviceDTO.getSrcPorts().equals(PolicyConstants.PORT_ANY)) {
                    serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }
            }
        } else {
            serviceList = new ArrayList<>();
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol("0");
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceList.add(serviceDTO);

        }
        return serviceList;
    }
}
