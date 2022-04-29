package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesNodeMapper;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.RecommendTask2IssuedDTO;
import com.abtnetworks.totems.issued.send.IssuedApiParamService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.PushAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSONObject;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 下发对外提供的api的接口时，所有参数包装
 * @date 2020/9/7
 */
@Service
public class IssuedApiParamServiceImpl implements IssuedApiParamService {

    @Resource
    RecommendTaskManager recommendTaskManager;

    @Autowired
    public DisposalScenesService disposalScenesService;

    @Resource
    private DisposalScenesNodeMapper disposalScenesNodeMapper;

    @Override
    public PushCmdDTO recommendTaskManagerToIssued(PushCmdDTO pushCmdDTO, CommandTaskEditableEntity commandTaskEntity) {
        RecommendTaskEntity recommendTaskEntity = recommendTaskManager.getRecommendTaskByTaskId(commandTaskEntity.getTaskId());
        if (recommendTaskEntity != null) {
            Integer ipType = recommendTaskEntity.getIpType();
            if (ObjectUtils.allNotNull(ipType)) {
                MoveParamDTO moveParamDTO = new MoveParamDTO();
                moveParamDTO.setIpType(ipType);
                pushCmdDTO.setMoveParamDTO(moveParamDTO);
            }

            String additionalInfo = recommendTaskEntity.getAdditionInfo();
            RecommendTask2IssuedDTO recommendTask2IssuedDTO = new RecommendTask2IssuedDTO();
            if (additionalInfo != null) {
                JSONObject object = JSONObject.parseObject(additionalInfo);
                PushAdditionalInfoEntity additionalInfoEntity = JSONObject.toJavaObject(object, PushAdditionalInfoEntity.class);
                // 这里如果additional中有场景id，需要根据场景id和设备uuid去获取获取指定设备在场景中配置的源域和目的域
                if (null != additionalInfoEntity && StringUtils.isNotBlank(additionalInfoEntity.getScenesUuid())) {
                    List<DisposalScenesDTO> scenesDTOList = disposalScenesService.findByScenesUuid(additionalInfoEntity.getScenesUuid());
                    if (CollectionUtils.isNotEmpty(scenesDTOList)) {
                        for (DisposalScenesDTO disposalScenesDTO : scenesDTOList) {
                            if (commandTaskEntity.getDeviceUuid().equals(disposalScenesDTO.getDeviceUuid())) {
                                recommendTask2IssuedDTO.setSrcZone(disposalScenesDTO.getSrcZoneName());
                                recommendTask2IssuedDTO.setDstZone(disposalScenesDTO.getDstZoneName());
                            }
                        }
                    }
                } else {
                    BeanUtils.copyProperties(additionalInfoEntity, recommendTask2IssuedDTO);
                }
            }
            recommendTask2IssuedDTO.setIdleTime(null == recommendTaskEntity.getIdleTimeout() ? null : recommendTaskEntity.getIdleTimeout().toString());
            String matchMsg = commandTaskEntity.getMatchMsg();
            if (StringUtils.isNotBlank(matchMsg)) {
                JSONObject json = JSONObject.parseObject(matchMsg);
                recommendTask2IssuedDTO.setRuleListName(json.getString("ruleListName"));
                recommendTask2IssuedDTO.setMatchRuleId(json.getString("matchRuleId"));
            }
            if(StringUtils.isNotBlank(commandTaskEntity.getMergeInfo())){
                recommendTask2IssuedDTO.setMergePolicy(true);
            }
            pushCmdDTO.setRecommendTask2IssuedDTO(recommendTask2IssuedDTO);
        }
        return pushCmdDTO;
    }

    @Override
    public PushCmdDTO disposalToIssued(PushCmdDTO pushCmdDTO, String deviceId, DisposalOrderDTO disposalOrderDTO) {
        DisposalScenesDTO scenesDTO = new DisposalScenesDTO();
        scenesDTO.setDeviceUuid(deviceId);
        scenesDTO.setScenesUuid(disposalOrderDTO.getScenesUuids());
        List<DisposalScenesDTO> disposalScenesDTOList = disposalScenesNodeMapper.findDtoListForIssue(scenesDTO);
        String scenesUUIDs = disposalOrderDTO.getScenesUuids();
        if (CollectionUtils.isNotEmpty(disposalScenesDTOList) && StringUtils.isNotEmpty(scenesUUIDs)) {
            for (DisposalScenesDTO disposalScenesNodeEntity : disposalScenesDTOList) {
                if (scenesUUIDs.contains(disposalScenesNodeEntity.getScenesUuid())) {
                    RecommendTask2IssuedDTO recommendTask2IssuedDTO = new RecommendTask2IssuedDTO();
                    recommendTask2IssuedDTO.setSrcZone(disposalScenesNodeEntity.getSrcZoneName());
                    recommendTask2IssuedDTO.setDstZone(disposalScenesNodeEntity.getDstZoneName());
                    pushCmdDTO.setRecommendTask2IssuedDTO(recommendTask2IssuedDTO);
                }
            }
        }
        return pushCmdDTO;
    }
}
