package com.abtnetworks.totems.common.constants;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 11:51
 * @Description: <p> 封堵常量 {@link DisposalConstants }
  *  策略仿真常量 {@link PolicyConstants}
  *  生成常量 {@link GenerateConstants}
  *  高级设置常量 {@link AdvancedSettingsConstants}
  *  公共类常量{@link CommonConstants}
  *  凭据常量{@link CredentialConstants}
  *  下发常量{@link PushConstants}
 */
public class PolicyConstants {
    /*****/
    public static final String POLICY_STR_NETWORK_TYPE_IP4 = "IP4";
    public static final String POLICY_STR_NETWORK_TYPE_IP6 = "IP6";

    public static final String ADDRESS_SEPERATOR = ",";

    public static final String VALUE_RANGE_SEPERATOR = "-";

    //Permission
    public static final String POLICY_STR_PERMISSION_PERMIT = "PERMIT";

    public static final String POLICY_STR_PERMISSION_DENY = "DENY";

    //Capacity
    public static final String POLICY_STR_CAPACITY_ENABLE = "ENABLE";

    public static final String POLICY_STR_CAPACITY_DISABLE = "DISABLE";

    //task status
    public static final String POLICY_STR_STATUS_FINISHED = "FINISHED";

    //policy
    public static final String POLICY_STR_CLAUSE_MATCHCLAUSE = "matchClause";

    public static final String POLICY_STR_CLAUSE_NATCLAUSE = "natClause";

    public static final String POLICY_STR_CLAUSE_ROUTINGDETAIL = "routingDetail";

    //value
    public static final String POLICY_STR_VALUE_ANY = "any";

    public static final String POLICY_STR_VALUE_TCP = "TCP";

    public static final String POLICY_STR_VALUE_UDP = "UDP";

    public static final String POLICY_STR_VALUE_ICMP = "ICMP";

    public static final String POLICY_STR_VALUE_ICMPV6 = "ICMPv6";

    //protocol number string
    public static final String POLICY_NUM_VALUE_ANY = "0";

    public static final String POLICY_NUM_VALUE_ICMP = "1";

    public static final String POLICY_NUM_VALUE_TCP = "6";

    public static final String POLICY_NUM_VALUE_UDP = "17";

    /**
     * ipv4为全部时的值
     */
    public static final String IPV4_ANY = "0.0.0.0/0";

    /**
     * ipv6为全部的值
     */
    public static final String IPV6_ANY = "::/0";

    /**
     * 协议为全部时的值
     */
    public static final String PROTOCOL_ANY = "0-255";

    /**
     * 端口为全部的值
     */
    public static final String PORT_ANY = "0-65535";

    /*******************路径分析状态**************************************/
    //未开始
    public static final int POLICY_INT_RECOMMEND_TASK_NO_STARTED = 0;

    //已完成
    public static final int POLICY_INT_RECOMMEND_ANALYZE_FINISHED = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_ANALYZE_ERROR = 2;

    //路径不存在
    public static final int POLICY_INT_RECOMMEND_ANALYZE_NO_ACCESS = 3;

    //路径已存在
    public static final int POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS = 4;

    //存在多路径
    public static final int POLICY_INT_RECOMMEND_ANALYZE_MULTIPATH = 5;

    //策略具有时间对象
    public static final int POLICY_INT_RECOMMEND_PATH_HAS_TIME_OBJECT = 6;

    //子网不存在
    public static final int POLICY_INT_RECOMMEND_ANALYZE_SRC_ADDRESS_HAS_NO_SUBNET = 7;

    public static final int POLICY_INT_RECOMMEND_ANALYZE_SUBNET_CANNOT_BE_THE_SAME = 8;

    public static final int POLICY_INT_RECOMMEND_ANALYZE_DST_ADDRESS_HAS_NO_SUBNET = 9;

    public static final int POLICY_INT_RECOMMEND_ANALYZE_LONG_LINK_DENY = 10;

    public static final int POLICY_INT_RECOMMEND_ANALYZE_NONE = 11;

    // 路径分析源目的相同子网
    public static final int POLICY_INT_RECOMMEND_ANALYZE_SRC_DST_HAS_SAME_SUBNET = 12;

    /*************************策略生成状态*******************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_ADVICE_FINISHED = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_ADVICE_FAILED = 2;

    //域不存在
    public static final int POLICY_INT_RECOMMEND_ADVICE_NO_ZONE = 3;

    /************************策略检查状态********************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_CHECK_NO_RULE = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMENT_CHECK_FAILED = 2;

    //有相关策略
    public static final int POLICY_INT_RECOMMEND_CHECK_HAS_RULE = 3;

    //策略获取检查结果失败
//    public static final int POLICY_INT_RECOMMEND_CHECH_GET_NO_RESULT =4;

    public static final int POLICY_INT_RECOMMEND_CHECK_SKIPEED = 4;

    /**************************风险分析状态******************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_RISK_NO_RISK = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_RISK_FAILED = 2;

    //存在风险
    public static final int POLICY_INT_RECOMMEND_RISK_HAS_RISK = 3;

    //逻辑安全域不存在
    public static final int POLICY_INT_RECOMMEND_RISK_NO_ZONE_UUID = 4;

    //跳过风险分析
    public static final int POLICY_INT_RECOMMEND_RISK_SKIPPED = 5;

    /***********************命令行生成************************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_CMD_SUCCESS = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_CMD_FAILED = 2;

    //部分设备没有生成命令行
    public static final int POLICY_INT_RECOMMEND_CMD_SOME_DEVICE_HAS_NO_CMD = 3;

    //未生成命令行
    public static final int POLICY_INT_RECOMMEND_CMD_NO_DEVICE_HAS_CMD =4;

    /***********************加入下发队列***********************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_ADD_PUSH_TASK_FINISHED =1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_ADD_PUSH_TASK_FAILED = 2;

    /***********************命令行下发***********************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_PUSH_SUCCESS = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_PUSH_FAILED = 2;

    //下发失败，设备被删除
    public static final int POLICY_INT_RECOMMEND_DEIVCE_DELETED = 3;

    //下发失败，凭证不存在
    public static final int POLICY_INT_RECOMMEND_NO_CREDENTIAL = 4;

    /**********************设备采集****************************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_GATHER_SUCCESS = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_GATHER_FAILED = 2;

    /*************************拓扑分析***********************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_ACCESS_ANALYZE_SUCCESS = 1;

    //执行出错，异常
    public static final int POLICY_INT_RECOMMEND_ACCESS_ANALYZE_FAILED = 2;

    /******************************路径验证******************************/
    //已完成
    public static final int POLICY_INT_RECOMMEND_VERIFY_SUCESS = 1;

    public static final int POLICY_INT_RECOMMEND_VERIFY_ERROR = 2;



    public static final int POLICY_INT_VERIFY_STATUS_PATH_FULLY_OPEN = 1;

    public static final int POLICY_INT_VERIFY_STATUS_PATH_NOT_OPEN = 2;




    //任务状态：仿真任务，下发任务和验证任务
    public static final int POLICY_INT_STATUS_SIMULATION_NOT_STARTED = 0;



    //任务常量
    public static final int POLICY_INT_TASK_NO_TASK = 0;

    public static final int POLICY_INT_TASK_ANALYZE = 1;

    public static final int POLICY_INT_TASK_RECOMMEND = 2;

    public static final int POLICY_INT_TASK_CHECK = 3;

    public static final int POLICY_INT_TASK_RISK = 4;

    public static final int POLICY_INT_TASK_CMD = 5;

    public static final int POLICY_INT_TASK_ADD_PUSH_LIST = 6;

    public static final int POLICY_INT_TASK_PUSH = 7;

    public static final int POLICY_INT_TASK_GATHER = 8;

    public static final int POLICY_INT_TASK_ACCESS_ANALYZE = 9;

    public static final int POLICY_INT_TASK_VERIFY = 10;

    //任务类型常量 new 投入使用
    public static final int POLICY_INT_TASK_TYPE_ALL = 0;

    public static final int POLICY_INT_TASK_TYPE_FRESH = 1;

    public static final int POLICY_INT_TASK_TYPE_ANALYZED = 2;

    public static final int POLICY_INT_TASK_TYPE_PUSHED = 3;

    public static final int POLICY_INT_TASK_TYPE_VERIFIED = 4;

    public static final int POLICY_INT_TASK_TYPE_QUEUED = 7;

    public static final int POLICY_INT_TASK_TYPE_RUNNING = 8;

    public static final int POLICY_INT_TASK_TYPE_ERROR = 9;


    //策略检查规则
    public static final String POLICY_STRING_HIDDEN_POLICY = "RuleCheck_1";
    /**
     * RC_HIDDEN_CONFLICT	冲突策略（隐藏策略的一种，策略动作不一样的屏蔽策略）
     */
    public static final String POLICY_STRING_HIDDEN_POLICY_RC_HIDDEN_CONFLICT = "RC_HIDDEN_CONFLICT";

    /**
     * RC_HIDDEN_SAME	冗余策略1（隐藏策略的一种，策略动作一样，也可理解为冗余的一种
     */
    public static final String POLICY_STRING_HIDDEN_POLICY_RC_HIDDEN_SAME = "RC_HIDDEN_SAME";

    public static final String POLICY_STRING_MERGE_POLICY = "RC_MERGE_RULE";



    public static final String POLICY_STRING_REDUNDANCY_POLICY = "RuleCheck_3";

    public static final int POLICY_INT_RAMDOM_ID_LENGTH = 6;

    /**************************任务状态*********************************/
    //未开始
    public static final int POLICY_INT_STATUS_INITIAL = 0;

    //等待中
    public static final int POLICY_INT_STATUS_SIMULATION_QUEUE = 1;

    //仿真执行中
    public static final int POLICY_INT_STATUS_SIMULATING = 2;
//    public static final int POLICY_INT_STATUS_ANALYZING = 2;

//    public static final int POLICY_INT_STATUS_POLICY_RECOMMEND = 3;
//
//    public static final int POLICY_INT_STATUS_POLICY_CHECK = 4;
//
//    public static final int POLICY_INT_STATUS_RISK_CHECK = 5;
//
//    public static final int POLICY_INT_STATUS_CMD_GENERATING = 6;
    /***任务停止**/
    public static final int POLICY_INT_STATUS_STOPPED = 7;

    //仿真失败
    public static final int POLICY_INT_STATUS_SIMULATION_ERROR = 9;

    //仿真完成
    public static final int POLICY_INT_STATUS_SIMULATION_DONE = 10;

    //加入下发队列
    public static final int POLICY_INT_STATUS_ADDED_PUSH_LIST = 11;

//    public static final int POLICY_INIT_STATUS_REVOCATION = 12;
//
//    public static final int POLICY_INT_STATUS_PUSH_STOPPED = 18;
    //下发部分成功
    public static final int POLICY_INT_STATUS_PUSH_PART_FINISHED = 13;

    //下发失败
    public static final int POLICY_INT_STATUS_PUSH_ERROR = 19;

    //下发完成
    public static final int POLICY_INT_STATUS_PUSH_FINISHED = 20;

    //验证中
    public static final int POLICY_INT_STATUS_VERIFYING = 21;

    public static final int POLICY_INT_VERIFY_QUEUED = 22;
//
//    public static final int POLICY_INT_STATUS_VERIFYING = 23;
//
//    public static final int POLICY_INT_STATUS_STOP_VERIFYING = 24;

    //验证失败
    public static final int POLICY_INT_STATUS_VERIFY_ERROR = 29;

    //验证完成
    public static final int POLICY_INT_STATUS_VERIFY_DONE = 30;

    //仿真部分完成（云、物理其中之一未完成）
    public static final int POLICY_INT_STATUS_SIMULATION_PART_DONE = 8;

    //策略下发任务类型
    //策略仿真任务明细开通
    public static final int POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND = 1;

    //策略检查处理任务
    public static final int POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK = 2;

    //新建安全策略
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED = 3;

    //路径生成命令行任务
    public static final int POLICY_INT_PUSH_TASK_TYPE_ROUTE_RECOMMEND = 4;

    //新建静态NAT命令行
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT = 5;

    //新建源NAT命令行
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT = 6;

    //新建目的NAT命令行
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT = 7;

    //互联网开通任务 已经拆分成8 和14
//    public static final int POLICY_INT_PUSH_TASK_TYPE_INTERNET_RECOMMEND = 8;


    //新建双向NAT命令行
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT = 9;

    /**
     * 应急处置--封堵
     */
    public static final int POLICY_INT_PUSH_TASK_TYPE_DENY = 10;

    /**
     * 应急处置--解封
     */
    public static final int POLICY_INT_PUSH_TASK_TYPE_PERMIT = 11;

    /**
     * 应急处置--回滚
     */
    public static final int POLICY_INT_PUSH_TASK_TYPE_DELETE = 12;
    /**Acl命令行**/
    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_ACL = 13;
    /**互联网内访问外**/
    public static final int IN2OUT_INTERNET_RECOMMEND  = 8;
    /**互联网外访问内**/
    public static final int OUT2IN_INTERNET_RECOMMEND = 14;

    /**大网段开通**/
    public static final int BIG_INTERNET_RECOMMEND = 15;

    /**云内互访，不走物理**/
    public static final int IN2IN_INTERNET_RECOMMEND = 16;

    /**自定义命令行下发**/
    public static final int CUSTOMIZE_CMD_PUSH = 16;

    /**策略优化*/
    public static final int  POLICY_OPTIMIZE= 17;

    /**地址管理**/
    public static final int ADDRESS_MANAGE = 22;

    /**域间合规*/
    public static final int  DOMAIN_COMPLIANCE= 23;

    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT = 18;

    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT = 19;

    public static final int POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING = 20;

    public static final int POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT = 21;

    /**命令收敛*/
    public static final int POLICY_INT_PUSH_CONVERGENCE = 24;



    //是否验证数据
    public static final int POLICY_INT_PATH_ANALYZE_DATA = 0;

    public static final int POLICY_INT_PATH_VERIFY_DATA = 1;

    //路径状态数据信息
    public static final String POLICY_STR_PATHINFO_PATH_STATUS = "pathStatus";

    public static final String POLICY_STR_PATHINFO_ANALYZE_STATUS = "analyzeStatus";

    public static final String POLICY_STR_PATHINFO_ADVICE_STATUS = "adviceStatus";

    public static final String POLICY_STR_PATHINFO_CHECK_STATUS = "checkStatus";

    public static final String POLICY_STR_PATHINFO_RISK_STATUS = "riskStatus";

    public static final String POLICY_STR_PATHINFO_CMD_STATUS = "cmdStatus";

    public static final String POLICY_STR_PATHINFO_PUSH_STATUS = "pushStatus";

    public static final String POLICY_STR_PATHINFO_GATHER_STATUS = "gatherStatus";

    public static final String POLICY_STR_PATHINFO_ACCESS_ANALYZE_STATUS = "accessAnalyzeStatus";

    public static final String POLICY_STR_PATHINFO_VERIFY_STATUS = "verifyStatus";

    /**五元组**/
    public static final String SRC = "SRC";
    public static final String DST = "DST";
    public static final String SERVICE = "SERVICE";

    public static final String ACCESS_GROUP = "access-group";

    public static final String OUT_INTERFACE = "out interface";

    public static final String IN_INTERFACE = "in interface";

    public static final Integer PATH_ENABLE_ENABLE = 1;

    public static final Integer PATH_ENABLE_DISABLE = 0;


    /**下发未开始**/
    public static final int PUSH_STATUS_NOT_START = 0;
    /**下发中**/
    public static final int PUSH_STATUS_PUSHING = 1;
    /**下发完成**/
    public static final int PUSH_STATUS_FINISHED = 2;
    /**下发失败**/
    public static final int PUSH_STATUS_FAILED = 3;
    /**下发部分完成**/
    public static final int PUSH_STATUS_PART_FINISHED = 5;
    /**回滚未开始**/
    public static final int REVERT_STATUS_NOT_START = 0;
    /**回滚中**/
    public static final int REVERT_STATUS_REVERTING = 1;
    /**回滚成功**/
    public static final int REVERT_STATUS_FINISHED = 2;
    /**回滚失败**/
    public static final int REVERT_STATUS_FAILED = 3;

    /**两种标签模型 or and，or 是默认**/
    public static final String LABEL_MODEL_OR = "OR";
    public static final String LABEL_MODEL_AND = "AND";

    /**策略目的地址含有域名**/
    public static final String POLICY_DST_IS_DOMAIN = "1";
    /**策略目的地址不含有域名**/
    public static final String POLICY_DST_NOT_DOMAIN = "0";


    /**飞塔场景NAT匹配对应关系**/
    public static final String MATCH_NOTHING = "0";

    public static final String MATCH_EXIST = "1";

    public static final String MATCH_WHATIF = "2";

    public static final String MATCH_EXIST_AND_WHATIF = "3";

    /***用完标签***/
    public static final String USE_UP = "无可用IP";

}
