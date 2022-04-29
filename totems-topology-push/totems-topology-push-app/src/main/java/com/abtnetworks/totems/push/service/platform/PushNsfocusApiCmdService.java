package com.abtnetworks.totems.push.service.platform;


import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @desc    绿盟API 下发接口
 * @author lifei
 * @date 2021-03-12 16:01
 */
public interface PushNsfocusApiCmdService {

    /**
     * 绿盟API下发
     * @param pushCmdDTO
     * @param nodeEntity
     * @return
     */
    PushResultDTO PushNsfocusApiCmd(PushCmdDTO pushCmdDTO, NodeEntity nodeEntity);

}
