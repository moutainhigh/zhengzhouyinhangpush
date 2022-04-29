package com.abtnetworks.totems.common.tools.vo;

import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 下发队列VO
 * @date 2021/11/30 14:08
 */
@Data
public class PushQueueVo {

    /**
     * 是否下发回滚命令行
     */
    private boolean revert;

    /**
     * 任务ids
     */
    private List<Integer> taskIds;

    /**
     * 命令行集合
     */
    private List<BatchCommandTaskDTO> batchCommandTaskDTOS;
}
