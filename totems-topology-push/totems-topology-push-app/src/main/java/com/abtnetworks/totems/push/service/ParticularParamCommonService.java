package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/7/30
 */
public interface ParticularParamCommonService {

    /**
     * 获取策略id 更新到回滚命令行使用
     * @param commandTaskEntity
     * @param pushCmdDTO
     */
    void getPolicyIdForRollback(CommandTaskEditableEntity commandTaskEntity, PushCmdDTO pushCmdDTO);
}
