package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;

import java.util.List;

public interface PathService {

    /**
     * @param task 仿真数据
     * 策略开通
     */
    List<PathInfoTaskDTO> findPath(SimulationTaskDTO task);

    /**
     * @param task 仿真数据
     * 青提策略开通
     */
    List<PathInfoTaskDTO> qtFindPath(SimulationTaskDTO task);

    /**
     * 互联网开通
     * @param task 仿真数据
     *
     * @return
     */
    List<PathInfoTaskDTO> findInternetPath(SimulationTaskDTO task);

    /**
     * 青提业务开通
     * @param task 仿真数据
     *
     * @return
     */
    List<PathInfoTaskDTO> qtBusinessFindPath(SimulationTaskDTO task);

    /**
     * 互联网开通
     * @param task
     * @return
     */
    List<PathInfoTaskDTO> qtInternetFindPath(SimulationTaskDTO task);
}
