package com.abtnetworks.totems.push.service.platform;


import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @desc    管理平台API 下发接口
 * @author liuchanghao
 * @date 2021-02-22 10:18
 */
public interface PushPlatformApiCmdService {

    /**
     * 飞塔管理平台API下发
     * @param pushCmdDTO
     * @param nodeEntity
     * @return
     */
    PushResultDTO PushFortinetApiCmd(PushCmdDTO pushCmdDTO, NodeEntity nodeEntity);

}
