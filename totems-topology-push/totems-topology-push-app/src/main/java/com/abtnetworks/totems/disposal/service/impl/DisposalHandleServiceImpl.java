package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.BaseService;
import com.abtnetworks.totems.disposal.CommonConfigParam;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalHandleMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderCenterMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderScenesMapper;
import com.abtnetworks.totems.disposal.dto.DisposalHandleCallbackDTO;
import com.abtnetworks.totems.disposal.dto.DisposalHandleListDTO;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.entity.*;
import com.abtnetworks.totems.disposal.enums.*;
import com.abtnetworks.totems.disposal.service.DisposalHandleService;
import com.abtnetworks.totems.disposal.service.DisposalOrderService;
import com.abtnetworks.totems.disposal.service.DisposalRollbackService;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * ????????????
 * @Author hw
 * @Description
 * @Date 10:23 2019/11/12
 */
@Service
public class DisposalHandleServiceImpl extends BaseService implements DisposalHandleService {

    @Value("${push.work.name}")
    private String workName;

    @Value("${push.work.ip}")
    private String workIp;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Resource
    private DisposalHandleMapper disposalHandleDao;

    @Autowired
    private DisposalOrderMapper disposalOrderDao;

    @Autowired
    private DisposalOrderCenterMapper disposalOrderCenterDao;

    @Autowired
    private DisposalRollbackService disposalRollbackService;

    @Autowired
    private DisposalOrderScenesMapper disposalOrderScenesMapper;

    @Autowired
    private DisposalScenesService disposalScenesService;

    @Autowired
    private DisposalOrderService disposalOrderService;

    @Autowired
    private LogClientSimple logClientSimple;

    /**
     * ??????
     */
    @Override
    @Transactional
    @KafkaListener(topics = CommonConfigParam.STR_TOPIC_ASSIGN_BRANCH)
    public ReturnT<String> insert(String jsonStr) {
        if (jsonStr == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "??????????????????");
        }

        try {
            logger.info("???????????????????????????,jsonStr:{}", jsonStr);
            DisposalOrderDTO dto = JSONObject.parseObject(jsonStr, DisposalOrderDTO.class);
            //????????????
            String branchNames = "," + dto.getBranchNames() + ",";
            if (!branchNames.contains("," + workName + ",")) {
                logger.info("??????????????????????????????????????????????????????????????????,branchNames:{} ", branchNames);
                return ReturnT.SUCCESS;
            }

            //????????????
            if (StringUtils.isBlank(dto.getOrderNo()) || dto.getCategory() == null
                    || dto.getType() == null || dto.getSendType() == null) {
                logger.error("????????????????????????????????????json");
                return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
            }


            //????????????
            if(dto.getCategory().equals(DisposalCategoryEnum.POLICY.getCode())){

                //???IP????????????srcIp???dstIp
                if(dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode())){
                    if(StringUtils.isBlank(dto.getSrcIp()) && StringUtils.isBlank(dto.getDstIp())){
                        logger.error("????????????-????????????????????????????????????IP???????????????????????????IP??????");
                        return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
                    }
                }else if(dto.getType().equals(DisposalTypeEnum.PATH.getCode()) || dto.getType().equals(DisposalTypeEnum.MANUAL.getCode())){
                    //??????????????????????????????????????????
                    if (StringUtils.isBlank(dto.getAction())) {
                        logger.error("????????????-??????????????????, ?????????????????????????????????????????????????????????");
                        return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
                    }

                }else{
                    logger.error("??????????????????, ??????????????????, type:{}", dto.getType());
                    return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
                }

                //???????????????
                if (StringUtils.isNotBlank(dto.getSrcIp())) {
                    int rc = InputValueUtils.checkIp(dto.getSrcIp());
                    //??????IP????????????????????????????????????????????????????????????
                    if (rc == ReturnCode.INVALID_IP_RANGE) {
                        dto.setSrcIp(InputValueUtils.autoCorrect(dto.getSrcIp()));
                    } else if (rc != ReturnCode.POLICY_MSG_OK) {
                        rc = InputValueUtils.checkIpV6(dto.getSrcIp());
                        if (rc != ReturnCode.POLICY_MSG_OK) {
                            String msg = "??????????????????" + ReturnCode.getMsg(rc);
                            logger.error(msg);
                            return new ReturnT(ReturnT.FAIL_CODE, msg);
                        }
                    }

                    //????????????????????????
                    dto.setSrcIp(InputValueUtils.formatIpAddress(dto.getSrcIp()));
                }

                if (StringUtils.isNotBlank(dto.getDstIp())) {
                    int rc = InputValueUtils.checkIp(dto.getDstIp());
                    //??????IP????????????????????????????????????????????????????????????
                    if (rc == ReturnCode.INVALID_IP_RANGE) {
                        dto.setDstIp(InputValueUtils.autoCorrect(dto.getDstIp()));
                    } else if (rc != ReturnCode.POLICY_MSG_OK) {
                        rc = InputValueUtils.checkIpV6(dto.getDstIp());
                        if (rc != ReturnCode.POLICY_MSG_OK) {
                            String msg = "?????????????????????" + ReturnCode.getMsg(rc);
                            logger.error(msg);
                            return new ReturnT(ReturnT.FAIL_CODE, msg);
                        }
                    }

                    dto.setDstIp(InputValueUtils.formatIpAddress(dto.getDstIp()));
                }

                if(StringUtils.isNotBlank(dto.getServiceList())){
                    List<ServiceDTO> serviceList =  ServiceDTOUtils.toList(dto.getServiceList());
                    for(ServiceDTO service : serviceList) {
                        if(StringUtils.isNotBlank(service.getDstPorts())) {
                            //?????????????????????
                            service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                        }
                    }
                    dto.setServiceList(ServiceDTOUtils.toString(serviceList));
                }

            }else if(dto.getCategory().equals(DisposalCategoryEnum.ROUT.getCode())){
                //???????????????
                if (!dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode()) && !dto.getType().equals(DisposalTypeEnum.MANUAL.getCode())) {
                    logger.error("????????????-?????????????????????type:{}?????????", dto.getType());
                    return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
                }
                if (StringUtils.isBlank(dto.getRoutingIp())) {
                    logger.error("????????????-???????????????????????????????????????IP??????");
                    return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
                }

                //??????IP??????
                int rc = InputValueUtils.checkIp(dto.getRoutingIp());
                //???IP????????????????????????????????????
                if (rc == ReturnCode.INVALID_IP_RANGE) {
                    dto.setRoutingIp(InputValueUtils.autoCorrect(dto.getRoutingIp()));
                } else if (rc != ReturnCode.POLICY_MSG_OK) {
                    rc = InputValueUtils.checkIpV6(dto.getRoutingIp());
                    if (rc != ReturnCode.POLICY_MSG_OK) {
                        String msg = "???????????????" + ReturnCode.getMsg(rc);
                        logger.error(msg);
                        return new ReturnT(ReturnT.FAIL_CODE, msg);
                    }
                }

                dto.setRoutingIp(InputValueUtils.formatIpAddress(dto.getRoutingIp()));

            }else{
                logger.error("??????????????????, ??????????????????, category:{}", dto.getCategory());
                return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
            }

            if(dto.getCallbackFlag() == null){
                dto.setCallbackFlag(false);
                logger.info("??????????????????????????????????????????????????????");
            }

            logger.info("????????????????????????");


            //???????????????????????????????????????????????????????????????
            if (!dto.getCallbackFlag()) {
                DisposalOrderCenterEntity center = new DisposalOrderCenterEntity();
                BeanUtils.copyProperties(dto, center);
                center.setUuid(dto.getCenterUuid());
                center.setSourceClassification(1);
                disposalOrderCenterDao.insert(center);
                logger.info("????????????????????????");
            }

            DisposalHandleEntity entity = new DisposalHandleEntity();
            entity.setCenterUuid(dto.getCenterUuid());
            entity.setCreateUser(dto.getCreateUser());
            entity.setCreateTime(DateUtil.getCurrentTimestamp());
            entity.setStatus(DisposalHandleStatusEnum.UN_PROCESS.getCode());
            entity.setCallbackFlag(dto.getCallbackFlag());


            if(dto.getSendType().equals(DisposalSendTypeEnum.AUDIT.getCode())){
                entity.setNeedAuditFlag(true);
            }else{
                entity.setNeedAuditFlag(false);
            }

            disposalHandleDao.insert(entity);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("??????????????????%s??????",dto.getOrderName()));
            logger.info("??????????????????????????????");

            //?????????????????????????????????IP?????????
            if(dto.getSendType().equals(DisposalSendTypeEnum.AUTO.getCode()) &&
                    (dto.getType().equals(DisposalTypeEnum.PATH.getCode()) || dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode()) )){

                entity.setStatus(DisposalHandleStatusEnum.AUTO_COMPLETED.getCode());
                int count = disposalHandleDao.update(entity);

                if(count == 1){
                    sendHandleStatus(entity.getCenterUuid(), dto.getCallbackFlag());
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("??????????????????%s??????",dto.getOrderName()));
                    logger.info("kafka??????????????????");
                }

                createOrder(dto.getCallbackFlag(), entity.getCenterUuid(), dto.getCreateUser());
                logger.info("??????????????????");

            }

        }catch (Exception e){
            logger.error("??????????????????????????????????????????????????????????????????,jsonStr:{}", jsonStr);
            return new ReturnT<>(ReturnT.FAIL_CODE, "????????????");
        }


        ReturnT<String> returnT = new ReturnT(ReturnT.SUCCESS_CODE);
        return returnT;
    }


    /**
     * ??????
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalHandleDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> update(DisposalHandleEntity disposalHandle) {
        int ret = disposalHandleDao.update(disposalHandle);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ?????? get By Id
     */
    @Override
    public DisposalHandleEntity getById(Long id) {
        return disposalHandleDao.getById(id);
    }

    /**
     * ?????? get
     */
    @Override
    public DisposalHandleEntity get(DisposalHandleEntity disposalHandle) {
        return disposalHandleDao.get(disposalHandle);
    }

    /**
     * ????????????
     */
    @Override
    public ResultRO<List<DisposalHandleListDTO>> findList(Integer category, Integer type,
                                                          Integer status, String content,
                                                          Boolean callbackFlag,
                                                          int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalHandleListDTO> list = disposalHandleDao.findByCondition(category, type, status, content, callbackFlag);
        if (list != null && !list.isEmpty()) {
            for (DisposalHandleListDTO dto : list) {
                //??????????????????
                if(dto.getCallbackFlag() != null && dto.getCallbackFlag()){
                    dto.setCategoryDesc(DisposalCategoryEnum.getDescByCode(dto.getCategory()) + "??????");
                }else{
                    dto.setCategoryDesc(DisposalCategoryEnum.getDescByCode(dto.getCategory()) + "??????");
                }

                dto.setTypeDesc(DisposalTypeEnum.getDescByCode(dto.getType()));
                dto.setStatusDesc(DisposalHandleStatusEnum.getDescByCode(dto.getStatus()));
                dto.setAction(DisposalActionEnum.getDescByCode(dto.getAction()));
                dto.setCreateTimeDesc(DateUtil.dateToString(dto.getCreateTime(), DateUtil.timeStamp_STANDARD));
                if (dto.getAuditTime() != null) {
                    dto.setAuditTimeDesc(DateUtil.dateToString(dto.getAuditTime(), DateUtil.timeStamp_STANDARD));
                }
            }
        }
        PageInfo<DisposalHandleListDTO> pageInfo = new PageInfo<>(list);
        ResultRO<List<DisposalHandleListDTO>> resultRO = new ResultRO<>(true);
        resultRO.setData(pageInfo.getList());
        resultRO.setTotal(Integer.valueOf(pageInfo.getTotal()+""));
        return resultRO;
    }


    @Override
    public ResultRO createHandle(String auditUser, Long id) {
        if(id == null){
            return new ResultRO(false, "??????????????????");
        }

        DisposalHandleEntity entity = disposalHandleDao.getById(id);
        if(entity == null){
            return new ResultRO(false, "?????????????????????");
        }

        DisposalOrderCenterEntity centerEntity =  disposalOrderCenterDao.getByUuid(entity.getCenterUuid());
        if(centerEntity == null){
            return new ResultRO(false, "?????????????????????");
        }

        logger.info("???????????????type:{},needAuditFlag:{}", DisposalTypeEnum.getDescByCode(centerEntity.getType()), entity.getNeedAuditFlag());

        //??????IP?????????????????????????????????????????????????????????????????????
        if (!centerEntity.getType().equals(DisposalTypeEnum.PATH.getCode())
                && !centerEntity.getType().equals(DisposalTypeEnum.BLACK_IP.getCode())) {
            return new ResultRO(false, "????????????");
        }

        if (!entity.getNeedAuditFlag()) {
            return new ResultRO(false, "????????????");
        }

        //?????????????????????
        entity.setStatus(DisposalHandleStatusEnum.COMPLETED.getCode());
        entity.setAuditUser(auditUser);
        entity.setAuditTime(DateUtil.getCurrentTimestamp());
        disposalHandleDao.update(entity);

        sendHandleStatus(entity.getCenterUuid(), entity.getCallbackFlag());
        logger.info("kafka??????????????????");

        createOrder(entity.getCallbackFlag(), entity.getCenterUuid(), auditUser);
        logger.info("??????????????????");

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("????????????%s??????",centerEntity.getOrderName()));

        return new ResultRO(true);
    }

    @Override
    public ResultRO joinScenes(String auditUser, Long id, List<String> uuidList) {
        if(id == null || uuidList == null || uuidList.isEmpty()){
            return new ResultRO(false, "??????????????????");
        }

        for(String uuid : uuidList){
            DisposalScenesEntity scenesEntity =  disposalScenesService.getByUUId(uuid);
            if(scenesEntity == null){
                return new ResultRO(false, "????????????");
            }
        }


        DisposalHandleEntity entity = disposalHandleDao.getById(id);
        if(entity == null){
            return new ResultRO(false, "?????????????????????");
        }

        DisposalOrderCenterEntity centerEntity =  disposalOrderCenterDao.getByUuid(entity.getCenterUuid());
        if(centerEntity == null){
            return new ResultRO(false, "?????????????????????");
        }

        logger.info("???????????????type:{},needAuditFlag:{}", DisposalTypeEnum.getDescByCode(centerEntity.getType()), entity.getNeedAuditFlag());

        //?????????
        if (!centerEntity.getType().equals(DisposalTypeEnum.MANUAL.getCode())) {
            return new ResultRO(false, "????????????");
        }

        if (!entity.getNeedAuditFlag()) {
            return new ResultRO(false, "????????????");
        }

        //??????????????????
        for(String uuid : uuidList){
            DisposalOrderScenesEntity orderScenes = new DisposalOrderScenesEntity();
            orderScenes.setCenterUuid(entity.getCenterUuid());
            orderScenes.setScenesUuid(uuid);
            disposalOrderScenesMapper.insert(orderScenes);
        }

        //?????????????????????
        entity.setStatus(DisposalHandleStatusEnum.COMPLETED.getCode());
        entity.setAuditUser(auditUser);
        entity.setAuditTime(DateUtil.getCurrentTimestamp());
        disposalHandleDao.update(entity);


        sendHandleStatus(entity.getCenterUuid(), entity.getCallbackFlag());
        logger.info("kafka??????????????????");

        createOrder(entity.getCallbackFlag(), entity.getCenterUuid(), auditUser);
        logger.info("??????????????????");

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("????????????%s??????",centerEntity.getOrderName()));
        return new ResultRO(true);
    }

    /**???kafka????????????????????????**/
    private void sendHandleStatus(String centerUuid, boolean isCallBackFlag) {
        DisposalHandleCallbackDTO dto = new DisposalHandleCallbackDTO();
        dto.setCenterUuid(centerUuid);
        dto.setWorkName(workName);
        dto.setWorkIp(workIp);
        dto.setCallbackFlag(isCallBackFlag);
        kafkaTemplate.send(CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK, dto);
    }

    /**????????????**/
    private void createOrder(Boolean callBackFlag, String centerUuid, String createUser){
        if (AliStringUtils.isEmpty(createUser)) {
            createUser = "system????????????";
        }
        if(callBackFlag != null && callBackFlag){
            //??????
            DisposalRollbackEntity rollbackEntity = new DisposalRollbackEntity();
            rollbackEntity.setCenterUuid(centerUuid);
            rollbackEntity.setPCenterUuid(centerUuid);
            rollbackEntity.setStatus(0);
            rollbackEntity.setCreateUser(createUser);
            rollbackEntity.setCreateTime(DateUtil.getCurrentTimestamp());
            disposalRollbackService.insert(rollbackEntity);

            //?????????id
            String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
            //??????????????????
            disposalRollbackService.startSendDeleteCommandTasks(streamId, centerUuid, createUser);
        }else if(callBackFlag != null && !callBackFlag){
            //??????
            DisposalOrderEntity orderEntity = new DisposalOrderEntity();
            orderEntity.setStatus(0);
            orderEntity.setCreateTime(DateUtil.getCurrentTimestamp());
            orderEntity.setCenterUuid(centerUuid);
            orderEntity.setCreateUser(createUser);
            disposalOrderDao.insert(orderEntity);

            //?????????id
            String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+IdGen.randomBase62(6);
            //??????????????????
            disposalOrderService.startSendCommandTasks(streamId, centerUuid, createUser);
        }
    }
}

