package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.dto.DisposalCommandDTO;

import java.util.List;

/**
 * @author zc
 * @date 2019/11/13
 */
public interface DisposalCommandService {

    /**
     * 命令行生成
     * @param orderUuid
     * @return
     */
    List<DisposalCommandDTO> generateCommand(String orderUuid);

}
