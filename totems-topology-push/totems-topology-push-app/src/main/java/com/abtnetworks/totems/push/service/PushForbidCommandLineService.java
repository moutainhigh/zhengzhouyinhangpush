package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.dto.ForbidCommandLineDTO;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @desc    封禁IP命令行 接口
 * @author liuchanghao
 * @date 2020-09-10 17:22
 */
@Service
public interface PushForbidCommandLineService {

    /**
     * 查询工单任务下，所有设备最新的下发记录信息
     * @param currentPage  当前页
     * @param pageSize  每页显示记录条数
     * @param uuid  任务单uuid
     * @return
     */
    PageInfo<ForbidCommandLineDTO> findLastListByUuid(Integer currentPage, Integer pageSize, String uuid);


    /**
     * 开始执行下发
     * @param streamId 任务线程唯一标识
     * @param uuid  任务单uuid
     * @param userName  操作人姓名
     */
    void startSendCommandTask(String streamId, String uuid, String userName);
}
