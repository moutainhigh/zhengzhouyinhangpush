package com.abtnetworks.totems.auto.schedule;

import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendTaskMapper;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @desc 自动下发自动开通工单定时任务类
 * @author zhoumuhua
 * @date 2021-07-08
 */
@Component
@Slf4j
public class AutoPushTaskSchedule {

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Autowired
    AutoRecommendTaskMapper autoRecommendTaskMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableMapper;


    @KafkaListener(topics = "autoPushTaskSchedule")
    public void autoPushTask() {

        log.info("------定时下发自动开通工单任务--->开始------");

        //获取需要定时下发的自动开通工单任务
        List<AutoRecommendTaskEntity> autoTaskEntityList = autoRecommendTaskMapper.getAutoPushTask();

        List<AutoRecommendTaskEntity> autoPushTaskEntityList = new ArrayList<>();
        for (AutoRecommendTaskEntity autoTaskEntity : autoTaskEntityList) {
            log.info("过滤不可下发工单");
            String relevancyNat = autoTaskEntity.getRelevancyNat();
            if (StringUtils.isEmpty(relevancyNat)) {
                continue;
            }
            JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
            boolean isAutoPush = true;

            for (int index = 0; index < jsonArray.size(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                Integer natId = jsonObject.getInteger("id");
                List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
                if (CollectionUtils.isEmpty(commandTaskEditableList)) {
                    isAutoPush = false;
                    break;
                }

                for (CommandTaskEditableEntity editTaskEntity : commandTaskEditableList) {
                    String command = editTaskEntity.getCommandline();
                    if (StringUtils.isEmpty(command)) {
                        isAutoPush = false;
                        break;
                    }
                }
            }

            if (isAutoPush) {
                autoPushTaskEntityList.add(autoTaskEntity);
            }

        }

        if (ObjectUtils.isNotEmpty(autoPushTaskEntityList)) {
            AutoRecommendTaskVO autoRecommendTaskVO = new AutoRecommendTaskVO();
            List<Integer> idList = autoPushTaskEntityList.stream().map(s -> s.getId()).collect(Collectors.toList());
            autoRecommendTaskVO.setIdList(idList);
            autoRecommendTaskVO.setIsRevert(false);
            pushAutoRecommendService.autoPush(autoRecommendTaskVO);
            log.info("------定时下发自动开通工单任务--->结束------");
        } else {
            log.info("------没有需要定时下发的自动开通任务------");
        }

    }
}
