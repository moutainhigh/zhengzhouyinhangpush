package com.abtnetworks.totems.push.manager.impl;

import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.push.dao.mysql.PushTaskMapper;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.push.entity.PushTaskEntity;
import com.abtnetworks.totems.push.manager.PushTaskManager;
import com.abtnetworks.totems.push.vo.DeviceCommandVO;
import com.abtnetworks.totems.push.vo.PushTaskPageVO;
import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 14:58
 */
@Service
public class PushTaskManagerImpl implements PushTaskManager {
    private static Logger logger = LoggerFactory.getLogger(PushTaskManagerImpl.class);

    @Autowired
    private PushTaskMapper pushTaskMapper;

    @Autowired
    private CredentialMapper credentialMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Override
    public PushTaskPageVO getPushTaskList(String orderNo, String type, String status, int page, int psize) {
        List<PushTaskEntity> list = null;
        Map<String,String> params = new HashMap();
        if(!AliStringUtils.isEmpty(orderNo)) {
            params.put("orderNo", orderNo);
        }
        if(!AliStringUtils.isEmpty(type)) {
            params.put("orderType", type);
        }
        if(!AliStringUtils.isEmpty(status)) {
            params.put("status", status);
        }
        list = pushTaskMapper.searchPushTaskListByOrderNo(params);

        List<PushTaskVO> pushTaskVOList = new ArrayList<>();

        for(PushTaskEntity taskEntity: list) {
            addPushTaskVO(pushTaskVOList, taskEntity);
        }
        int startNo = (page - 1) * psize;
        int endNo = page* psize;

        List<PushTaskVO> pageList = new ArrayList<>();
        if(pushTaskVOList.size() > startNo) {
            for(int index = startNo; index < endNo; index++ ){
                if(index < pushTaskVOList.size()) {
                    pageList.add(pushTaskVOList.get(index)) ;
                }
            }
        }
        PushTaskPageVO pageVO = new PushTaskPageVO();
        pageVO.setTotal(pushTaskVOList.size());
        pageVO.setList(pageList);
        return pageVO;
    }



    @Override
    public PushTaskEntity getPushTaskById(int taskId){
        return pushTaskMapper.getPushTaskById(taskId);
    }

    @Override
    public void addPushTask(PushTaskEntity entity) {
        pushTaskMapper.addPushTask(entity);
    }

    @Override
    public List<PushTaskEntity> getPolicyRecommendTaskListByOrderNo(String orderNo) {
        List<PushTaskEntity> list = pushTaskMapper.getPushTaskListByOrderNo(orderNo);
        return list;
    }

    @Override
    public void updatePushResult(int id, int status, String pushResult) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        params.put("pushResult", pushResult);
        pushTaskMapper.updatePushResult(params);
    }

    @Override
    public void updatePushStatus(int id, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        params.put("status", String.valueOf(status));
        pushTaskMapper.updatePushStatus(params);
    }

    @Override
    public void deletePushTask(int id) {
        pushTaskMapper.deletePushTask(id);
    }

    @Override
    public CredentialEntity getCredentialEntity(String uuid){
        List<CredentialEntity> list = credentialMapper.get(uuid);

        if(list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            logger.info("凭据数据不唯一！");
        }

        CredentialEntity entity = list.get(0);
        // 密码解密
        entity.setLoginPassword(Encodes.decodeBase64Key(entity.getLoginPassword()));
        entity.setEnablePassword(Encodes.decodeBase64Key(entity.getEnablePassword()));

        return entity;
    }

    @Override
    public String getDeviceModelNumber(String uuid) {
        return nodeMapper.getDeviceModelNumber(uuid);
    }

    @Override
    public String getCredentialUuid(String uuid){
        return nodeMapper.getCredentialUuidByDeviceUuid(uuid);
    }

    @Override
    public void updatePushTime(PushTaskEntity entity){
        pushTaskMapper.updatePushTime(entity);
    }

    /**
     * 将PushTaskEntity添加到PushTaskVO列表中，合并相同工单号的PushTaskEntity到一个PushTaskVO
     * @param list PushTaskVO列表
     * @param pushTaskEntity PushTaskEntity对象
     */
    private void addPushTaskVO(List<PushTaskVO> list, PushTaskEntity pushTaskEntity) {
        DeviceCommandVO deviceCommandVO = new DeviceCommandVO();
        deviceCommandVO.setCommand(pushTaskEntity.getCommand());
        deviceCommandVO.setDeviceName(pushTaskEntity.getDeviceName());
        deviceCommandVO.setManageIp(pushTaskEntity.getManageIp());
        deviceCommandVO.setPushResult(pushTaskEntity.getPushResult());

        //查询现有队列中是否有相同工单号的VO，有则添加DeviceCommandVO信息，然后返回
        for(PushTaskVO pushTaskVO:list) {
            if(pushTaskVO.getOrderNo().equals(pushTaskEntity.getOrderNo())) {
//                List<DeviceCommandVO> deviceCommandVOList = pushTaskVO.getDeviceCommandList();
//                boolean sameDevice = false;
//                for(DeviceCommandVO commandVO : deviceCommandVOList) {
//                    if(deviceCommandVO.getManageIp().equals(commandVO.getManageIp())) {
//                        sameDevice = true;
//                        String command = commandVO.getCommand() + "\n\n" + deviceCommandVO.getCommand();
//                        commandVO.setCommand(command);
//                        break;
//                    }
//                }
//                if(!sameDevice) {
//                    deviceCommandVOList.add(deviceCommandVO);
//                }
                return;
            }
        }

        //当前队列中没有相同工单号VO，则新建一个PushTaskVO
//        PushTaskVO pushTaskVO = new PushTaskVO();
//        pushTaskVO.setOrderNo(pushTaskEntity.getOrderNo());
//        pushTaskVO.setOrderType(pushTaskEntity.getOrderType());
//        pushTaskVO.setUserName(pushTaskEntity.getUserName());
//        pushTaskVO.setPushTime(pushTaskEntity.getPushTime());
//        pushTaskVO.setCreateTime(pushTaskEntity.getCreateTime());
//        pushTaskVO.setStatus(pushTaskEntity.getStatus());
//        List<DeviceCommandVO> deviceCommandVOList = new ArrayList();
//        deviceCommandVOList.add(deviceCommandVO);
//        pushTaskVO.setDeviceCommandList(deviceCommandVOList);
//
//        list.add(pushTaskVO);
    }


}
