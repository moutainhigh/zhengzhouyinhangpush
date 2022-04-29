package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderScenesMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesNodeMapper;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderScenesEntity;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalOrderScenesService;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.push.dao.mysql.PushForbidCommandLineMapper;
import com.abtnetworks.totems.push.dao.mysql.PushForbidIpMapper;
import com.abtnetworks.totems.push.entity.PushForbidCommandLineEntity;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.abtnetworks.totems.push.enums.EnableStatusEnum;
import com.abtnetworks.totems.push.enums.PushForbidIpStatusEnum;
import com.abtnetworks.totems.push.enums.PushStatusEnum;
import com.abtnetworks.totems.push.service.PushForbidIpService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.CommandlineManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Policy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-09-10 18:52
 */
@Slf4j
@Service
public class PushForbidIpServiceImpl implements PushForbidIpService {

    private static Logger logger = LoggerFactory.getLogger(PushForbidIpServiceImpl.class);

    @Autowired
    private PushForbidIpMapper pushForbidIpMapper;

    @Autowired
    private DisposalOrderScenesMapper disposalOrderScenesMapper;

    @Autowired
    private DisposalScenesNodeMapper disposalScenesNodeMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    //生成器相关
    @Autowired
    VendorManager vendorManager;

    //调用命令生成
    @Autowired
    CommandlineManager commandlineManager;

    @Autowired
    private PushForbidCommandLineMapper forbidCommandLineMapper;

    @Autowired
    private DisposalOrderScenesService disposalOrderScenesService;

    @Autowired
    private DisposalScenesMapper disposalScenesMapper;

    @Override
    public ReturnT<String> enable(int id, String enableStatus,String updateUser) {
        // 启用/禁用，修改状态，重新生成命令行
        try{
            PushForbidIpEntity pushForbidIpEntity = pushForbidIpMapper.selectByPrimaryKey(id);
            pushForbidIpEntity.setEnableStatus(enableStatus);
            pushForbidIpEntity.setUpdateUser(updateUser);
            pushForbidIpEntity.setUpdateTime(new Date());
            int num = pushForbidIpMapper.updateByPrimaryKey(pushForbidIpEntity);
            if(num >0){
                if(enableStatus.equalsIgnoreCase(EnableStatusEnum.ENABLE.getCode())){
                    generateCommand(pushForbidIpEntity.getUuid(), PolicyEnum.ENABLE);
                }else if(enableStatus.equalsIgnoreCase(EnableStatusEnum.DISABLE.getCode())){
                    generateCommand(pushForbidIpEntity.getUuid(), PolicyEnum.DISABLE);
                }
            }
            return num>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (Exception e) {
            logger.error("封禁IP id为:{} 启用或禁用异常，异常原因：", id, e);
            return ReturnT.FAIL;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PushForbidIpEntity addOrUpdate(PushForbidIpEntity entity) {
        // 新增时直接保存到库，然后生成命令行
        PushForbidIpEntity returnEntity = new PushForbidIpEntity();
        try{
            if(ObjectUtils.isEmpty(entity.getId())){
                entity.setUuid(IdGen.uuid());
                entity.setStatus(PushForbidIpStatusEnum.INIT.getCode());
                entity.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
                entity.setSerialNumber(generateSerialNumber());
                pushForbidIpMapper.insert(entity);
                returnEntity = pushForbidIpMapper.getByUuid(entity.getUuid());
                saveDisposalOrderScenes(entity);
                generateCommand(entity.getUuid(), PolicyEnum.FORBID);
            } else {
                PushForbidIpEntity oldEntity = pushForbidIpMapper.selectByPrimaryKey(entity.getId());
                entity.setStatus(PushForbidIpStatusEnum.PRE_PUSH.getCode());
                entity.setUpdateTime(new Date());
                entity.setSerialNumber(oldEntity.getSerialNumber());
                entity.setUuid(oldEntity.getUuid());
                entity.setEnableStatus(oldEntity.getEnableStatus());
                entity.setCreateDate(oldEntity.getCreateDate());
                entity.setCreateUser(oldEntity.getCreateUser());
                // 如果编辑场景，先删除原关联场景数据，再添加新场景信息
                if(ObjectUtils.isNotEmpty(entity.getScenesUuidArray())){
                    disposalOrderScenesMapper.deleteByCenterUuid(entity.getUuid());
                    for(String uuid : entity.getScenesUuidArray()){
                        DisposalOrderScenesEntity disposalOrderScenes = new DisposalOrderScenesEntity();
                        disposalOrderScenes.setCenterUuid(entity.getUuid());
                        disposalOrderScenes.setScenesUuid(uuid);
                        disposalOrderScenesMapper.insert(disposalOrderScenes);
                    }
                }
                pushForbidIpMapper.updateByPrimaryKey(entity);
                BeanUtils.copyProperties(entity, returnEntity);
                generateCommand(entity.getUuid(), PolicyEnum.EDIT_FORBID);
            }
            return returnEntity;
        } catch (Exception e) {
            logger.error("新增或编辑封禁IP异常", e);
            return returnEntity;
        }
    }

    @Override
    public PageInfo<PushForbidIpEntity> findList(PushForbidIpEntity entity, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PushForbidIpEntity> list = pushForbidIpMapper.findList(entity);
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(data ->{
                StringBuilder nameSb = new StringBuilder();
                StringBuilder uuidSb = new StringBuilder();
                List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(data.getUuid());
                if(CollectionUtils.isNotEmpty(orderScenesList)){
                    orderScenesList.forEach(orderScenes ->{
                        DisposalScenesEntity scenesEntity = disposalScenesMapper.getByUUId(orderScenes.getScenesUuid());
                        if(scenesEntity != null ){
                            nameSb.append(scenesEntity.getName()).append(",");
                            uuidSb.append(scenesEntity.getUuid()).append(",");
                        }
                    });
                }
                if(StringUtils.isNotBlank(nameSb)){
                    data.setScenesName(nameSb.deleteCharAt(nameSb.length() -1).toString());
                    data.setScenesUuid(uuidSb.deleteCharAt(uuidSb.length() -1).toString());
                }
            });
        }
        PageInfo<PushForbidIpEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PushForbidIpEntity getByUuid(String uuid) {
        PushForbidIpEntity entity = pushForbidIpMapper.getByUuid(uuid);
        return entity;
    }


    /**
     * 生成封禁命令行
     * @param uuid  任务uuid
     * @param policyEnum 操作策略类型
     */
    private void generateCommand(String uuid, PolicyEnum policyEnum) {
        if (StringUtils.isBlank(uuid)) {
            log.error("参数错误，无法生成命令行");
            return;
        }

        //查询任务单
        PushForbidIpEntity entity = pushForbidIpMapper.getByUuid(uuid);
        if (entity == null) {
            log.error("封禁任务单不存在，无法生成命令行");
            return;
        }

        //将任务单的状态改为：命令行生成中
        pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATING.getCode());

        //查询任务单关联的场景
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(uuid);
        if (orderScenesList == null || orderScenesList.isEmpty()) {
            log.error("根据封禁任务uuid查询场景返回空");
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
            return;
        }

        List<String> scenesUuids = new ArrayList<>();
        orderScenesList.stream().forEach(scenesEntity -> scenesUuids.add(scenesEntity.getScenesUuid()));

        //查询场景下的设备
        List<DisposalScenesDTO> scenesList = disposalScenesNodeMapper.findBySceneUuidList(scenesUuids);
        if (scenesList == null || scenesList.isEmpty()) {
            logger.info("查询场景的设备返回空，可能是设备已删除");
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
            return;
        }

        log.info("去重之前，设备数：" + scenesList.size());
        //去重
        scenesList = scenesList.stream().distinct().collect(Collectors.toList());
        log.info("去重之后，设备数：" + scenesList.size());

        //服务any
        List<ServiceDTO> anyServiceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("0");
        serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
        serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
        anyServiceList.add(serviceDTO);

        //转换成命令行对象
        List<CommandlineDTO> commandList = new ArrayList<>(scenesList.size());
        for (DisposalScenesDTO scenesDTO : scenesList) {
            String deviceUuid = scenesDTO.getDeviceUuid();
            CommandlineDTO dto = new CommandlineDTO();
            dto.setSrcIp(entity.getSrcIp());
            dto.setBusinessName(entity.getSerialNumber());
            dto.setDeviceUuid(deviceUuid);
            dto.setAction(ActionEnum.DENY.getKey());
            dto.setSrcZone(scenesDTO.getSrcZoneName());
            dto.setSrcItf(scenesDTO.getSrcZoneName());
            dto.setSrcItfAlias(scenesDTO.getSrcZoneName());

            dto.setDstZone(scenesDTO.getDstZoneName());
            dto.setDstItf(scenesDTO.getDstZoneName());
            dto.setDstItfAlias(scenesDTO.getDstZoneName());

            commandList.add(dto);
        }

        int commandCount = 0;
        //转换成通用的CmdDTO
        for (CommandlineDTO dto : commandList) {
            //生成命令行
            String deviceUuid = dto.getDeviceUuid();

            NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error("根据设备uuid查询返回空，可能是设备已删除,uuid:{}", deviceUuid);
                continue;
            }

            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
            if (deviceRO == null || !deviceRO.getSuccess()) {
                logger.error("根据设备uuid查询mongoDB返回空，可能是设备已删除,uuid:{}", deviceUuid);
                continue;
            }

            PolicyEnum currentPolicyEnum = policyEnum;

            //需要查询该设备，最近一次下发成功的记录
            PushForbidCommandLineEntity lastOk = null;
            if (!currentPolicyEnum.getKey().equals(PolicyEnum.FORBID.getKey())) {
                lastOk = forbidCommandLineMapper.getLastSuccessByUuid(uuid, deviceUuid, PushStatusEnum.PUSH_STATUS_ENUM.getCode());
                //非新增业务，但查不到历史下发成功的记录，则命令行生成业务为 “新增”
                if (lastOk == null) {
                    log.info("非新增，查询历史下发成功的记录为空，变更业务为新增，生成封禁命令行");
                    currentPolicyEnum = PolicyEnum.FORBID;
                }
            }

            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            Boolean isVsys = deviceDataRO.getIsVsys();
            //是虚墙
            if (isVsys != null && isVsys) {
                String vSysName = deviceDataRO.getVsysName();
                dto.setVsys(true);
                dto.setVsysName(vSysName);
            }

            //目的IP是any
            dto.setDstIp(null);
            //服务是any
            dto.setServiceList(anyServiceList);


            CmdDTO cmdDTO = new CmdDTO();

            //设备
            com.abtnetworks.totems.common.dto.DeviceDTO device = cmdDTO.getDevice();
            device.setDeviceUuid(deviceUuid);
            device.setVsys(dto.isVsys());
            device.setVsysName(dto.getVsysName());
            device.setHasVsys(dto.isHasVsys());
            device.setModelNumber(DeviceModelNumberEnum.fromString(node.getModelNumber()));
            device.setNodeEntity(node);

            //策略
            PolicyDTO policyDTO = cmdDTO.getPolicy();
            policyDTO.setType(currentPolicyEnum);

            policyDTO.setSrcIp(dto.getSrcIp());
            policyDTO.setDstIp(dto.getDstIp());
            policyDTO.setServiceList(dto.getServiceList());
            policyDTO.setSrcZone(dto.getSrcZone());
            policyDTO.setDstZone(dto.getDstZone());

            policyDTO.setSrcItf(dto.getSrcItf());
            policyDTO.setDstItf(dto.getDstItf());

            policyDTO.setSrcItfAlias(dto.getSrcItfAlias());
            policyDTO.setDstItfAlias(dto.getDstItfAlias());


            if (dto.getAction().equalsIgnoreCase(ActionEnum.PERMIT.getKey())) {
                policyDTO.setAction(ActionEnum.PERMIT);
            } else {
                policyDTO.setAction(ActionEnum.DENY);
            }

            //任务
            TaskDTO taskDTO = cmdDTO.getTask();
            taskDTO.setTheme(dto.getBusinessName());

            //高级设置  默认置顶、并且创建对象
            SettingDTO settingDTO = cmdDTO.getSetting();
            settingDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
            settingDTO.setCreateObject(true);

            //生成的对象
            GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
            if (lastOk != null) {
                generatedObject.setPolicyName(lastOk.getPolicyName());
                generatedObject.setSrcObjectName(lastOk.getSrcObjectName());
            }

            //设置生成器
            vendorManager.getGenerator(policyDTO.getType(), cmdDTO.getDevice(), cmdDTO.getProcedure());

            if (cmdDTO.getProcedure().getGenerator() == null) {
                logger.error("无法生成该设备的命令行，生成器不存在，型号, model:{}", node.getModelNumber());
                continue;
            }

            //执行生成器
            String command = commandlineManager.generate(cmdDTO);
            logger.info("生成的命令行信息:" + command);

            //保存初始下发记录-命令行到表
            PushForbidCommandLineEntity forbidRecord = new PushForbidCommandLineEntity();
            forbidRecord.setSrcIp(entity.getSrcIp());
            forbidRecord.setDeviceUuid(deviceUuid);
            forbidRecord.setForbidIpUuid(uuid);
            forbidRecord.setCommandType(currentPolicyEnum.getKey());
            forbidRecord.setCommandline(command);
            forbidRecord.setPushStatus(PushStatusEnum.PUSH_NOT_START.getCode());
            if (StringUtils.isNotBlank(generatedObject.getPolicyName())) {
                forbidRecord.setPolicyName(generatedObject.getPolicyName());
            }
            if (StringUtils.isNotBlank(generatedObject.getSrcObjectName())) {
                forbidRecord.setSrcObjectName(generatedObject.getSrcObjectName());
            }
            forbidCommandLineMapper.insert(forbidRecord);
            commandCount++;

        }


        //修改封禁任务状态：命令行生成成功
        if (commandCount == 0) {
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
        } else {
            //是禁用
            if (policyEnum.getKey().equals(PolicyEnum.DISABLE.getKey())) {
                pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PRE_PUSH.getCode());
            } else if (policyEnum.getKey().equals(PolicyEnum.ENABLE.getKey())) {
                pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PRE_PUSH.getCode());
            } else if (policyEnum.getKey().equals(PolicyEnum.EDIT_FORBID.getKey())) {
                pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PRE_PUSH.getCode());
            } else {
                pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PRE_PUSH.getCode());
            }


        }
    }

    /**
     * 获取当前封禁IP流水号
     * DI+ yyMMdd + 3位流水号
     * @return
     */
    private String generateSerialNumber(){
        StringBuilder serialNumSb = new StringBuilder();
        serialNumSb.append("DI");
        String currentDate = DateUtils.getDate().replace("-","");
        String date = currentDate.substring(2, currentDate.length());
        serialNumSb.append(date);
        String currentNum;
        List<PushForbidIpEntity> entityList = pushForbidIpMapper.findSerialNumber(serialNumSb.toString());
        if(CollectionUtils.isEmpty(entityList)){
            currentNum = "1";
        } else {
            currentNum = String.valueOf(entityList.size()+ 1);
        }
        String serialNum = StringUtils.leftPad(currentNum, 3, "0");
        serialNumSb.append(serialNum);
        return serialNumSb.toString();
    }

    /**
     * 保存场景
     * @param entity
     */
    private void saveDisposalOrderScenes(PushForbidIpEntity entity){
        String[] scenesUuids = entity.getScenesUuidArray();
        if(ObjectUtils.isNotEmpty(scenesUuids)){
            for(String scenesUuid : scenesUuids){
                DisposalOrderScenesEntity disposalOrderScenes = new DisposalOrderScenesEntity();
                disposalOrderScenes.setCenterUuid(entity.getUuid());
                disposalOrderScenes.setScenesUuid(scenesUuid);
                disposalOrderScenesService.insert(disposalOrderScenes);
            }
        }
    }

}
