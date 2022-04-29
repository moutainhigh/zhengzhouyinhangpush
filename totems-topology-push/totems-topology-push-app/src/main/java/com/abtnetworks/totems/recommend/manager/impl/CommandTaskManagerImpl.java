package com.abtnetworks.totems.recommend.manager.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dto.recommend.EditCommandDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.vo.CommandVO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandTaskManagerImpl implements CommandTaskManager {
    private static Logger logger = LoggerFactory.getLogger(CommandTaskManagerImpl.class);

    @Autowired
    CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    private LogClientSimple logClientSimple;

    @Override
    public int addCommandEditableEntityTask(CommandTaskEditableEntity entity) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(entity.getTaskId()));
        params.put("deviceUuid", entity.getDeviceUuid());
        List<CommandTaskEditableEntity> list = commandTaskEdiableMapper.selectByTaskIdAndDeviceUuid(params);
        if(CollectionUtils.isEmpty(list)) {
            commandTaskEdiableMapper.insert(entity);
        } else {
            entity.setId(list.get(0).getId());
            commandTaskEdiableMapper.updateByPrimaryKey(entity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int insertCommandEditableEntityTask(CommandTaskEditableEntity entity) {

        return commandTaskEdiableMapper.insert(entity);
    }

    @Override
    public int insertCommandEditableEntityList(List<CommandTaskEditableEntity> list) {
        if(list == null || list.size() == 0) {
            return ReturnCode.SAVE_EMPTY_LIST;
        }

        int rc = commandTaskEdiableMapper.insertList(list);
        if(rc != list.size() ) {
            logger.error(String.format("任务(%d)存储命令行错误，SQL影响行数与存储行数不一致！%d:%d",list.get(0).getTaskId(), list.size(), rc));
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public CommandTaskEditableEntity getCommandEditableEntityByTaskIdAndDeviceUuid(int taskId, String deviceUuid) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        params.put("deviceUuid", deviceUuid);
        List<CommandTaskEditableEntity> list = commandTaskEdiableMapper.selectByTaskIdAndDeviceUuid(params);
        if(list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1){
            logger.error(String.format("Task(%d)对应命令行数据不唯一", taskId));
        }

        return list.get(0);
    }

    @Override
    public List<CommandVO> getCommandByTaskId(int taskId) {
        List<CommandTaskEditableEntity> list =  commandTaskEdiableMapper.selectByTaskId(taskId);
        List<CommandVO> commandVoList = new ArrayList<>();
        for(CommandTaskEditableEntity entity: list) {
            String deviceUuid = entity.getDeviceUuid();
            CommandVO commandVO = new CommandVO();
            BeanUtils.copyProperties(entity,commandVO);
            commandVO.setTaskId(taskId);
            commandVO.setCommand(entity.getCommandline());
            commandVO.setPushResult(entity.getCommandlineEcho()==null?"":entity.getCommandlineEcho());
            commandVO.setRevert(entity.getCommandlineRevert()==null?"":entity.getCommandlineRevert());
            commandVO.setRevertEcho(entity.getCommandlineRevertEcho()==null?"":entity.getCommandlineRevertEcho());
            NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
            String deviceName = String.format("未知设备(%s)", deviceUuid);
            if(nodeEntity != null) {
                deviceName = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
            }
            commandVO.setDeviceName(deviceName);
            commandVO.setDeviceUuid(deviceUuid);
            commandVO.setEditUserName(entity.getEditUserName());
            commandVO.setEditTime(entity.getModifiedTime());
            if(entity.getPushStatus()> PolicyConstants.PUSH_STATUS_NOT_START) {
                commandVO.setEditable(false);
            } else {
                commandVO.setEditable(true);
            }
            if(entity.getRevertStatus()> PolicyConstants.REVERT_STATUS_NOT_START) {
                commandVO.setEditableRevert(false);
            } else {
                commandVO.setEditableRevert(true);
            }
            commandVO.setId(entity.getId());
            commandVO.setPushStatus(entity.getPushStatus());
            commandVoList.add(commandVO);
        }
        return commandVoList;
    }

    @Override
    public List<CommandTaskEditableEntity> getCommandTaskByTaskId(int taskId) {
        return commandTaskEdiableMapper.selectByTaskId(taskId);
    }

    @Override
    public CommandTaskEditableEntity selectByPrimaryKey(int id) {
        return commandTaskEdiableMapper.selectByPrimaryKey(id);
    }

    @Override
    public int editCommandEditableEntity(EditCommandDTO editCommandDTO,  String userName) {
        Integer taskId = editCommandDTO.getTaskId();
        String deviceUuid = editCommandDTO.getDeviceUuid();
        String command = editCommandDTO.getCommand();
        Integer type = editCommandDTO.getType();
        CommandTaskEditableEntity entity = getCommandEditableEntityByTaskIdAndDeviceUuid(taskId , deviceUuid);
        if (entity == null) {
            logger.error(String.format("任务(%d)没有对应命令行", taskId));
            return ReturnCode.NO_RECORD;
        }
        CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
        newEntity.setId(entity.getId());


        newEntity.setRevertStatus(null);
        newEntity.setPushStatus(null);
        if(type!=null && type == 0){
            newEntity.setEditUserName(userName);
            newEntity.setCommandline(command);
            newEntity.setModifiedTime(new Date());
        }else{
            newEntity.setCommandlineRevert(command);
            newEntity.setRevertModifiedTime(new Date());
            newEntity.setEditRevertUserName(userName);
        }

        int count = commandTaskEdiableMapper.updateByPrimaryKeySelective(newEntity);
        String message = String.format("编辑工单: %s 命令行%s", entity.getTheme(), count > 0 ? "成功" : "失败");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandEcho(int id, String commandlineEcho) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setId(id);
        entity.setCommandlineEcho(commandlineEcho);
        return commandTaskEdiableMapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public int update(CommandTaskEditableEntity entity) {
        return commandTaskEdiableMapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public int removeByTaskId(int taskId) {
        return commandTaskEdiableMapper.deleteByTaskId(taskId);
    }

    @Override
    public List<CommandTaskEditableEntity> getScheduledCommand(){
        return commandTaskEdiableMapper.selectScheduledTask();
    }

    @Override
    public List<CommandTaskEditableEntity> getExecuteTask() {
        return commandTaskEdiableMapper.selectExecuteTask();
    }

    @Override
    public int setPushSchedule(CommandTaskEditableEntity entity) {
        return commandTaskEdiableMapper.setPushSchedule(entity);
    }
}
