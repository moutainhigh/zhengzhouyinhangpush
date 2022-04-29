package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.global.CloudAddsContainResult;
import com.abtnetworks.totems.recommend.dto.global.VmwareSdnBusinessDTO;
import com.abtnetworks.totems.recommend.entity.AddRecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface GlobalRecommendService {

   void editWETask(AddRecommendTaskEntity task);
   String startGlobalRecommendTaskList(String ids, Authentication authentication) throws Exception;

    RecommendTaskEntity addGlobalRecommendTask(AddRecommendTaskEntity entity, Authentication auth);

    void addGlobalinternat(AddRecommendTaskEntity entity, RecommendTaskEntity recommendTaskEntity);

    VmwareSdnBusinessDTO getWETaskByWETaskId(Integer weTaskId);

    CloudAddsContainResult checkCloudAddsContain(AddRecommendTaskEntity entity);

    VmwareSdnBusinessDTO buildWETaskDTO(AddRecommendTaskEntity entity, CloudAddsContainResult containResult);

    void deleteWeTasks(List<Integer> weTaskIds);
}
