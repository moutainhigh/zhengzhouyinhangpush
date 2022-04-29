package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @desc 自动开通下发验证相关接口
 * @author zhoumuhua
 * @date 2021-07-20
 */
public interface PushVerifyService {

    /**
     * 获取验证的命令行
     * @param pushCmdDTO
     * @param pushResultDTO
     * @return
     */
    String getVerifyCmd (PushCmdDTO pushCmdDTO, PushResultDTO pushResultDTO);
}
