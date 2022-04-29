package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import com.abtnetworks.totems.common.enums.ConnectTypeEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.push.manager.PolicyMergeNewTaskService;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @Title:
 * @Description: 移动的数据顺序在查询策略id后面
 * @date 2020/12/29
 */
@Slf4j
@Service
public class GetSettingMoveCmdServiceImpl implements CmdService {
    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    PolicyMergeNewTaskService policyMergeNewTaskService;

    @Resource
    CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        SettingDTO settingDTO = cmdDTO.getSetting();
        com.abtnetworks.totems.common.dto.DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        Integer idleTime = cmdDTO.getPolicy().getIdleTimeout();

        TaskDTO task = cmdDTO.getTask();
        Boolean beforeConflict = null  == task.getBeforeConflict() ? false : task.getBeforeConflict();
        String swaNameId = cmdDTO.getSetting().getSwapNameId();
        MoveSeatEnum moveSeatEnum = cmdDTO.getSetting().getMoveSeatEnum();
        // 满足这三个条件证明已经走了前面流程的移动冲突策略的流程了，这里就直接return 不走高级设置的逻辑
        if(beforeConflict && StringUtils.isNotBlank(swaNameId) && null != moveSeatEnum){
            return;
        }

        // 1.标识长链接 2.标识短链接
        Integer longConnect = null == idleTime ? ConnectTypeEnum.SHORT_CONNECT.getCode() : ConnectTypeEnum.LONG_CONNECT.getCode();
        String connectType = longConnect.toString();
        //设置策略移动的位置
        DeviceDTO beforeDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, deviceUuid,connectType);
        DeviceDTO afterDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, deviceUuid,connectType);
        DeviceDTO beforeConflictDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE, deviceUuid,connectType);

        DeviceDTO topDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, deviceUuid,connectType);
        if (null != topDevice) {
            settingDTO.setMoveSeatEnum(MoveSeatEnum.LAST);
        } else if (beforeDevice != null) {
            settingDTO.setMoveSeatEnum(MoveSeatEnum.BEFORE);
            String relatedRule = beforeDevice.getRelatedRule();
            settingDTO.setSwapNameId(relatedRule == null ? "" : relatedRule.trim());
        } else if (afterDevice != null) {
            settingDTO.setMoveSeatEnum(MoveSeatEnum.AFTER);
            String relatedRule = afterDevice.getRelatedRule();
            settingDTO.setSwapNameId(relatedRule == null ? "" : relatedRule.trim());
        } else if (beforeConflictDevice != null) {
            //查询冲突策略
            DenyPolicyInfoDTO policyIdByFirstDeny = policyMergeNewTaskService.getPolicyIdByFirstDeny(cmdDTO,true);
            
            if (ObjectUtils.isNotEmpty(policyIdByFirstDeny)) {
                String policyId = policyIdByFirstDeny.getPolicyId();
                String ruleName = policyIdByFirstDeny.getRuleName();

                if (StringUtils.isNotBlank(policyId)) {
                    CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                    // 判断是否有行号，目前只有思科支持行号移动
                    if (StringUtils.isNotBlank(policyIdByFirstDeny.getLineNum())) {
                        String lineNum = policyIdByFirstDeny.getLineNum();
                        cmdDTO.getSetting().setPolicyId(lineNum);
                        cmdDTO.getSetting().setSwapNameId(lineNum);
                        commandTaskEditableEntity.setMovePosition(lineNum);
                    } else {
                        cmdDTO.getSetting().setPolicyId(policyId);
                        cmdDTO.getSetting().setSwapNameId(policyId);
                        commandTaskEditableEntity.setMovePosition(policyId);
                    }
                    settingDTO.setMoveSeatEnum(MoveSeatEnum.BEFORE);
                    Integer id = task.getId();
                    commandTaskEditableEntity.setId(id);
                    commandTaskEdiableMapper.updateByPrimaryKeySelective(commandTaskEditableEntity);
                } else if (StringUtils.isNotBlank(ruleName)) {
                    settingDTO.setPolicyName(ruleName);
                    settingDTO.setMoveSeatEnum(MoveSeatEnum.BEFORE);
                    settingDTO.setSwapNameId(ruleName);
                    CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                    Integer id = task.getId();
                    commandTaskEditableEntity.setId(id);
                    commandTaskEditableEntity.setMovePosition(ruleName);
                    commandTaskEdiableMapper.updateByPrimaryKeySelective(commandTaskEditableEntity);
                }
            }
        } else {
            settingDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
        }
        log.info("构建完移动参数之后的cmdDTO is " + JSONObject.toJSONString(cmdDTO, false));
    }
}
