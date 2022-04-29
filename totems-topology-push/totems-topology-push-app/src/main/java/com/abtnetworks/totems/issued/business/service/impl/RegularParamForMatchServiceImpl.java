package com.abtnetworks.totems.issued.business.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.issued.business.dao.mysql.CommandRegularParamMapper;
import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.business.service.RegularParamForMatchService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.whale.system.dto.FortressDeviceInfoDTO;
import com.abtnetworks.totems.whale.system.ro.FortressDeviceRO;
import com.abtnetworks.totems.whale.system.service.WhaleProxyClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-17
 */
@Slf4j
@Service
public class RegularParamForMatchServiceImpl implements RegularParamForMatchService {
    /***正则表达式*/
    @Resource
    CommandRegularParamMapper commandRegularParamMapper;

    @Autowired
    AdvancedSettingService advancedSettingService;

    /**
     * 正则表示层服务
     **/
    @Resource
    RegularParamForMatchService regularParamForMatchService;

    @Autowired
    WhaleProxyClient whaleProxyClient;



    @Override
    public GlobAndRegexElementDTO ordinalError2RegForList(String modelNumber) {
        PushCommandRegularParamEntity commandRegularParamEntity = commandRegularParamMapper.getCommandRegularParamByModelNumber(modelNumber);
        GlobAndRegexElementDTO globAndRegexElementDTO = new GlobAndRegexElementDTO();
        List<JSONObject> linuxPromptGlobEx = new ArrayList<>();
        List<JSONObject> linuxPromptRegEx = new ArrayList<>();
        if (commandRegularParamEntity != null) {
            String promptErrorInfo = commandRegularParamEntity.getPromptErrorInfo();
            String promptRegCommand = commandRegularParamEntity.getPromptRegCommand();
            commonGetRegCommand(promptRegCommand, promptErrorInfo, linuxPromptGlobEx);


            String promptErrorRegInfo = commandRegularParamEntity.getPromptErrorRegInfo();
            String promptRegExCommand = commandRegularParamEntity.getPromptRegExCommand();
            commonGetRegCommand(promptRegExCommand, promptErrorRegInfo, linuxPromptRegEx);
            globAndRegexElementDTO.setLinuxPromptGlobEx(linuxPromptGlobEx);
            globAndRegexElementDTO.setLinuxPromptRegEx(linuxPromptRegEx);
            return globAndRegexElementDTO;
        } else {
            return globAndRegexElementDTO;
        }

    }

    /**
     * 对支持glob和reg方式公共方法
     *
     * @param promptCommand
     * @param promptErrorRegInfo
     * @param globOrRegexList
     */
    private void commonGetRegCommand(String promptCommand, String promptErrorRegInfo, List<JSONObject> globOrRegexList) {

        if (StringUtils.isNotEmpty(promptErrorRegInfo)) {
            JSONArray promptErrorJsonArray = JSONArray.parseArray(promptErrorRegInfo);
            promptErrorJsonArray.stream().forEach(p -> {
                JSONObject jsonObject = (JSONObject) p;
                globOrRegexList.add(jsonObject);
            });

        }

        if (StringUtils.isNotEmpty(promptCommand)) {

            String[] mapPromptRegular = promptCommand.trim().split(SendCommandStaticAndConstants.COMMA_SPLIT);
            for (int i = 0; i < mapPromptRegular.length; i++) {
                if (StringUtils.isNotEmpty(mapPromptRegular[i])) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(SendCommandStaticAndConstants.KEY, mapPromptRegular[i].trim());
                    jsonObject.put(SendCommandStaticAndConstants.VALUE, SendCommandStaticAndConstants.DEFAULT_PROMPT);
                    globOrRegexList.add(jsonObject);
                }
            }
        }

    }

    @Override
    public PushCommandRegularParamDTO produceRegexParam(PushCmdDTO pushCmdDTO) {
        PushCommandRegularParamEntity commandRegularParamEntity = commandRegularParamMapper.getCommandRegularParamByModelNumber(pushCmdDTO.getDeviceModelNumberEnum().getKey());
        if (pushCmdDTO.getDeviceModelNumberEnum().getKey().equals(DeviceModelNumberEnum.HILLSTONE.getKey())) {
            String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
            if (StringUtils.isNotEmpty(paramValue) && pushCmdDTO.getSpecialParamDTO() != null) {
                if (paramValue.equals("0")) {
                    pushCmdDTO.getSpecialParamDTO().setRollbackType(true);
                } else {
                    pushCmdDTO.getSpecialParamDTO().setRollbackType(false);
                }
            }
        }
        if (commandRegularParamEntity != null) {
            PushCommandRegularParamDTO pushCommandRegularParamDTO = new PushCommandRegularParamDTO();
            BeanUtils.copyProperties(commandRegularParamEntity, pushCommandRegularParamDTO);
            //准备正则匹配项
            GlobAndRegexElementDTO linuxPromptRegEx = regularParamForMatchService.ordinalError2RegForList(pushCmdDTO.getDeviceModelNumberEnum().getKey());
            pushCommandRegularParamDTO.setLinuxPromptRegEx(linuxPromptRegEx);

            return pushCommandRegularParamDTO;
        }

        return null;
    }

    @SneakyThrows
    @Override
    public String python2Fortress(String ip ,String username){
        FortressDeviceInfoDTO fortressDeviceInfoDTO = new FortressDeviceInfoDTO();
        fortressDeviceInfoDTO.setHostAddress(ip);
        fortressDeviceInfoDTO.setLoginName(username);
        FortressDeviceRO fortressDeviceRO = whaleProxyClient.queryDevicePassword(fortressDeviceInfoDTO);
        if (ObjectUtils.isNotEmpty(fortressDeviceRO) && StringUtils.isNotBlank(fortressDeviceRO.getDevicePassword())) {
            return fortressDeviceRO.getDevicePassword();
        } else {
            log.info("正常连接，不走堡垒机连接获取密码下发");
        }
        return "";
    }
}
