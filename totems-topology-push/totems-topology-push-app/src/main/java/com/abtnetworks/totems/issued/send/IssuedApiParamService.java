package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/7
 */
public interface IssuedApiParamService {
    /**
     * 从任务工单表中获取到的数据给下发使用
     *
     * @param pushCmdDTO
     * @return
     */
    PushCmdDTO recommendTaskManagerToIssued(PushCmdDTO pushCmdDTO, CommandTaskEditableEntity commandTaskEntity);

    /**
     * 从封堵工单中获取到的数据给下发使用
     *
     * @param pushCmdDTO
     * @param deviceId
     * @param disposalOrderDTO
     * @return
     */
    PushCmdDTO disposalToIssued(PushCmdDTO pushCmdDTO, String deviceId, DisposalOrderDTO disposalOrderDTO);
}
