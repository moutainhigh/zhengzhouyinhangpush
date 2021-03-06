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

    //???????????????
    @Autowired
    VendorManager vendorManager;

    //??????????????????
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
        // ??????/?????????????????????????????????????????????
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
            logger.error("??????IP id???:{} ???????????????????????????????????????", id, e);
            return ReturnT.FAIL;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PushForbidIpEntity addOrUpdate(PushForbidIpEntity entity) {
        // ???????????????????????????????????????????????????
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
                // ??????????????????????????????????????????????????????????????????????????????
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
            logger.error("?????????????????????IP??????", e);
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
     * ?????????????????????
     * @param uuid  ??????uuid
     * @param policyEnum ??????????????????
     */
    private void generateCommand(String uuid, PolicyEnum policyEnum) {
        if (StringUtils.isBlank(uuid)) {
            log.error("????????????????????????????????????");
            return;
        }

        //???????????????
        PushForbidIpEntity entity = pushForbidIpMapper.getByUuid(uuid);
        if (entity == null) {
            log.error("????????????????????????????????????????????????");
            return;
        }

        //????????????????????????????????????????????????
        pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATING.getCode());

        //??????????????????????????????
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(uuid);
        if (orderScenesList == null || orderScenesList.isEmpty()) {
            log.error("??????????????????uuid?????????????????????");
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
            return;
        }

        List<String> scenesUuids = new ArrayList<>();
        orderScenesList.stream().forEach(scenesEntity -> scenesUuids.add(scenesEntity.getScenesUuid()));

        //????????????????????????
        List<DisposalScenesDTO> scenesList = disposalScenesNodeMapper.findBySceneUuidList(scenesUuids);
        if (scenesList == null || scenesList.isEmpty()) {
            logger.info("?????????????????????????????????????????????????????????");
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
            return;
        }

        log.info("???????????????????????????" + scenesList.size());
        //??????
        scenesList = scenesList.stream().distinct().collect(Collectors.toList());
        log.info("???????????????????????????" + scenesList.size());

        //??????any
        List<ServiceDTO> anyServiceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("0");
        serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
        serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
        anyServiceList.add(serviceDTO);

        //????????????????????????
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
        //??????????????????CmdDTO
        for (CommandlineDTO dto : commandList) {
            //???????????????
            String deviceUuid = dto.getDeviceUuid();

            NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error("????????????uuid??????????????????????????????????????????,uuid:{}", deviceUuid);
                continue;
            }

            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
            if (deviceRO == null || !deviceRO.getSuccess()) {
                logger.error("????????????uuid??????mongoDB????????????????????????????????????,uuid:{}", deviceUuid);
                continue;
            }

            PolicyEnum currentPolicyEnum = policyEnum;

            //?????????????????????????????????????????????????????????
            PushForbidCommandLineEntity lastOk = null;
            if (!currentPolicyEnum.getKey().equals(PolicyEnum.FORBID.getKey())) {
                lastOk = forbidCommandLineMapper.getLastSuccessByUuid(uuid, deviceUuid, PushStatusEnum.PUSH_STATUS_ENUM.getCode());
                //??????????????????????????????????????????????????????????????????????????????????????? ????????????
                if (lastOk == null) {
                    log.info("???????????????????????????????????????????????????????????????????????????????????????????????????");
                    currentPolicyEnum = PolicyEnum.FORBID;
                }
            }

            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            Boolean isVsys = deviceDataRO.getIsVsys();
            //?????????
            if (isVsys != null && isVsys) {
                String vSysName = deviceDataRO.getVsysName();
                dto.setVsys(true);
                dto.setVsysName(vSysName);
            }

            //??????IP???any
            dto.setDstIp(null);
            //?????????any
            dto.setServiceList(anyServiceList);


            CmdDTO cmdDTO = new CmdDTO();

            //??????
            com.abtnetworks.totems.common.dto.DeviceDTO device = cmdDTO.getDevice();
            device.setDeviceUuid(deviceUuid);
            device.setVsys(dto.isVsys());
            device.setVsysName(dto.getVsysName());
            device.setHasVsys(dto.isHasVsys());
            device.setModelNumber(DeviceModelNumberEnum.fromString(node.getModelNumber()));
            device.setNodeEntity(node);

            //??????
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

            //??????
            TaskDTO taskDTO = cmdDTO.getTask();
            taskDTO.setTheme(dto.getBusinessName());

            //????????????  ?????????????????????????????????
            SettingDTO settingDTO = cmdDTO.getSetting();
            settingDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
            settingDTO.setCreateObject(true);

            //???????????????
            GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
            if (lastOk != null) {
                generatedObject.setPolicyName(lastOk.getPolicyName());
                generatedObject.setSrcObjectName(lastOk.getSrcObjectName());
            }

            //???????????????
            vendorManager.getGenerator(policyDTO.getType(), cmdDTO.getDevice(), cmdDTO.getProcedure());

            if (cmdDTO.getProcedure().getGenerator() == null) {
                logger.error("???????????????????????????????????????????????????????????????, model:{}", node.getModelNumber());
                continue;
            }

            //???????????????
            String command = commandlineManager.generate(cmdDTO);
            logger.info("????????????????????????:" + command);

            //????????????????????????-???????????????
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


        //????????????????????????????????????????????????
        if (commandCount == 0) {
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.GENERATE_FAIL.getCode());
        } else {
            //?????????
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
     * ??????????????????IP?????????
     * DI+ yyMMdd + 3????????????
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
     * ????????????
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
