package com.abtnetworks.totems.mapping.manager.impl;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.exception.BusinessException;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.mapping.common.AutoMappingExecutor;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingIpMapper;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingTaskMapper;
import com.abtnetworks.totems.mapping.dto.AddressPoolDTO;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingNatTypeEnum;
import com.abtnetworks.totems.mapping.enums.AutoMappingTaskStatusEnum;
import com.abtnetworks.totems.mapping.enums.CustomRuleTypeEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.manager.AutoMappingProcessManager;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.mapping.vo.OrderCheckVO;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @desc    ?????????????????????????????????????????????
 * @author liuchanghao
 * @date 2022-01-20 20:55
 */
@Service
@Log4j2
public class AutoMappingProcessManagerImpl implements AutoMappingProcessManager {


    @Autowired
    private PushAutoMappingTaskMapper pushAutoMappingTaskMapper;

    @Autowired
    Map<String, AutoMappingExecutor> autoMappingExecutorMap;

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    private PushAutoMappingIpMapper pushAutoMappingIpMapper;

    private static Properties props;
    private static InputStreamReader in = null;

    static{
        try {
            props = new Properties();
            in = new InputStreamReader(AutoMappingProcessManagerImpl.class.getResourceAsStream("/application-mapping.properties"), "utf-8");
            props.load(in);
        } catch (FileNotFoundException e) {
            log.error("?????????application-mapping.properties????????????");
        } catch (Exception e) {
            log.error("????????????????????????????????????????????????????????????", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("?????????????????????????????????", e);
                }
            }
        }
    }


    @Override
    public AutoMappingTaskResultDTO generateAutoMappingTask(RuleProcessDTO dto) throws Exception {
        AutoMappingTaskResultDTO autoMappingTaskResultDTO = null;
        RuleTypeTaskEnum ruleTypeTaskEnum = dto.getRuleTypeTaskEnum();
        // 1.????????????????????????????????????
        CustomRuleTypeEnum customRuleTypeEnum = this.getConfig(ruleTypeTaskEnum.getKey());
        // 2.?????????????????????
        String autoMappingExecutor = this.getExecutor(customRuleTypeEnum, dto.getRuleTypeTaskEnum());

        if (autoMappingExecutorMap.containsKey(autoMappingExecutor)) {
            // 3.????????????
            autoMappingTaskResultDTO = autoMappingExecutorMap.get(autoMappingExecutor).matchAndGenerateAutoMappingTask(dto);
        }
        return autoMappingTaskResultDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PushAutoMappingTaskEntity> generateAutoMappingProcessTask(OrderCheckVO checkVO,PageInfo<PushAutoMappingPoolEntity> pageInfo,PushAutoMappingSceneRuleEntity sceneRuleEntity,
                                                                          Authentication auth, boolean isAppointPostSrcIp) throws Exception {
        List<PushAutoMappingTaskEntity> pushAutoMappingTaskEntities = new ArrayList<>();

        try {

            String ruleType = sceneRuleEntity.getRuleType();
            String[] ruleTypes = ruleType.split(",");
            JSONArray ruleJsonArray = new JSONArray();
            JSONArray natArray = new JSONArray();
            JSONArray routeArray = new JSONArray();
            int natIndex = 1;
            int routeIndex = 1;
            String postSrcIp = null;
            String preDstIp = null;

            boolean createRuleInfo = false;
            for (String currentRuleType : ruleTypes) {
                RuleTypeTaskEnum ruleTypeTask = RuleTypeTaskEnum.getRuleTypeTaskEnumByCode(currentRuleType);
                // 1.????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????nat?????????????????????????????????
                if (isAppointPostSrcIp && (ruleTypeTask.getCode() == 1 || ruleTypeTask.getCode() == 2)) {
                    log.info("????????????????????????????????????????????????????????????{}", ruleTypeTask.getDesc());
                    continue;
                }
                createRuleInfo = true;
                RuleProcessDTO dto = new RuleProcessDTO();
                AddressPoolDTO poolDTO = new AddressPoolDTO();
                poolDTO.setIpPoolList(pageInfo.getList());
                dto.setRuleTypeTaskEnum(ruleTypeTask);
                dto.setAddressPoolDTO(poolDTO);
                dto.setSrcIp(checkVO.getSrcIp());
                dto.setDstIp(checkVO.getDstIp());
                dto.setServiceList(checkVO.getServiceList());
                dto.setAuth(auth);
                dto.setTheme(checkVO.getTheme());
                // 2.????????????
                AutoMappingTaskResultDTO taskResultDTO = this.generateAutoMappingTask(dto);
                log.info("??????:{}????????????????????????:{}", checkVO.getTheme(), JSONObject.toJSONString(taskResultDTO));
                // 3. ???????????????????????????????????????????????????????????????????????????
                if (ObjectUtils.isNotEmpty(taskResultDTO.getNatId())) {
                    JSONObject natJson = new JSONObject();
                    natJson.put("index", natIndex);
                    natJson.put("name", taskResultDTO.getNatTheme() + "(" + taskResultDTO.getDeviceName() + "(" + taskResultDTO.getDeviceIp() + "))");
                    natJson.put("natTheme", taskResultDTO.getNatTheme());
                    natJson.put("taskId", taskResultDTO.getNatId());
                    natJson.put("ruleType", taskResultDTO.getRuleTypeTaskEnum().getCode());
                    natJson.put("ruleId", sceneRuleEntity.getId());
                    natJson.put("ruleName", sceneRuleEntity.getRuleName());
                    if (StringUtils.containsIgnoreCase(taskResultDTO.getNatTheme(), "_SNAT")) {
                        natJson.put("type", 6);
                        postSrcIp = taskResultDTO.getPostSrcIp();
                    } else {
                        natJson.put("type", 7);
                        preDstIp = taskResultDTO.getPreDstIp();
                    }
                    natArray.add(natJson);
                    natIndex++;
                }

                if (ObjectUtils.isNotEmpty(taskResultDTO.getRouteId())) {
                    JSONObject routeJson = new JSONObject();
                    routeJson.put("index", routeIndex);
                    routeJson.put("name", taskResultDTO.getNatTheme() + "(" + taskResultDTO.getDeviceName() + "(" + taskResultDTO.getDeviceIp() + "))");
                    routeJson.put("routeTheme", taskResultDTO.getRouteTheme());
                    routeJson.put("taskId", taskResultDTO.getRouteId());
                    routeJson.put("ruleType", taskResultDTO.getRuleTypeTaskEnum().getCode());
                    routeJson.put("ruleId", sceneRuleEntity.getId());
                    routeJson.put("ruleName", sceneRuleEntity.getRuleName());
                    routeJson.put("type", 20);
                    routeArray.add(routeJson);
                    routeIndex++;
                }

            }
            // ?????????????????????????????????????????????????????????????????????nat??????
            if (isAppointPostSrcIp){
                createRuleInfo = true;
                postSrcIp = createSnatForAppointPostSrc(checkVO, pageInfo, auth, natArray, natIndex);
            }

            // ??????????????????????????????????????????????????????
            if(createRuleInfo){

                if (ObjectUtils.isNotEmpty(sceneRuleEntity.getId())) {
                    JSONObject ruleJson = new JSONObject();
                    ruleJson.put("ruleType", sceneRuleEntity.getRuleType());
                    ruleJson.put("ruleId", sceneRuleEntity.getId());
                    ruleJson.put("ruleName", sceneRuleEntity.getRuleName());
                    ruleJsonArray.add(ruleJson);
                }

                PushAutoMappingTaskEntity record = new PushAutoMappingTaskEntity();
                BeanUtils.copyProperties(checkVO, record);
                if (CollectionUtils.isNotEmpty(checkVO.getServiceList())) {
                    record.setServiceList(JSONObject.toJSONString(checkVO.getServiceList()));
                }
                record.setAdditionInfo(ruleJsonArray.toJSONString());
                record.setRelevancyNat(natArray.toJSONString());
                record.setRelevancyRoute(routeArray.toJSONString());
                // ??????????????????????????????????????????????????????????????????????????????( ???????????????????????????????????????????????????(dnat) ?????????????????????(dnat))
                record.setSrcIp(checkVO.getSrcIp());
                record.setDstIp(checkVO.getDstIp());
                record.setPostSrcIp(StringUtils.isBlank(postSrcIp) ? checkVO.getSrcIp() : postSrcIp);
                record.setPostDstIp(StringUtils.isBlank(preDstIp) ? checkVO.getDstIp() : preDstIp);
                record.setStatus(AutoMappingTaskStatusEnum.WAIT_RECOMMEND.getCode());
                record.setUuid(IdGen.uuid());
                record.setUserName(auth.getName());
                record.setServiceList(JSON.toJSONString(checkVO.getServiceList()));
                record.setCreateTime(new Date());
                record.setCreateUser(auth.getName());
                pushAutoMappingTaskMapper.insert(record);
                record = pushAutoMappingTaskMapper.getByUuid(record.getUuid());
                pushAutoMappingTaskEntities.add(record);
                log.info("??????:{}?????????????????????:{}", checkVO.getTheme(), JSONObject.toJSONString(pushAutoMappingTaskEntities));
            }
            return pushAutoMappingTaskEntities;
        } catch (BusinessException e) {
            log.error("??????:{}??????????????????,????????????:{}", checkVO.getTheme(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("??????:{}??????????????????,????????????:{}", checkVO.getTheme(), e);
            throw e;
        }
    }

    /**
     * ???????????????nat??????
     * @param checkVO
     * @param pageInfo
     * @param auth
     * @param natArray
     * @param natIndex
     * @return
     * @throws Exception
     */
    private String createSnatForAppointPostSrc(OrderCheckVO checkVO, PageInfo<PushAutoMappingPoolEntity> pageInfo, Authentication auth, JSONArray natArray, int natIndex) throws Exception {
        TotemsReturnT createSnatForAppointPostSrcIpResult = this.createSnatForAppointPostSrcIp(checkVO, pageInfo, auth);
        // ?????????nat??????????????????
        if(createSnatForAppointPostSrcIpResult.getCode() == TotemsReturnT.FAIL_CODE ){
            throw new BusinessException(SendErrorEnum.ADDRESS_NOT_FULLY_CONTAINED);
        }

        AutoMappingTaskResultDTO appointPostSrcIpResult = (AutoMappingTaskResultDTO)createSnatForAppointPostSrcIpResult.getData();

        JSONObject natJson = new JSONObject();
        natJson.put("index", natIndex + 1);
        natJson.put("name", appointPostSrcIpResult.getNatTheme() +"(" + appointPostSrcIpResult.getNodeEntity().getDeviceName() + "(" + appointPostSrcIpResult.getNodeEntity().getIp() + "))");
        natJson.put("taskId", appointPostSrcIpResult.getNatId());
        natJson.put("natTheme", checkVO.getTheme() + "_SNAT");
        natJson.put("type", 6);
        natJson.put("ruleType", RuleTypeTaskEnum.SNAT_MANT_TO_ONE.getCode());
        natArray.add(natJson);
        //????????????
        String postSrcIp = checkVO.getAppointPostSrcIp();
        // 1.??????????????????ip?????????ip???????????????
        PushAutoMappingIpEntity queryEntity = new PushAutoMappingIpEntity();
        queryEntity.setDeviceUuid(appointPostSrcIpResult.getNodeEntity().getUuid());
        queryEntity.setNatType(AutoMappingNatTypeEnum.SNAT.getCode());
        queryEntity.setPostIp(postSrcIp);
        List<PushAutoMappingIpEntity> result = pushAutoMappingIpMapper.findIpMappingByEntity(queryEntity);
        if(CollectionUtils.isEmpty(result)){
            // 2.?????????????????? ????????????????????????IP????????????
            PushAutoMappingIpEntity mappingIpEntity = new PushAutoMappingIpEntity();
            mappingIpEntity.setUuid(IdGen.uuid());
            mappingIpEntity.setNatType(AutoMappingNatTypeEnum.SNAT.getCode());
            mappingIpEntity.setPreIp(checkVO.getSrcIp());
            mappingIpEntity.setPostIp(checkVO.getAppointPostSrcIp());
            mappingIpEntity.setDeviceUuid(appointPostSrcIpResult.getNodeEntity().getUuid());
            mappingIpEntity.setDeviceIp(appointPostSrcIpResult.getNodeEntity().getIp());
            mappingIpEntity.setDeviceName(appointPostSrcIpResult.getNodeEntity().getDeviceName());
            mappingIpEntity.setCreateUser(auth.getName());
            mappingIpEntity.setCreateTime(new Date());
            pushAutoMappingIpMapper.insert(mappingIpEntity);
        }else{
            // 3.????????????????????????????????????????????????????????????????????????????????????????????????????????????
            PushAutoMappingIpEntity existPostSrcEntity = result.get(0);
            String existPreIp = existPostSrcEntity.getPreIp();
            String[]  existPreIps = existPreIp.split(PolicyConstants.ADDRESS_SEPERATOR);
            String[] targetIps = checkVO.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(existPreIps));

            // ???????????????????????????????????????????????????????????????,????????????????????????ip?????????????????????????????????srcIp,???????????????ip?????????????????????
            if (CollectionUtils.isNotEmpty(tmp.getFilterOutData()) && CollectionUtils.isEmpty(tmp.getPostFilterData())) {
                return postSrcIp;
            }
            // ???????????????????????????????????????????????????????????????????????????ip?????????
            if (CollectionUtils.isNotEmpty(tmp.getPostFilterData())) {
                List<String> postFilterData = tmp.getPostFilterData();
                String newPreIP = existPreIp + PolicyConstants.ADDRESS_SEPERATOR + StringUtils.join(postFilterData, PolicyConstants.ADDRESS_SEPERATOR);
                existPostSrcEntity.setPreIp(newPreIP);
                pushAutoMappingIpMapper.updateByPrimaryKeySelective(existPostSrcEntity);
            }
        }
        return postSrcIp;
    }

    @Override
    public String getExecutor(CustomRuleTypeEnum customRuleTypeEnum, RuleTypeTaskEnum ruleTypeTaskEnum) {

        String executor = "";
        Class executorClass = null;
        switch (ruleTypeTaskEnum){
            case DNAT_ONE_TO_ONE:
            case SNAT_MANT_TO_ONE:
            case SNAT_ONE_TO_ONE:
            case STATIC_ROUTING:
                switch (customRuleTypeEnum){
                    case DEFAULT:
                        executorClass = ruleTypeTaskEnum.getDefaultClass();
                        break;
                    case CUSTOM_V2:
                        executorClass = ruleTypeTaskEnum.getV2Class();
                        break;
                }
                break;
        }

        if(executorClass != null) {
            String name = NameUtils.getServiceDefaultName(executorClass);
            executor = NameUtils.firstLowerCase(name);
            log.info("????????????????????????{}", executor);
        }
        return executor;
    }

    /**
     * ??????????????????
     * @param ruleTypeTask
     * @return
     */
    private CustomRuleTypeEnum getConfig(String ruleTypeTask){
        String customRuleTypeConfig = props.getProperty(ruleTypeTask);
        if(StringUtils.isNotBlank(customRuleTypeConfig)){
            CustomRuleTypeEnum customRuleTypeEnum = CustomRuleTypeEnum.getRuleTypeEnumByDesc(customRuleTypeConfig);
            return customRuleTypeEnum;
        }
        return null;
    }

    /**
     * ????????????????????????????????????nat????????????
     * @param checkVO
     * @param pageInfo
     * @param auth
     */
    private TotemsReturnT createSnatForAppointPostSrcIp(OrderCheckVO checkVO, PageInfo<PushAutoMappingPoolEntity> pageInfo, Authentication auth) throws Exception{
        AutoMappingTaskResultDTO taskResultDTO = new AutoMappingTaskResultDTO();

        RuleProcessDTO dto = new RuleProcessDTO();
        AddressPoolDTO poolDTO = new AddressPoolDTO();
        poolDTO.setIpPoolList(pageInfo.getList());
        dto.setAddressPoolDTO(poolDTO);
        dto.setRuleTypeTaskEnum(RuleTypeTaskEnum.SNAT_MANT_TO_ONE);
        dto.setSrcIp(checkVO.getSrcIp());
        dto.setDstIp(checkVO.getDstIp());
        dto.setServiceList(checkVO.getServiceList());

        boolean validateResult = MappingUtils.validateOrderAndAddressPool(dto);
        if (!validateResult) {
            log.error("??????????????????ip??????,???????????????????????????????????????,???????????????");
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, ReturnCode.getMsg(ReturnCode.ADDRESS_NOT_FULLY_CONTAINED));
        }
        PushAutoMappingPoolEntity entity = dto.getAddressPoolDTO().getMatchPools().get(0);

        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        BeanUtils.copyProperties(checkVO, sNatPolicyDTO);
        sNatPolicyDTO.setPostIpAddress(checkVO.getAppointPostSrcIp());
        sNatPolicyDTO.setTheme(checkVO.getTheme() + "_SNAT");
        sNatPolicyDTO.setDeviceUuid(entity.getDeviceUuid());
        sNatPolicyDTO.setSrcZone(entity.getSrcZone());
        sNatPolicyDTO.setDstZone(entity.getDstZone());
        sNatPolicyDTO.setSrcItf(entity.getInDevIf());
        sNatPolicyDTO.setDstItf(entity.getOutDevIf());
        taskService.insertSrcNatPolicy(sNatPolicyDTO,auth);

        taskResultDTO.setNatId(sNatPolicyDTO.getTaskId());
        taskResultDTO.setNatTheme(sNatPolicyDTO.getTheme());
        taskResultDTO.setPostSrcIp(checkVO.getAppointPostSrcIp());
        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(entity.getDeviceUuid());
        if(ObjectUtils.isNotEmpty(nodeEntity)){
            taskResultDTO.setNodeEntity(nodeEntity);
        }
        return new TotemsReturnT(taskResultDTO);
    }


}
