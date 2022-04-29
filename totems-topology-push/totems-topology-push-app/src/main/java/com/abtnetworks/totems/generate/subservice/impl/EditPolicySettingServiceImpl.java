package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.push.manager.PolicyMergeNewTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @Title:
 * @Description: 编辑策略生成设置
 * @date 2021/3/17
 */
@Slf4j
@Service
public class EditPolicySettingServiceImpl implements CmdService {

    @Resource
    PolicyMergeNewTaskService policyMergeNewTaskService;
    
    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        DeviceDTO device = cmdDto.getDevice();
        Boolean isDisasterDevice = device.getIsDisasterDevice();
        if(isDisasterDevice != null && isDisasterDevice){
            log.info("说明是灾备设备，不在走合并那一套逻辑");
            return;
        }
        policyMergeNewTaskService.mergePolicyForGenerateParam(cmdDto);
    }
}
