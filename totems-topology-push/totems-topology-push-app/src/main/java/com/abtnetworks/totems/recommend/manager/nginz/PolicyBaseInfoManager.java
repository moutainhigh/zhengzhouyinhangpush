package com.abtnetworks.totems.recommend.manager.nginz;

import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.vo.SubnetSearchResultDTO;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/29
 */
public interface PolicyBaseInfoManager {


    /**
     * 搜索子网关联的设备、接口、域
     * @param task
     * @return
     */
    List<SubnetSearchResultDTO> subnetSearchWith(SimulationTaskDTO task);
}
