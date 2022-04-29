package com.abtnetworks.totems.issued.business.service;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-17
 */
public interface RegularParamForMatchService {
    /***
     * 准备一些组合成list
     * @param modelNumber 错误配置的个数
     * @return
     */
    GlobAndRegexElementDTO ordinalError2RegForList(String modelNumber);

    /**
     * 提供给外层使用的
     *
     * @param pushCmdDTO
     * @return
     */
    PushCommandRegularParamDTO produceRegexParam(PushCmdDTO pushCmdDTO);

    /**
     * python调用
     * @param ip
     * @param username
     * @return
     */
    String python2Fortress(String ip ,String username);
}
