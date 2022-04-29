package com.abtnetworks.totems.common.constants;
/**
 * @author Administrator
 * @Title:
 * @Description: <p> 封堵常量 {@link DisposalConstants }
 *  策略仿真常量 {@link PolicyConstants}
 *  生成常量 {@link GenerateConstants}
 *  高级设置常量 {@link AdvancedSettingsConstants}
 *  公共类常量{@link CommonConstants}
 *  凭据常量{@link CredentialConstants}
 *  下发常量{@link PushConstants}
 *
 * </p>
 * @date 2021/1/26
 */
public class AdvancedSettingsConstants {

    //下发时间锁设置
    public static final String PARAM_NAME_CONFIG_PUSH_TIME_LOCK = "push_time_lock";

    //是否创建对象设置
    public static final String PARAM_NAME_CREATE_OBJECT = "create_object";

    //是否创建策略设置
    public static final String PARAM_NAME_CREATE_RULES = "create_rules";

    // 移动冲突策略前
    public static final String PARAM_NAME_MOVE_BEFORECONFLICT_RULE = "move_rule_before_conflict";

    //是否移动策略到最上
    public static final String PARAM_NAME_MOVE_RULE_TOP = "move_rule_top";

    //移动策略到某条之前
    public static final String PARAM_NAME_MOVE_RULE_BEFORE = "move_rule_before";

    //移动策略到某条之后
    public static final String PARAM_NAME_MOVE_RULE_AFTER = "move_rule_after";

    //是否配置源域
    public static final String PARAM_NAME_CONFIG_SRC_ZONE = "config_src_zone";

    //是否配置目的域
    public static final String PARAM_NAME_CONFIG_DST_ZONE = "config_dst_zone";

    //源域目的域均不配置
    public static final String PARAM_NAME_CONFIG_NO_ZONE = "config_no_zone";

    //是否使用现有对象
    public static final String PARAM_NAME_CONFIG_USE_CURRENT_OBJECT = "use_current_object";

    public static final String PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT = "use_address_object";

    //思科ACL策略是否配置到出接口上
    public static final String PARAM_NAME_CISCO_ACL_OUT_INTERFACE = "cisco_out_itf";

    public static final String PARAM_NAME_CHECK_RULE = "config_check_rule";

    public static final String PARAM_NAME_CHECK_RISK = "config_check_risk";

    public static final String PARAM_NAME_ABTNETWORKS_POLICY_ID = "abtnetworks_policy_id";

    public static final String PARAM_NAME_ABTNETWORKS6_POLICY_ID = "abtnetworks6_policy_id";

    public static final String PARAM_NAME_SDNWARE_POLICY_ID = "sdnware_policy_id";

    public static final String PARAM_NAME_SDNWARE6_POLICY_ID = "sdnware6_policy_id";

    public static final String PARAM_NAME_ANHENG_POLICY_ID = "anheng_policy_id";

    public static final String PARAM_NAME_ANHENG6_POLICY_ID = "anheng6_policy_id";

    public static final String PARAM_NAME_MAIPU_POLICY_ID = "maipu_policy_id";

    public static final String PARAM_NAME_MAIPU6_POLICY_ID = "maipu6_policy_id";

    public static final String PARAM_NAME_WESTONE_POLICY_ID = "westone_policy_id";

    public static final String PARAM_NAME_WESTONE6_POLICY_ID = "westone6_policy_id";

    public static final String PARAM_NAME_WESTONE_SRC_NAT_POLICY_ID = "westone_src_nat_policy_id";
    public static final String PARAM_NAME_WESTONE_DST_NAT_POLICY_ID = "westone_dst_nat_policy_id";
    public static final String PARAM_NAME_WESTONE_STATIC_NAT_POLICY_ID = "westone_static_nat_policy_id";

    public static final String PARAM_NAME_WESTONE6_NAT_POLICY_ID = "westone6_nat_policy_id";

    public static final String PARAM_NAME_FORTINET_POLICY_ID = "fortinet_policy_id";

    public static final String PARAM_NAME_CISCO_ASA_POLICY_ID = "cisco_asa_policy_id";

    public static final String PARAM_NAME_FORTINET_STATIC_ROUTING_ID = "fortinet_static_route_id";

    public static final String PARAM_NAME_CISCO_ROUTE_POLICY_ID = "cisco_route_policy_id";

    //天融信分组设置
    public static final String PARAM_NAME_TOPSEC_GROUP_NAME = "topsec_group_name";

    //主备双活设置
    public static final String PARAM_NAME_ACTIVE_STANDBY = "active_standby";

    //主备灾备中心设置
    public static final String PARAM_NAME_DISASTER_RECOVERY = "disaster_recovery";

    /**checkPoint layer package 命令设置关键字**/
    public static final String PARAM_NAME_CHECK_POINT = "layer_package_name";

    public static final String PARAM_NAME_CHECK_EDIT_POLICY = "check_edit_policy";

    //山石防火墙回滚命令行时根据策略名称或策略ID
    public static final String PARAM_NAME_ROLLBACK_TYPE = "hillstone_rollback_type";

    //juniper防火墙是全局地址还是安全域地址来生成命令行
    public static final String PARAM_NAME_GLOBLE_OR_SECURITY = "juniper_globle_or_security";

    //华3V7地址池id记录(当前系统使用最大值)
    public static final String PARAM_NAME_H3V7_ADDRESS_GROUP_ID = "h3v7address_group_id";

    //用户是否接收邮件设置
    public static final String PARAM_NAME_USER_RECEIVE_EMAIL = "user_receive_email";

    //管理员邮箱
    public static final String PARAM_NAME_MANAGER_EMAIL = "manager_email";

    // 管理下发py文件
    public static final String PARAM_NAME_PYTHON_FILE_UPLOAD = "manager_push_py_file";


    //设置值
    public static final String PARAM_VALUE_TRUE = "true";

    public static final String PARAM_VALUE_FALSE = "false";

    public static final String IS_CREATE_OBJECT_VALUE = "0";

    public static final String IS_REFERENCE_CONTENT_VALUE = "1";

    public static final String IS_CREATE_RULE_VALUE = "0";

    public static final String IS_MERGE_RULE_VALUE = "1";

    public static final String IS_SKIP_CHECK_RULE = "0";

    public static final String IS_CHECK_RULE = "1";

    public static final String IS_SKIP_CHECK_RISK = "0";

    public static final String IS_CHECK_RISK = "1";

    public static final String IS_USE_CURRENT = "1";

    public static final String IS_CREATE_NEW = "0";

    public static final String IS_SECURITY = "0";

    public static final String IS_RECEIVE_EMAIL = "1";

    public static final Integer PARAM_INT_SET_BOTH_ZONE = 0;

    public static final Integer PARAM_INT_SET_SRC_ZONE = 1;

    public static final Integer PARAM_INT_SET_DST_ZONE = 2;

    public static final Integer PARAM_INT_SET_NO_ZONE = 3;

    public static final Integer PARAM_INT_CISCO_POLICY_IN_DIRECTION = 0;

    public static final Integer PARAM_INT_CISCO_POLICY_OUT_DIRECTION = 1;

    public static final Integer PARAM_INT_CREATE_OBJECT = 0;

    public static final Integer PARAM_INT_REFERENCE_CONTENT = 1; //NOT create object

    public static final Integer PARAM_INT_CREATE_RULE = 0;

    public static final Integer PARAM_INT_MERGE_RULE = 1;

    public static final Integer PARAM_INT_MOVE_POLICY_FIRST = 0;

    public static final Integer PARAM_INT_NOT_MOVE_POLICY = 1;

    public static final Integer PARAM_INT_MOVE_POLICY_BEFORE = 2;

    public static final Integer PARAM_INT_MOVE_POLICY_AFTER = 3;


}
