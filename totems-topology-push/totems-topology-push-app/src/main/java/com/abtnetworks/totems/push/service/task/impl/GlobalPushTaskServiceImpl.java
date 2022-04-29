package com.abtnetworks.totems.push.service.task.impl;

import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.service.task.GlobalPushTaskService;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlobalPushTaskServiceImpl implements GlobalPushTaskService {
    private final static Logger logger = LoggerFactory.getLogger(GlobalPushTaskServiceImpl.class);
    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    private OAuth2RestTemplate OAuth2RestTemplate;

    @Value("${topology.vmsdn-server-prefix:http://${service_connection_url}:8085/}")
    private String vmwareServerPrefix;
    @Value("${topology.restpath.getPushTaskCntByTaskIds:vmsdn/ware_sdn_push_task/getPushTaskCntByTaskIds}")
    private String getPushTaskCntByTaskIds;
    @Value("${topology.restpath.sendPolicy:vmsdn/ware_sdn_push_task/sendPolicy}")
    private String sendPolicy;
    @Value("${topology.restpath.rollBack:vmsdn/ware_sdn_push_task/rollBack}")
    private String rollBack;

    //revert=true表示回滚
    public void pushWeTask(List<Integer> weTaskIds,boolean revert){
        String path = vmwareServerPrefix;
        if(revert){
            path+=rollBack;
        }else{
            path+=sendPolicy;
        }
        JSONObject jsonObject = new JSONObject();
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("taskIds",weTaskIds);
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, JSON.toJSONString(weTaskIds));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, paramMap, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用{}接口服务端异常", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用{}接口响应状态码：{}", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("errcode"), jsonObject.get("errmsg"));
                throw (new RuntimeException(String.format("远程调用{}接口响应errorCode:{};errmsg:{}", path, jsonObject.get("errcode"), jsonObject.get("errmsg"))));
            }
        }
    }


    public int getWePushTaskListByTaskId(List<Integer> weTaskIds){
        String path = vmwareServerPrefix + getPushTaskCntByTaskIds;
        JSONObject jsonObject = new JSONObject();
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, JSON.toJSONString(weTaskIds));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, weTaskIds, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用{}接口服务端异常", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用{}接口响应状态码：{}", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                return Integer.parseInt(jsonObject.get("data").toString());
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("errcode"), jsonObject.get("errmsg"));
                throw (new RuntimeException(String.format("远程调用{}接口响应errorCode:{};errmsg:{}", path, jsonObject.get("errcode"), jsonObject.get("errmsg"))));
            }
        }
    }

    @Autowired
    private RecommendTaskManager recommendTaskService;
    @Autowired
    private CommandTaskManager commandTaskManager;

    public List<CommandTaskDTO> getCommandTaskDTOListByTaskid(List<Integer> idList,boolean revert,StringBuilder errMsg,List<String> themeList,List<Integer> weTaskIds){
        List<CommandTaskDTO> taskDTOList = new ArrayList<>();
        for(Integer id: idList) {
            RecommendTaskEntity taskEntity = recommendTaskService.getRecommendTaskByTaskId(id);
            if(taskEntity.getWeTaskId()!=null){
                weTaskIds.add(taskEntity.getWeTaskId());
            }
            logger.info(String.format("获取任务(%d)", id));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            if(taskEntityList.size() == 0) {
                logger.error(String.format("获取任务(%d)失败，任务下没有命令行数据...", id));
                continue;
            }
            boolean ignore = false;
            for(CommandTaskEditableEntity task: taskEntityList) {
                String command = task.getCommandline();
                if(org.apache.commons.lang3.StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                    errMsg.append(String.format("[%s]开始下发失败，存在未生成命令行的设备！", taskEntity.getTheme()));
                    ignore = true;
                    break;
                }
            }
            if(ignore) {
                continue;
            }
            CommandTaskDTO taskDTO = new CommandTaskDTO();
            taskDTO.setList(taskEntityList);
            taskDTO.setRevert(revert);

            taskDTO.setTaskId(taskEntity.getId());
            taskDTO.setTheme(taskEntity.getTheme());

            taskDTOList.add(taskDTO);
            themeList.add(taskEntity.getTheme());
        }
        return taskDTOList;
    }


}
