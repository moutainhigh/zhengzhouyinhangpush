package com.abtnetworks.totems.generate.subservice;

import com.abtnetworks.totems.generate.subservice.impl.*;
import com.abtnetworks.totems.generate.subservice.impl.device.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 子服务类型枚举类，用来列举子服务
 * 普通服务键值从0开始设置
 * 设备类型特定服务从100开始设置
 */
@Slf4j
public enum SubServiceEnum {
    //公用服务类枚举变量
    CONVERT_IP_RANGE_TO_SEGMENT(0, "转换IP地址中IP范围为子网", ConvertIpRangeToSegmentCmdServiceImpl.class),
    FORMAT_SERVICE_DTO(1, "格式化服务对象", FormatServiceCmdServiceImpl.class),
    GET_FIRST_POLICY_ID(2, "获取第一条策略ID", GetFirstPolicyIdCmdServiceImpl.class),
    GET_FIRST_POLICY_NAME(3,"获取第一条策略名称", GetFirstPolicyNameCmdServiceImpl.class),
    GET_FIRST_POLICY_NAME_IN_ZONE(4, "获取域间第一条策略名称", GetFirstPolicyNameInZoneCmdServiceImpl.class),
    GET_RULE_LIST_UUID(5, "获取策略集UUID", GetRuleListUuidCmdServiceImpl.class),
    GET_SETTING(6, "获取高级设置基础参数", GetSettingCmdServiceImpl.class),
    SEARCH_UNITARY_EXIST_ADDRESS(7, "整体查找地址对象", SearchUnitaryEixtAddressObjectCmdServiceImpl.class),
    SEARCH_DISCRETE_EXIST_ADDRESS(8, "离散查找地址对象", SearchDiscreteExistAddressCmdServiceImpl.class),
    SEARCH_UNITARY_EXIST_SERVICE(9, "整体查找服务对象", SearchUnitaryExistServiceCmdServiceImpl.class),
    SEARCH_DISCRETE_EXIST_SERVICE(10, "离散查找服务对象", SearchDiscreteExistServiceCmdServiceImpl.class),
    SET_ZONE(11, "设置域相关内容", SetZoneCmdServiceImpl.class),
    DECRETE_SERVICE(12, "拆分服务对象为离散服务对象", DiscreteServiceCmdServiceImpl.class),
    SEARCH_UNITARY_DISCRETE_EXIST_SERVICE(13, "整体和离散查找服务对象", SearchUnitaryAndDiscreteExistServiceObjServiceImpl.class),
    SEARCH_UNITARY_DISCRETE_EXIST_ADDRESS(14, "整体和离散查找地址对象", SearchUnitaryAndDiscreteExistAddressServiceImpl.class),
    GET_SETTING_MOVE(15, "获取高级设置移动参数", GetSettingMoveCmdServiceImpl.class),
    //设备相关服务类枚举变量
    GET_CISCO_INTERFACE_RULE_LIST(100, "获取思科接口上策略集名称", GetCiscoInterfaceRuleListCmdServiceImpl.class),
    GET_FIRST_POLICY_ID_AND_MIN_USABLE_POLICY_ID(101, "获取第一条策略ID以及当前最小空闲策略ID", GetFortinetFirstPolicyIdAndPolicyIdCmdServiceImpl.class),
    GET_VENUSTECH_FIRST_POLICY_ID(102, "获取启明星辰第一条策略ID以及当前策略ID", GetVenustechFirstPolicyIdAndPolicyIdCmdServiceImpl.class),
    HAS_VSYS(103, "查询设备是否为虚墙",HasVsysCmdServiceImpl.class),

    CISCO_SPECIAL_OBJECT_REF(104, "思科8.6版本，对象引用特殊处理", GetCiscoSpecialObjectRefServiceImpl.class),
    GET_TOPSEC_GROUP_NAME(105, "天融信设置分组名称", GetTopsecGroupNameServiceImpl.class),
    GET_CHECKPOINT_LAYER_PACKAGE(106, "高级设置checkpoint 分层和策略包配置", GetCheckPointLayerPackageServiceImpl.class),
    DELETE_EXCESS_ADDRESS_REUSE_NAME(107,"对复用地址对象时加的前缀去掉",DeleteExcessAddressReuseNameServiceImpl.class),
    EDIT_POLICY(108,"编辑命令行",EditPolicySettingServiceImpl.class),
    GET_CISCO_ACL_POLICY_ID(109, "思科ACL策略生成,策略id获取", GetCiscoACLPolicyIdServiceImpl.class),
    GET_RULE_LIST_UUID_AND_MATCH_RULE_ID(110,"策略开通流程acl策略命令行生成策略集uuid和匹配deny策略id获取",GetAclRuleListUuidServiceImpl.class),
    GET_ADDRESS_GROUP_ID(111, "获取地址池中使用的最大的id", GetH3cAddressGroupIdServiceImpl.class),
    GET_ABTNETWORKS_FIRST_POLICY_ID(112, "获取安博通第一条策略ID以及当前最小空闲策略ID", GetAbnetworksFirstPolicyIdAndPolicyIdCmdServiceImpl.class),
    GET_ANHENG_FIRST_POLICY_ID(113, "获取安恒第一条策略ID以及当前最小空闲策略ID", GetAnhengFirstPolicyIdAndPolicyIdCmdServiceImpl.class),
    GET_MAIPU_FIRST_POLICY_ID(114, "获取迈普第一条策略ID以及当前最小空闲策略ID", GetMaipuFirstPolicyIdAndPolicyIdCmdServiceImp.class),
    GET_WESTONE_FIRST_POLICY_ID(115, "获取龙马卫士第一条策略ID以及当前最小空闲策略ID", GetWestoneFirstPolicyIdAndPolicyIdCmdServiceImp.class),
    STATIC_ROUTING_CHECK(116, "静态路由参数针对不同厂商校验", StaticRoutingCheckServiceImpl.class),
    GET_FORTINET_STATICROUTING_FIRST_POLICY_ID(117, "获取飞塔静态路由第一条路由ID以及当前最小空闲路由ID", GetFortinetStatingRoutingFirstPolicyIdAndPolicyIdCmdServiceImp.class),
    GET_FORTINET_VIRTUALIP_NAME(118, "获取飞塔虚拟ip名称", GetFortVirtualIpNameImpl.class),
    GET_CISCO_ACL_NAME(119, "获取复用思科ASA的源Nat策略名称", GetCiscoASANameImpl.class),
    GET_CISCO_POLICY_ID(120, "获取思科ASA的策略id", GetCiscoASAPolicyIdCmdServiceImpl.class),

    ;

    private int code;

    private String desc;

    private Class serviceClass;

    SubServiceEnum(int code, String desc, Class serviceClass) {
        this.code = code;
        this.desc = desc;
        this.serviceClass = serviceClass;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public static SubServiceEnum valueOf(Integer keyValue) {
        for (SubServiceEnum value : SubServiceEnum.values()) {
            Integer tmp = value.getCode();
            if (keyValue.equals(tmp)) {
                return value;
            }
        }
        return null;
    }
}
