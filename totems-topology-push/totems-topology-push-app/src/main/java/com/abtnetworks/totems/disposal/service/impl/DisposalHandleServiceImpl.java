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
 * 分级协作
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
     * 新增
     */
    @Override
    @Transactional
    @KafkaListener(topics = CommonConfigParam.STR_TOPIC_ASSIGN_BRANCH)
    public ReturnT<String> insert(String jsonStr) {
        if (jsonStr == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        try {
            logger.info("派发过来的参数信息,jsonStr:{}", jsonStr);
            DisposalOrderDTO dto = JSONObject.parseObject(jsonStr, DisposalOrderDTO.class);
            //单位过滤
            String branchNames = "," + dto.getBranchNames() + ",";
            if (!branchNames.contains("," + workName + ",")) {
                logger.info("收到总部派发下来的信息，没有本单位，跳过处理,branchNames:{} ", branchNames);
                return ReturnT.SUCCESS;
            }

            //校验参数
            if (StringUtils.isBlank(dto.getOrderNo()) || dto.getCategory() == null
                    || dto.getType() == null || dto.getSendType() == null) {
                logger.error("派发参数不能为空，请检查json");
                return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
            }


            //策略封堵
            if(dto.getCategory().equals(DisposalCategoryEnum.POLICY.getCode())){

                //黑IP，只校验srcIp、dstIp
                if(dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode())){
                    if(StringUtils.isBlank(dto.getSrcIp()) && StringUtils.isBlank(dto.getDstIp())){
                        logger.error("策略封堵-派发参数错误，当类型为黑IP时，必须指定封堵的IP地址");
                        return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
                    }
                }else if(dto.getType().equals(DisposalTypeEnum.PATH.getCode()) || dto.getType().equals(DisposalTypeEnum.MANUAL.getCode())){
                    //是路径或手动封堵，校验五元组
                    if (StringUtils.isBlank(dto.getAction())) {
                        logger.error("策略封堵-派发参数错误, 当工单类型为路径或手动时，动作不能为空");
                        return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
                    }

                }else{
                    logger.error("派发参数错误, 工单类型非法, type:{}", dto.getType());
                    return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
                }

                //校验五元组
                if (StringUtils.isNotBlank(dto.getSrcIp())) {
                    int rc = InputValueUtils.checkIp(dto.getSrcIp());
                    //若出IP范围起始地址大于终止地址错误，则自动纠正
                    if (rc == ReturnCode.INVALID_IP_RANGE) {
                        dto.setSrcIp(InputValueUtils.autoCorrect(dto.getSrcIp()));
                    } else if (rc != ReturnCode.POLICY_MSG_OK) {
                        rc = InputValueUtils.checkIpV6(dto.getSrcIp());
                        if (rc != ReturnCode.POLICY_MSG_OK) {
                            String msg = "源地址错误！" + ReturnCode.getMsg(rc);
                            logger.error(msg);
                            return new ReturnT(ReturnT.FAIL_CODE, msg);
                        }
                    }

                    //修正无用的空地址
                    dto.setSrcIp(InputValueUtils.formatIpAddress(dto.getSrcIp()));
                }

                if (StringUtils.isNotBlank(dto.getDstIp())) {
                    int rc = InputValueUtils.checkIp(dto.getDstIp());
                    //若出IP范围起始地址大于终止地址错误，则自动纠正
                    if (rc == ReturnCode.INVALID_IP_RANGE) {
                        dto.setDstIp(InputValueUtils.autoCorrect(dto.getDstIp()));
                    } else if (rc != ReturnCode.POLICY_MSG_OK) {
                        rc = InputValueUtils.checkIpV6(dto.getDstIp());
                        if (rc != ReturnCode.POLICY_MSG_OK) {
                            String msg = "目的地址错误！" + ReturnCode.getMsg(rc);
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
                            //仅校验端口即可
                            service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                        }
                    }
                    dto.setServiceList(ServiceDTOUtils.toString(serviceList));
                }

            }else if(dto.getCategory().equals(DisposalCategoryEnum.ROUT.getCode())){
                //是路由封堵
                if (!dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode()) && !dto.getType().equals(DisposalTypeEnum.MANUAL.getCode())) {
                    logger.error("路由封堵-派发参数错误，type:{}值非法", dto.getType());
                    return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
                }
                if (StringUtils.isBlank(dto.getRoutingIp())) {
                    logger.error("路由封堵-派发参数错误，必须指定路由IP地址");
                    return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
                }

                //校验IP格式
                int rc = InputValueUtils.checkIp(dto.getRoutingIp());
                //若IP范围起始错误，则自动纠正
                if (rc == ReturnCode.INVALID_IP_RANGE) {
                    dto.setRoutingIp(InputValueUtils.autoCorrect(dto.getRoutingIp()));
                } else if (rc != ReturnCode.POLICY_MSG_OK) {
                    rc = InputValueUtils.checkIpV6(dto.getRoutingIp());
                    if (rc != ReturnCode.POLICY_MSG_OK) {
                        String msg = "地址错误！" + ReturnCode.getMsg(rc);
                        logger.error(msg);
                        return new ReturnT(ReturnT.FAIL_CODE, msg);
                    }
                }

                dto.setRoutingIp(InputValueUtils.formatIpAddress(dto.getRoutingIp()));

            }else{
                logger.error("派发参数错误, 业务类型非法, category:{}", dto.getCategory());
                return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
            }

            if(dto.getCallbackFlag() == null){
                dto.setCallbackFlag(false);
                logger.info("派发过来的回滚标识为空，则默认为封堵");
            }

            logger.info("派发参数校验完成");


            //是封堵才插入，回滚的内容是一样的，跳过即可
            if (!dto.getCallbackFlag()) {
                DisposalOrderCenterEntity center = new DisposalOrderCenterEntity();
                BeanUtils.copyProperties(dto, center);
                center.setUuid(dto.getCenterUuid());
                center.setSourceClassification(1);
                disposalOrderCenterDao.insert(center);
                logger.info("保存工单内容完毕");
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
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("添加分级协作%s成功",dto.getOrderName()));
            logger.info("保存工单处置信息完毕");

            //自动处置工单，只支持黑IP和路径
            if(dto.getSendType().equals(DisposalSendTypeEnum.AUTO.getCode()) &&
                    (dto.getType().equals(DisposalTypeEnum.PATH.getCode()) || dto.getType().equals(DisposalTypeEnum.BLACK_IP.getCode()) )){

                entity.setStatus(DisposalHandleStatusEnum.AUTO_COMPLETED.getCode());
                int count = disposalHandleDao.update(entity);

                if(count == 1){
                    sendHandleStatus(entity.getCenterUuid(), dto.getCallbackFlag());
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("自动处置工单%s成功",dto.getOrderName()));
                    logger.info("kafka发送处置状态");
                }

                createOrder(dto.getCallbackFlag(), entity.getCenterUuid(), dto.getCreateUser());
                logger.info("保存工单完毕");

            }

        }catch (Exception e){
            logger.error("从总部派发过来的工单转换对象错误，请检查参数,jsonStr:{}", jsonStr);
            return new ReturnT<>(ReturnT.FAIL_CODE, "参数错误");
        }


        ReturnT<String> returnT = new ReturnT(ReturnT.SUCCESS_CODE);
        return returnT;
    }


    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalHandleDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalHandleEntity disposalHandle) {
        int ret = disposalHandleDao.update(disposalHandle);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalHandleEntity getById(Long id) {
        return disposalHandleDao.getById(id);
    }

    /**
     * 查询 get
     */
    @Override
    public DisposalHandleEntity get(DisposalHandleEntity disposalHandle) {
        return disposalHandleDao.get(disposalHandle);
    }

    /**
     * 分页查询
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
                //处置枚举显示
                if(dto.getCallbackFlag() != null && dto.getCallbackFlag()){
                    dto.setCategoryDesc(DisposalCategoryEnum.getDescByCode(dto.getCategory()) + "回滚");
                }else{
                    dto.setCategoryDesc(DisposalCategoryEnum.getDescByCode(dto.getCategory()) + "下发");
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
            return new ResultRO(false, "参数不能为空");
        }

        DisposalHandleEntity entity = disposalHandleDao.getById(id);
        if(entity == null){
            return new ResultRO(false, "请核对后再操作");
        }

        DisposalOrderCenterEntity centerEntity =  disposalOrderCenterDao.getByUuid(entity.getCenterUuid());
        if(centerEntity == null){
            return new ResultRO(false, "请核对后再操作");
        }

        logger.info("审核对象，type:{},needAuditFlag:{}", DisposalTypeEnum.getDescByCode(centerEntity.getType()), entity.getNeedAuditFlag());

        //是黑IP、路径，需要审核，即人工点击【生成处置单】按钮
        if (!centerEntity.getType().equals(DisposalTypeEnum.PATH.getCode())
                && !centerEntity.getType().equals(DisposalTypeEnum.BLACK_IP.getCode())) {
            return new ResultRO(false, "非法操作");
        }

        if (!entity.getNeedAuditFlag()) {
            return new ResultRO(false, "非法操作");
        }

        //修改协作单状态
        entity.setStatus(DisposalHandleStatusEnum.COMPLETED.getCode());
        entity.setAuditUser(auditUser);
        entity.setAuditTime(DateUtil.getCurrentTimestamp());
        disposalHandleDao.update(entity);

        sendHandleStatus(entity.getCenterUuid(), entity.getCallbackFlag());
        logger.info("kafka发送处置状态");

        createOrder(entity.getCallbackFlag(), entity.getCenterUuid(), auditUser);
        logger.info("保存工单完毕");

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("处置工单%s完成",centerEntity.getOrderName()));

        return new ResultRO(true);
    }

    @Override
    public ResultRO joinScenes(String auditUser, Long id, List<String> uuidList) {
        if(id == null || uuidList == null || uuidList.isEmpty()){
            return new ResultRO(false, "场景不能为空");
        }

        for(String uuid : uuidList){
            DisposalScenesEntity scenesEntity =  disposalScenesService.getByUUId(uuid);
            if(scenesEntity == null){
                return new ResultRO(false, "场景错误");
            }
        }


        DisposalHandleEntity entity = disposalHandleDao.getById(id);
        if(entity == null){
            return new ResultRO(false, "请核对后再操作");
        }

        DisposalOrderCenterEntity centerEntity =  disposalOrderCenterDao.getByUuid(entity.getCenterUuid());
        if(centerEntity == null){
            return new ResultRO(false, "请核对后再操作");
        }

        logger.info("审核对象，type:{},needAuditFlag:{}", DisposalTypeEnum.getDescByCode(centerEntity.getType()), entity.getNeedAuditFlag());

        //是场景
        if (!centerEntity.getType().equals(DisposalTypeEnum.MANUAL.getCode())) {
            return new ResultRO(false, "非法操作");
        }

        if (!entity.getNeedAuditFlag()) {
            return new ResultRO(false, "非法操作");
        }

        //保存场景信息
        for(String uuid : uuidList){
            DisposalOrderScenesEntity orderScenes = new DisposalOrderScenesEntity();
            orderScenes.setCenterUuid(entity.getCenterUuid());
            orderScenes.setScenesUuid(uuid);
            disposalOrderScenesMapper.insert(orderScenes);
        }

        //修改协作单状态
        entity.setStatus(DisposalHandleStatusEnum.COMPLETED.getCode());
        entity.setAuditUser(auditUser);
        entity.setAuditTime(DateUtil.getCurrentTimestamp());
        disposalHandleDao.update(entity);


        sendHandleStatus(entity.getCenterUuid(), entity.getCallbackFlag());
        logger.info("kafka发送处置状态");

        createOrder(entity.getCallbackFlag(), entity.getCenterUuid(), auditUser);
        logger.info("保存工单完毕");

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("处置工单%s完成",centerEntity.getOrderName()));
        return new ResultRO(true);
    }

    /**往kafka里面发送协作状态**/
    private void sendHandleStatus(String centerUuid, boolean isCallBackFlag) {
        DisposalHandleCallbackDTO dto = new DisposalHandleCallbackDTO();
        dto.setCenterUuid(centerUuid);
        dto.setWorkName(workName);
        dto.setWorkIp(workIp);
        dto.setCallbackFlag(isCallBackFlag);
        kafkaTemplate.send(CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK, dto);
    }

    /**创建工单**/
    private void createOrder(Boolean callBackFlag, String centerUuid, String createUser){
        if (AliStringUtils.isEmpty(createUser)) {
            createUser = "system自动执行";
        }
        if(callBackFlag != null && callBackFlag){
            //回滚
            DisposalRollbackEntity rollbackEntity = new DisposalRollbackEntity();
            rollbackEntity.setCenterUuid(centerUuid);
            rollbackEntity.setPCenterUuid(centerUuid);
            rollbackEntity.setStatus(0);
            rollbackEntity.setCreateUser(createUser);
            rollbackEntity.setCreateTime(DateUtil.getCurrentTimestamp());
            disposalRollbackService.insert(rollbackEntity);

            //数据流id
            String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
            //执行回滚命令
            disposalRollbackService.startSendDeleteCommandTasks(streamId, centerUuid, createUser);
        }else if(callBackFlag != null && !callBackFlag){
            //封堵
            DisposalOrderEntity orderEntity = new DisposalOrderEntity();
            orderEntity.setStatus(0);
            orderEntity.setCreateTime(DateUtil.getCurrentTimestamp());
            orderEntity.setCenterUuid(centerUuid);
            orderEntity.setCreateUser(createUser);
            disposalOrderDao.insert(orderEntity);

            //数据流id
            String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+IdGen.randomBase62(6);
            //开始执行下发
            disposalOrderService.startSendCommandTasks(streamId, centerUuid, createUser);
        }
    }
}

