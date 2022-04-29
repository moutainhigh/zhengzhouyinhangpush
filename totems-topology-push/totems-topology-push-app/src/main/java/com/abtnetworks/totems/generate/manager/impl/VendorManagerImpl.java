package com.abtnetworks.totems.generate.manager.impl;

import com.abtnetworks.totems.common.annotation.GenerateCliBuilder;
import com.abtnetworks.totems.common.config.DeviceConfig;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.common.dto.ProcedureDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.generate.manager.VendorManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VendorManagerImpl implements VendorManager {

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Override
    public void getVendorInfo(CmdDTO cmdDTO) {
        DeviceDTO deviceDTO = cmdDTO.getDevice();

        //获取设备信息
        setDeviceInfo(deviceDTO);

        //获取策略信息
        PolicyDTO policy = cmdDTO.getPolicy();

        //获取策略生成流程数据
        ProcedureDTO procedure = cmdDTO.getProcedure();

        //设置设备命令行生成流程
        setInternalProcedure(policy.getType(), deviceDTO, procedure);

    }

    /**
     * 设置设备相关数据信息
     * @param device 设备数据
     */
    public void setDeviceInfo(DeviceDTO device) {
        String deviceUuid = device.getDeviceUuid();

        NodeEntity nodeEntity = recommendTaskManager.getTheNodeByUuid(deviceUuid);

        device.setNodeEntity(nodeEntity);

        DeviceModelNumberEnum modelNumber = DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());

        device.setModelNumber(modelNumber);
    }

    /**
     * 设置内部定义策略生成流程
     *
     * @param policyType 策略类型
     * @param device 设备信息
     * @param procedure 策略生成流程数据
     */
    protected void setInternalProcedure(PolicyEnum policyType, DeviceDTO device, ProcedureDTO procedure) {
        List<String> procedureList = new ArrayList<>();
        switch(policyType) {
            case SECURITY:
                getSecurityProcedure(device, procedure);
                break;
            case EDIT_SECURITY:
                getSecurityProcedure(device, procedure);
                break;
            case ACL:
                getAclProcedure(device, procedure);
                break;
            case F5_DNAT:
            case F5_BOTH_NAT:
                getFiveNatProcedure(device, procedure);
                break;
            case STRTIC_ROUTING:
                getRoutingProcedure(device, procedure);
                break;
            default:
                getNatProcedure(device, procedure,policyType);
        }
    }

    /**
     * 获取f5策略生成步骤列表
     * @param deviceDTO
     * @param procedureDTO
     */
    protected void getFiveNatProcedure(DeviceDTO deviceDTO, ProcedureDTO procedureDTO){
        List<Integer> fiveNatSteps = new ArrayList<>();
        // 针对于第一版只做snatpool地址复用
        Integer procedure = getSearchAddressObject(deviceDTO.getModelNumber());
        if(procedure != null) {
            fiveNatSteps.add(procedure);
        }
        procedureDTO.setSteps(fiveNatSteps);
    }

    /**
     * 获取静态路由生成步骤列表
     * @param deviceDTO
     * @param procedureDTO
     */
    protected void getRoutingProcedure(DeviceDTO deviceDTO, ProcedureDTO procedureDTO){
        List<Integer> staticRoutingSteps = new ArrayList<>();
        Integer procedure = getStaticRoutingSteps(deviceDTO.getModelNumber());
        if(procedure != null) {
            staticRoutingSteps.add(procedure);
        }
        procedure = getMaxIdStaticRoutingSteps(deviceDTO.getModelNumber());
        if(procedure != null) {
            staticRoutingSteps.add(procedure);
        }
        procedureDTO.setSteps(staticRoutingSteps);
    }

    /**
     * 获取acl策略生成步骤列表
     * @param deviceDTO
     * @param procedureDTO
     */
    protected void getAclProcedure(DeviceDTO deviceDTO, ProcedureDTO procedureDTO){
        List<Integer> aclSteps = new ArrayList<>();
        aclSteps.addAll(getGeneralService(deviceDTO.getModelNumber()));
        Integer procedure = getSearchAddressObject(deviceDTO.getModelNumber());
        if(procedure != null) {
            aclSteps.add(procedure);
        }
        procedure = getSearchExistServiceProcedure(deviceDTO.getModelNumber());
        if(procedure != null) {
            aclSteps.add(procedure);
        }
        // 针对思科路由交换设备acl策略下发  策略生成流程策略集uuid和策略集名称获取
        // 这里拿ruleListName判断是仿真开通(仿真开通在前面的流程已经有获取ruleListName,不需要再去查询处理), 还是策略生成
        if(StringUtils.isBlank(deviceDTO.getRuleListName())){
            procedure = getRuleListUuidByACLRule(deviceDTO.getModelNumber());
            if(procedure != null) {
                aclSteps.add(procedure);
            }
        }
        // 添加acl路由交换ruleId的前置处理
        procedure = getFirstPolicyNameIdByACLRule(deviceDTO.getModelNumber());
        if(procedure != null) {
            aclSteps.add(procedure);
        }
        procedureDTO.setSteps(aclSteps);
    }
    /**
     * 获取安全策略生成步骤列表
     *
     * @param deviceDTO 设备信息数据
     * @param procedureDTO 生成步骤列表数据
     */
    protected void getSecurityProcedure(DeviceDTO deviceDTO, ProcedureDTO procedureDTO){
        List<Integer> steps = new ArrayList<>();
        Integer procedure = null;

        steps.addAll(getGeneralService(deviceDTO.getModelNumber()));

        procedure = getSearchAddressObject(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        procedure = getSearchExistServiceProcedure(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        procedure = getFirstPolicyNameId(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }
        //高级设置放在获取第一个策略的后面，代表配置了高级设置，以用高级设置覆盖，获取到的第一个策略id、name
        steps.add(SubServiceEnum.GET_SETTING_MOVE.getCode());
        steps.addAll(getExtraProcedure(deviceDTO.getModelNumber()));


        //飞塔 获取虚拟ip名称 看是否能复用
        procedure = getVirtualIpName(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        procedureDTO.setSteps(steps);
    }

    /**
     * 获取NAT策略生成步骤列表
     *
     * @param deviceDTO 设备信息数据
     * @param procedureDTO 生成步骤列表数据
     */
    protected void getNatProcedure(DeviceDTO deviceDTO, ProcedureDTO procedureDTO,PolicyEnum policyType) {
        List<Integer> steps = new ArrayList<>();

        steps.addAll(getGeneralService(deviceDTO.getModelNumber()));

        Integer procedure = getSearchAddressObject(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        // 飞塔nat 策略取策略id
        //高级设置放在获取第一个策略的后面，代表配置了高级设置，以用高级设置覆盖，获取到的第一个策略id、name
        steps.add(SubServiceEnum.GET_SETTING_MOVE.getCode());
        procedure = getNatFirstPolicyNameId(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        procedure = getNatHasVsys(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }


        procedure = getSearchExistServiceProcedure(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        //h3c nat设备获取地址池最大id
        procedure = getNatAddressGroupId(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        //飞塔 获取虚拟ip名称 看是否能复用
        procedure = getVirtualIpName(deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        //思科ASA 获取ACL策略名称 看是否能复用
        procedure = getSNatNameSteps(policyType,deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        //思科ASA 获取id
        procedure = getSNatPolicyIdSteps(policyType,deviceDTO.getModelNumber());
        if(procedure != null) {
            steps.add(procedure);
        }

        steps.addAll(getExtraNatProcedure(deviceDTO.getModelNumber()));

        procedureDTO.setSteps(steps);
    }

    /**
     * Nat策略获取设备第一条策略名称步骤
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getNatFirstPolicyNameId(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case FORTINET:
            case FORTINET_V5:
            case FORTINET_V5_2:
            case KFW:
                return SubServiceEnum.GET_FIRST_POLICY_ID_AND_MIN_USABLE_POLICY_ID.getCode();
            case WESTONE:
                return SubServiceEnum.GET_WESTONE_FIRST_POLICY_ID.getCode();
            default:
                return null;
        }
    }

    /**
     * Nat策略是否查询虚墙
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getNatHasVsys(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case FORTINET:
            case FORTINET_V5_2:
                return SubServiceEnum.HAS_VSYS.getCode();
            default:
                return null;
        }
    }

    /**
     * 获取基本通用流程服务
     *
     * @param modelNumber 设备型号数据
     * @return 基本通用流程列表
     */
    protected List<Integer> getGeneralService(DeviceModelNumberEnum modelNumber) {
        List<Integer> generalList = new ArrayList<>();
        generalList.add(SubServiceEnum.FORMAT_SERVICE_DTO.getCode());
        generalList.add(SubServiceEnum.GET_RULE_LIST_UUID.getCode());
        generalList.add(SubServiceEnum.GET_SETTING.getCode());
        generalList.add(SubServiceEnum.SET_ZONE.getCode());
        return generalList;
    }

    /**
     * 获取服务对象复用查询流程
     * @param modelNumber 设备型号
     * @return 流程名称
     */
    @Override
    public Integer getSearchExistServiceProcedure(DeviceModelNumberEnum modelNumber) {
        if(!recommendTaskManager.isUseCurrentObject()) {
            log.info("当前不进行服务对象复用");
            switch(modelNumber) {
                case HILLSTONE:
                case TOPSEC_NG:
                case TOPSEC_TOS_005:
                case TOPSEC_TOS_010_020:
                case FORTINET:      //飞塔设备加入离散对象生成
                case FORTINET_V5_2:
                case LEGEND_SEC_NSG:
                case LEAD_SEC_POWER_V:
                case LEGEND_SEC_GATE:
                case USG6000:
                    log.info("{}设备需要拆散服务为离散对象", modelNumber.getDesc());
                    return SubServiceEnum.DECRETE_SERVICE.getCode();
                default:
                    return null;
            }
        }
        switch(modelNumber) {
            default:
                return SubServiceEnum.SEARCH_UNITARY_DISCRETE_EXIST_SERVICE.getCode();
        }
    }

    /**
     * 获取地址对象复用服务,
     * 山石和飞塔进行离散地址对象复用，
     * @param modelNumber 设备型号
     * @return 地址对象查找服务
     */
    @Override
    public Integer getSearchAddressObject(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber){

            default:
                return SubServiceEnum.SEARCH_UNITARY_DISCRETE_EXIST_ADDRESS.getCode();
        }
    }

    private Integer getStaticRoutingSteps(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber){

            default:
                return SubServiceEnum.STATIC_ROUTING_CHECK.getCode();
        }
    }

    private Integer getMaxIdStaticRoutingSteps(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber){

            case FORTINET:
            case FORTINET_V5_2:
                return SubServiceEnum.GET_FORTINET_STATICROUTING_FIRST_POLICY_ID.getCode();
            default:
                return null;
        }
    }


    private Integer getSNatPolicyIdSteps(PolicyEnum policyType,DeviceModelNumberEnum modelNumber) {

        switch (policyType){
            case SNAT:
                switch(modelNumber){

                    case CISCO:
                        return SubServiceEnum.GET_CISCO_POLICY_ID.getCode();
                    default:
                        return null;
                }
            default:
                return null;
        }

    }

    private Integer getSNatNameSteps(PolicyEnum policyType,DeviceModelNumberEnum modelNumber) {

        switch (policyType){
            case SNAT:
                switch(modelNumber){

                    case CISCO:
                        return SubServiceEnum.GET_CISCO_ACL_NAME.getCode();
                    default:
                        return null;
                }
            default:
                return null;
        }

    }

    /**
     * 获取取得设备第一条策略名称步骤
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getFirstPolicyNameId(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case SRX:
            case SRX_NoCli:
                return SubServiceEnum.GET_FIRST_POLICY_NAME_IN_ZONE.getCode();
            case DPTECHR003:
            case DPTECHR004:
                return SubServiceEnum.GET_FIRST_POLICY_NAME.getCode();
            case VENUSTECHVSOS:
            case VENUSTECHVSOS_V263:
                return SubServiceEnum.GET_VENUSTECH_FIRST_POLICY_ID.getCode();
            case FORTINET:
            case FORTINET_V5:
            case FORTINET_V5_2:
            case KFW:
                return SubServiceEnum.GET_FIRST_POLICY_ID_AND_MIN_USABLE_POLICY_ID.getCode();
            case ABTNETWORKS:
            case SDNWARE:
                return SubServiceEnum.GET_ABTNETWORKS_FIRST_POLICY_ID.getCode();
            case WESTONE:
                return SubServiceEnum.GET_WESTONE_FIRST_POLICY_ID.getCode();
            case ANHENG:
                return SubServiceEnum.GET_ANHENG_FIRST_POLICY_ID.getCode();
            case MAIPUMSGFIREWALL:
                return SubServiceEnum.GET_MAIPU_FIRST_POLICY_ID.getCode();
            case H3CV7:
                return SubServiceEnum.GET_FIRST_POLICY_ID.getCode();
            default:
                return null;
        }
    }

    /**
     * 获取取得设备第一条策略名称步骤
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getFirstPolicyNameIdByACLRule(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case CISCO_IOS:
            case CISCO_NX_OS:
            case RUIJIE:
                return SubServiceEnum.GET_CISCO_ACL_POLICY_ID.getCode();
            default:
                return null;
        }
    }

    /**
     * 获取取得设备第一条策略名称步骤
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getRuleListUuidByACLRule(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case CISCO_IOS:
            case CISCO_NX_OS:
            case RUIJIE:
            case SRX:
            case SRX_NoCli:
            case JUNIPER_ROUTER:
                return SubServiceEnum.GET_RULE_LIST_UUID_AND_MATCH_RULE_ID.getCode();
            default:
                return null;
        }
    }

    /**
     * 获取设备特定处理步骤
     *
     * @param modelnumber 设备型号
     * @return 特定处理步骤key值列表
     */
    protected List<Integer> getExtraProcedure(DeviceModelNumberEnum modelnumber) {
        List<Integer> extraList = new ArrayList<>();
        switch(modelnumber) {
            case SSG:
                extraList.add(SubServiceEnum.CONVERT_IP_RANGE_TO_SEGMENT.getCode());
            case CISCO:
                extraList.add(SubServiceEnum.GET_CISCO_INTERFACE_RULE_LIST.getCode());
                extraList.add(SubServiceEnum.CONVERT_IP_RANGE_TO_SEGMENT.getCode());
                break;
            case CISCO_ASA_86:
                extraList.add(SubServiceEnum.GET_CISCO_INTERFACE_RULE_LIST.getCode());
                extraList.add(SubServiceEnum.CISCO_SPECIAL_OBJECT_REF.getCode());
                break;
            case CISCO_ASA_99:
                extraList.add(SubServiceEnum.GET_CISCO_INTERFACE_RULE_LIST.getCode());
                extraList.add(SubServiceEnum.CISCO_SPECIAL_OBJECT_REF.getCode());
//                extraList.add(SubServiceEnum.CONVERT_IP_RANGE_TO_SEGMENT.getCode());
                break;
            case FORTINET:
            case FORTINET_V5_2:
                extraList.add(SubServiceEnum.HAS_VSYS.getCode());
                break;
            case TOPSEC_NG:
            case TOPSEC_TOS_005:
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG2:
            case TOPSEC_NG3:
            case TOPSEC_NG4:
                extraList.add(SubServiceEnum.GET_TOPSEC_GROUP_NAME.getCode());
                break;
            case CHECK_POINT:
                extraList.add(SubServiceEnum.GET_CHECKPOINT_LAYER_PACKAGE.getCode());
                break;
        }
        return extraList;
    }

    /**
     * 获取设备特定处理步骤
     *
     * @param modelnumber 设备型号
     * @return 特定处理步骤key值列表
     */
    protected List<Integer> getExtraNatProcedure(DeviceModelNumberEnum modelnumber) {
        List<Integer> extraList = new ArrayList<>();
        switch(modelnumber) {
            case CISCO:
                extraList.add(SubServiceEnum.CONVERT_IP_RANGE_TO_SEGMENT.getCode());
                break;
        }
        return extraList;
    }

    /**
     * h3c nat设备获取地址池最大id
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getNatAddressGroupId(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case H3CV7:
                return SubServiceEnum.GET_ADDRESS_GROUP_ID.getCode();
            default:
                return null;
        }
    }

    /**
     *  获取虚拟ip名称
     * @param modelNumber 设备型号
     * @return 步骤名称key值
     */
    protected Integer getVirtualIpName(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            case FORTINET:
            case FORTINET_V5:
            case FORTINET_V5_2:
                return SubServiceEnum.GET_FORTINET_VIRTUALIP_NAME.getCode();
            default:
                return null;
        }
    }

    @Override
    @GenerateCliBuilder
    public void getGenerator(PolicyEnum policyType, DeviceDTO device, ProcedureDTO procedure) {
        DeviceModelNumberEnum modelNumberEnum = device.getModelNumber();
        String generator = null;
        Class generatorClass = null;
        Class rollbackClass = null;
        switch(policyType) {
            case SECURITY:
                generatorClass = modelNumberEnum.getSecurityClass();
                rollbackClass = modelNumberEnum.getRollbackSecurityClass();
                break;
            case SNAT:
                generatorClass = modelNumberEnum.getSnatClass();
                rollbackClass = modelNumberEnum.getRollbackSnatClass();
                break;
            case DNAT:
                generatorClass = modelNumberEnum.getDnatClass();
                rollbackClass = modelNumberEnum.getRollbackDnatClass();
                break;
            case STATIC:
                generatorClass = modelNumberEnum.getStaticNatClass();
                rollbackClass = modelNumberEnum.getRollbackStaticNatClass();
                break;
            case BOTH:
                generatorClass = modelNumberEnum.getBothNatClass();
                rollbackClass = modelNumberEnum.getRollbackBothNatClass();
                break;
            case ACL:
                generatorClass = modelNumberEnum.getAclClass();
                rollbackClass = modelNumberEnum.getRollbackAclClass();
                break;
            case ROUTING:
                generatorClass = modelNumberEnum.getRoutingClass();
                rollbackClass = modelNumberEnum.getRollbakcRoutingClass();
                break;
            case FORBID:
                generatorClass = modelNumberEnum.getForbidClass();
                break;
            case EDIT_FORBID:
                generatorClass = modelNumberEnum.getEditForbidClass();
                break;
            case DISABLE:
                generatorClass = modelNumberEnum.getDisableSecurityClass();
                break;
            case ENABLE:
                generatorClass = modelNumberEnum.getEnableSecurityClass();
                break;
            case EDIT_SECURITY:
                generatorClass = modelNumberEnum.getEditSecurityClass();
                rollbackClass = modelNumberEnum.getEditRollbackSecurityClass();
                break;
            case F5_DNAT:
                generatorClass = modelNumberEnum.getBalanceDnatClass();
                rollbackClass = modelNumberEnum.getRollbackBalanceDnatClass();
                break;
            case F5_BOTH_NAT:
                generatorClass = modelNumberEnum.getBalanceBothNatClass();
                rollbackClass = modelNumberEnum.getRollbackBalanceBothNatClass();
                break;
            case STRTIC_ROUTING:
                generatorClass = modelNumberEnum.getStaticRoutingClass();
                rollbackClass = modelNumberEnum.getRollbackStaticRoutingClass();
                break;
        }

        if(generatorClass != null) {
            String name = NameUtils.getServiceDefaultName(generatorClass);
            generator = NameUtils.firstLowerCase(name);
            log.info("命令行生成器为{}", generator);
            procedure.setGenerator(generator);
        }

        if(rollbackClass != null) {
            String name = NameUtils.getServiceDefaultName(rollbackClass);
            generator = NameUtils.firstLowerCase(name);
            log.info("回滚命令行生成器为{}", generator);
            procedure.setRollbackGenerator(generator);
        }
    }

    @Override
    public boolean isDisassembleIps(DeviceModelNumberEnum modelNumber) {
        switch(modelNumber) {
            /**
             * 此需求源于中信银行，主要因为Cisco对每个IP单独建立策略导致生成策略过大，
             * 因此希望最后生成的时候进行合并，以减少Cisco命令行生成条数。
             */
            case CISCO:
                return false;
            /**
             * 默认拆解，因为用户输入原则，用户输入单个IP，生成的策略最后就应该是单个IP
             */
            default:
                return true;
        }
    }
}
