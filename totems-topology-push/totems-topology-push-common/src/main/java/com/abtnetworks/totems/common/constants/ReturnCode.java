package com.abtnetworks.totems.common.constants;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 12:45
 */
public class ReturnCode {

    public static final int POLICY_MSG_OK = 0;

    public static final int POLICY_MSG_EMPTY_VALUE = 1;

    public static final int POLICY_MSG_INVALID_LENGTH = 2;

    public static final int POLICY_MSG_INVALID_FORMAT = 3;

    public static final int POLICY_MSG_INVALID_SUBNET_MASK = 4;

    public static final int NO_PATH = 5;

    public static final int FULL_ACCESS = 6;

    public static final int AUTOWIRE_FAILED = 7;

    public static final int CHECK_RISK_FAIL = 8;

    public static final int RECORD_ALREADY_EXIST = 9;

    public static final int NO_DEVICE_DETAIL = 10;

    public static final int NOT_AUTO_GATHER_DEVICE = 11;

    public static final int RECOMMEND_PROCEDURE_FINISHED = 12;

    public static final int COMMANDLINE_GENERATE_FAILED = 13;

    public static final int PUSH_PROCEDURE_NOT_FINISHED = 14;

    public static final int FAILED_TO_GET_RULE_CHECK_RESULT_ENTITY = 15;

    public static final int RULE_CHECK_RESULT_EMPTY = 16;

    public static final int FAILED_TO_PARSE_STRING_TO_JSONOBJECT = 17;

    public static final int FAILED_TO_PARSE_JSONOBJECT_TO_JAVA_OBJECT = 18;

    public static final int FAILED_TO_DELETE_CREDENTIAL = 19;

    public static final int FAILED_TO_MODIFY_CREDENTIAL = 20;

    public static final int FAILED_TO_CREATE_CREDENTIAL = 21;

    public static final int CANNOT_GET_UNIQUE_UUID_FROM_IP = 22;

    public static final int GET_MULTIPLE_UUID_FROM_IP = 23;

    public static final int POLICY_MSG_INVALID_POST_LENGTH = 24;

    public static final int POLICY_MSG_INVALID_POST_VALUE = 25;

    public static final int MULTIPLE_PATH = 26;

    public static final int TASK_ALREADY_EXIST = 27;

    public static final int STOP_TASK_FAILED = 28;

    public static final int NO_POLICY_TO_PUSH = 29;

    public static final int PUSH_TASK_FINISHED = 30;

    public static final int VERIFY_TASK_FINISHED = 31;

    public static final int PATH_NO_ACCESS = 32;

    public static final int PATH_FULL_ACCESS = 33;

    public static final int PATH_NO_DATA = 34;

    public static final int PATH_MULTI_PATH = 35;

    public static final int RECOMMEND_TASK_LIST_EMPTY = 36;

    public static final int NO_RECOMMEND_TASK_ID_SELECTED = 37;

    public static final int NO_RECOMMEND_TASK_ENTITY_GET = 38;

    public static final int TASK_LIST_IS_EMPTY = 39;

    public static final int TASK_LIST_SIZE_IS_NOT_ENOUGH = 40;

    public static final int IMPORT_FAILED_INVALID_IMPORT_FILE = 41;

    public static final int CAN_NOT_DELETE_RUNNING_TASK = 42;

    public static final int NO_TASK_SELECTED_TO_VERIFY = 43;

    public static final int NO_SEARCH_PARAMETERS = 44;

    public static final int SRC_IP_FORMAT_ERROR = 45;

    public static final int DST_IP_FORMAT_ERROR = 46;

    public static final int SRC_PORT_FORMAT_ERROR = 47;

    public static final int DST_PORT_FORMAT_ERROR =48;

    public static final int PROTOCOL_FORMAT_ERROR = 49;

    public static final int ICMP_CODE_FORMAT_ERROR = 50;

    public static final int ICMP_TYPE_FORMAT_ERROR = 51;

    public static final int TIME_FORMAT_ERROR = 52;

    public static final int VERIFICATION_IS_RUNNING = 53;

    public static final int FAILED_TO_DELETE_CREDENTIAL_NODE_EXIST = 54;

    public static final int VERIFY_TASK_IS_RUNNING = 55;

    public static final int IP_ADDRESS_HAS_NO_SUBNET = 56;

    public static final int IP_ADDRESS_HAS_MULTIPLE_SUBNET = 57;

    public static final int POLICY_CHECK_ERROR = 58;

    public static final int POLICY_RISK_ERROR = 59;

    public static final int POLICY_VERIFY_ERROR = 60;

    public static final int POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND = 61;

    public static final int PATH_FULL_ACCESS_WITH_TIME_OBJECT = 62;

    public static final int OPEN_DEVICE_WITH_TIME_OBJECT = 63;

    public static final int INVALID_IP_COUNT = 64;

    public static final int REMOVE_UUID_FROM_LIST_FAILED = 65;

    public static final int MODIFY_PARAM_VALUE_FAILED = 66;

    public static final int ADD_DEVICE_TO_LIST_FAILED = 67;

    public static final int PLEASE_SELECT_A_DEVICE = 68;

    public static final int SUBNET_UUID_IS_EMPTY = 69;

    public static final int SUBNET_CANNOT_BE_THE_SAME = 70;

    public static final int INVALID_ORDER_NAME = 71;

    public static final int INVALID_TASK_ID = 72;

    public static final int INVALID_IP_RANGE = 73;

    public static final int INVALID_SRC_IP_RANGE = 74;

    public static final int INVALID_DST_IP_RANGE = 75;

    public static final int INVALID_ENTITY_ID = 76;

    public static final int TASK_IS_DELETED = 77;

    public static final int INVALID_USER = 78;

    public static final int TASK_STATUS_ERROR = 79;

    public static final int INVALID_COMMANDLINE_STATUS = 80;

    public static final int INVALID_TASK_STATUS = 81;

    public static final int EMPTY_PARAMETERS = 82;

    public static final int DEVICE_NOT_EXIST = 83;

    public static final int EMPTY_DEVICE_INFO = 84;

    public static final int NO_RECOMMEND_POLICY  = 85;

    public static final int PRE_SRC_ADDRESS_FORMAT_ERROR = 86;

    public static final int POST_SRC_ADDRESS_FORMAT_ERROR = 87;

    public static final int PRE_DST_ADDRESS_FORMAT_ERROR = 88;

    public static final int POST_DST_ADDRESS_FORMAT_ERROR = 89;

    public static final int PRE_SERVICE_FORMAT_ERROR = 90;

    public static final int POST_SERVICE_FORMAT_ERROR = 91;

    public static final int POST_SRC_ADDRESS_IS_EMPTY = 92;

    public static final int POST_DST_ADDRESS_IS_EMPTY = 93;

    public static final int INVALID_NAT_TYPE = 94;

    public static final int SAVE_EMPTY_LIST = 95;

    public static final int LONG_LINK_DENY = 96;

    //策略下发任务返回值，从101开始
    public static final int NO_TASK_ID_SELECTED_TO_DELETE = 101;

    public static final int NO_TASK_ID_SELECTED_TO_START = 102;

    public static final int PARSE_ID_LIST_FAIL = 103;

    public static final int PUSH_TASK_ERROR = 104;

    public static final int CONNECT_FAILED = 105;

    public static final int NO_CREDENTIAL = 106;

    public static final int EFFECTIVE_TIME_ERROR = 107;

    public static final int PROTOCOL_REPEAT_ERROR = 108;

    public static final int SERVICE_FORMAT_ERROR = 109;

    public static final int SERVICE_CANNOT_BE_ANY = 110;

    public static final int NO_COMMAND_TO_DOWNLOAD = 111;

    public static final int NO_RECORD = 112;

    public static final int EMPTY_ID = 113;

    public static final int EMPTY_SERVICE = 114;

    public static final int INVALID_NUMBER = 115;

    public static final int INVALID_PORT_FORMAT = 116;

    public static final int INVALID_TIME_VALUE = 117;

    public static final int IDLE_TIMEOUT_FORMAT_ERROR = 118;

    public static final int SAME_VENDOR_NAME_ERROR = 119;

    public static final int DEVICE_NUM_ERROR = 120;
    /**大网段经过nat错误***/
    public static final int BIG_INTERNET_NAT_ERROR = 121;
    //任务线程返回值，从151开始

    public static final int CREDENTIAL_VALIDATION_DUPLICATE_NAME = 122;

    public static final int RESTART_PUSH_OR_REVERT_RUNNING = 123;

    public static final int NO_NAT_MAPPING_ADDRESS = 124;

    public static final int DST_IP_FORMAT_ERROR_HAS_SPACE = 125;

    public static final int SCENE_NOT_EXIST = 126;

    public static final int ADDRESS_NOT_IP_HOST = 127;

    public static final int SRC_DST_FROM_SAME_SUBNET = 128;

    public static final int SUBNET_MASK_ERROR = 129;

    public static final int PRIORITY_OR_MANAGERDISTANCE_ERROR = 130;

    public static final int NEXTHOP_FORMAT_ERROR = 131;

    public static final int OUTINTERFACE_AND_NEXTHOP_ALL_EMPTY = 132;

    //下发时间锁时间内
    public static final int PUSH_TIME_LOCKED = 133;

    public static final int PROTECT_NEWWORK_ERROR = 134;

    public static final int OUTSIDEIP_ERROR = 135;

    public static final int INSIDEIP_ERROR = 136;

    public static final int NATTYPE_ERROR = 137;

    public static final int PROTECT_ICMP_TYPE_ERROR = 138;

    public static final int PROTECT_EMPTY_TYPE_ERROR = 139;

    public static final int PROTECT_TCP_UDP_PORT_ERROR = 140;

    public static final int SRC_NATTYPE_IPEMPTY_ERROR = 141;

    public static final int DST_NATTYPE_IPEMPTY_ERROR = 142;

    public static final int ADDRESS_NOT_EXIST = 143;

    public static final int ADDRESS_LIST_EMPTY = 144;

    public static final int OBJECT_PUSH_ERROR = 145;

    public static final int FILE_IS_EXIST_ERROR = 146;

    public static final int FILE_FORMAT_ERROR = 147;

    public static final int NO_NEXT_AVAILABLEIP = 148;

    public static final int ADDRESS_NOT_FULLY_CONTAINED = 149;

    public static final int FAILED = 255;


    public static String getMsg(int rc) {
        switch(rc) {
            case POLICY_MSG_OK:
                return "OK";
            case POLICY_MSG_EMPTY_VALUE:
                return "IP地址不能为空！";
            case POLICY_MSG_INVALID_LENGTH:
                return "只能输入一个主机IP、IP范围或子网";
            case POLICY_MSG_INVALID_FORMAT:
                return "IP地址格式不正确，只能IP、IP范围或子网";
            case POLICY_MSG_INVALID_SUBNET_MASK:
                return "子网掩码长度不能小于24！";
            case NO_PATH:
                return "路径不存在！";
            case FULL_ACCESS:
                return "路径不存在！";
            case AUTOWIRE_FAILED:
                return "路径已开通！";
            case CHECK_RISK_FAIL:
                return "自动注入失败！";
            case RECORD_ALREADY_EXIST:
                return "主题（工单号）已存在！";
            case NO_DEVICE_DETAIL:
                return "没有设备数据！";
            case NOT_AUTO_GATHER_DEVICE:
                return "非自动采集设备！";
            case RECOMMEND_PROCEDURE_FINISHED:
                return "策略建议已完成！";
            case COMMANDLINE_GENERATE_FAILED:
                return "命令行未生成，不能进行下发！";
            case PUSH_PROCEDURE_NOT_FINISHED:
                return "策略下发未完成！";
            case FAILED_TO_GET_RULE_CHECK_RESULT_ENTITY:
                return "获取策略检查数据失败";
            case RULE_CHECK_RESULT_EMPTY:
                return "策略检查数据为空";
            case FAILED_TO_PARSE_STRING_TO_JSONOBJECT:
                return "字符串转换JSON对象失败";
            case FAILED_TO_PARSE_JSONOBJECT_TO_JAVA_OBJECT:
                return "JSON对象转换成JAVA对象失败";
            case FAILED_TO_DELETE_CREDENTIAL:
                return "删除采集凭据失败";
            case FAILED_TO_MODIFY_CREDENTIAL:
                return "修改采集凭据失败";
            case FAILED_TO_CREATE_CREDENTIAL:
                return "创建采集凭据失败";
            case FAILED_TO_DELETE_CREDENTIAL_NODE_EXIST:
                return "凭据已被使用，不可删除";
            case VERIFY_TASK_IS_RUNNING:
                return "策略验证任务正在进行！请稍后再开始验证";
            case IP_ADDRESS_HAS_NO_SUBNET:
                return "地址对应子网不存在！";
            case IP_ADDRESS_HAS_MULTIPLE_SUBNET:
                return "地址对应多个子网！";
            case POLICY_CHECK_ERROR:
                return "策略检查异常，错误！";
            case POLICY_RISK_ERROR:
                return "风险分析异常，错误！";
            case POLICY_VERIFY_ERROR:
                return "策略验证异常，错误！";
            case POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND:
                return "可下发设备数量为0！";
            case PATH_FULL_ACCESS_WITH_TIME_OBJECT:
                return "路径已存在，设备策略含有时间对象！";
            case OPEN_DEVICE_WITH_TIME_OBJECT:
                return "放通设备策略含有时间对象！";
            case INVALID_IP_COUNT:
                return "只能为1～254个IP、IP范围或者IP网段";
            case REMOVE_UUID_FROM_LIST_FAILED:
                return "从里外列表中删除设备失败, 数据库中数据不存在";
            case MODIFY_PARAM_VALUE_FAILED:
                return "修改参数失败，参数不存在";
            case ADD_DEVICE_TO_LIST_FAILED:
                return "添加设备到例外列表失败，数据库中数据不存在";
            case PLEASE_SELECT_A_DEVICE:
                return "请至少选一个设备添加到例外列表";
            case SUBNET_UUID_IS_EMPTY:
                return "子网不存在";
            case SUBNET_CANNOT_BE_THE_SAME:
                return "源地址与目的地址子网不能相同";
            case INVALID_ORDER_NAME:
                return "主题（工单号）长度不超过16个字符，只能包括数字和字母，不能以数字开头！";
            case INVALID_TASK_ID:
                return "任务ID不正确!";
            case INVALID_IP_RANGE:
                return "IP范围格式错误！";
            case INVALID_SRC_IP_RANGE:
                return "源地址IP范围格式错误！";
            case INVALID_DST_IP_RANGE:
                return "目的地址IP范围格式错误！";
            case INVALID_ENTITY_ID:
                return "工单ID非法！";
            case TASK_IS_DELETED:
                return "工单已被删除！";
            case INVALID_USER:
                return "不能编辑他人的工单！";
            case TASK_STATUS_ERROR:
                return "只能编辑未开始仿真/仿真失败/仿真完成的工单！";
            case INVALID_COMMANDLINE_STATUS:
                return "只能编辑未下发的命令行！";
            case INVALID_TASK_STATUS:
                return "只能对未下发的任务设置有效/无效路径！";
            case EMPTY_PARAMETERS:
                return "参数为空！";
            case DEVICE_NOT_EXIST:
                return "设备不存在！";
            case EMPTY_DEVICE_INFO:
                return "设备信息为空！";
            case NO_RECOMMEND_POLICY:
                return "没有策略建议生成！";
            case PRE_SRC_ADDRESS_FORMAT_ERROR:
                return "转换前源地址格式错误！";
            case POST_SRC_ADDRESS_FORMAT_ERROR:
                return "转换后源地址格式错误！";
            case PRE_DST_ADDRESS_FORMAT_ERROR:
                return "转换前目的地地址格式错误！";
            case POST_DST_ADDRESS_FORMAT_ERROR:
                return "转换后目的地址格式错误";
            case PRE_SERVICE_FORMAT_ERROR:
                return "转换前服务格式错误！";
            case POST_SERVICE_FORMAT_ERROR:
                return "转换后服务格式错误！";
            case POST_SRC_ADDRESS_IS_EMPTY:
                return "转换后源地址不能为空！";
            case POST_DST_ADDRESS_IS_EMPTY:
                return "转换后目的地址不能为空！";
            case INVALID_NAT_TYPE:
                return "NAT协议类型错误！";
            case SAVE_EMPTY_LIST:
                return "存储数据列表为空！";
            case LONG_LINK_DENY:
                return "长链接未放通！";
            case CANNOT_GET_UNIQUE_UUID_FROM_IP:
                return "不能确定地址所在子网信息，地址不在子网中或对应多个子网";
            case GET_MULTIPLE_UUID_FROM_IP:
                return "地址对应多个子网";
            case POLICY_MSG_INVALID_POST_LENGTH:
                return "端口数量不能大于5！";
            case POLICY_MSG_INVALID_POST_VALUE:
                return "端口格式不正确！";
            case MULTIPLE_PATH:
                return "路径多于一条！";
            case TASK_ALREADY_EXIST:
                return "任务已在队列中，请稍后！";
            case STOP_TASK_FAILED:
                return "当前没有可以停止的工单任务！";
            case NO_POLICY_TO_PUSH:
                return "没有策略下发！";
            case PUSH_TASK_FINISHED:
                return "下发任务已结束，不能重复下发！";
            case VERIFY_TASK_FINISHED:
                return "验证任务已结束，不能重复验证！";
            case PATH_NO_ACCESS:
                return "没有通路";
            case PATH_FULL_ACCESS:
                return "通路已存在";
            case PATH_NO_DATA:
                return "路径数据不存在";
            case PATH_MULTI_PATH:
                return "路径多于一条！";
            case RECOMMEND_TASK_LIST_EMPTY:
                return "策略仿真任务列表为空！";
            case NO_RECOMMEND_TASK_ID_SELECTED:
                return "没有选择策略仿真任务！";
            case NO_RECOMMEND_TASK_ENTITY_GET:
                return "没有获取到策略仿真任务！";
            case TASK_LIST_IS_EMPTY:
                return "任务列表为空！";
            case TASK_LIST_SIZE_IS_NOT_ENOUGH:
                return "任务列表不足！";
            case IMPORT_FAILED_INVALID_IMPORT_FILE:
                return "导入失败，请确保文件格式和内容正确！";
            case CAN_NOT_DELETE_RUNNING_TASK:
                return "不能删除执行中的任务";
            case NO_TASK_SELECTED_TO_VERIFY:
                return "没有选择验证任务！";
            case NO_SEARCH_PARAMETERS:
                return "搜索参数全部为空！";
            case SRC_IP_FORMAT_ERROR:
                return "源地址格式错误！只能为IP，IP范围或者IP网段。";
            case DST_IP_FORMAT_ERROR:
                return "目的地址格式错误！只能IP，IP范围或者IP网段。";
            case SRC_PORT_FORMAT_ERROR:
                return "源端口格式错误！只能为5个以下的端口号或者端口号范围。";
            case DST_PORT_FORMAT_ERROR:
                return "目的端口格式错误！只能为5个以下的端口号或者端口范围。";
            case PROTOCOL_FORMAT_ERROR:
                return "服务格式错误！只能为\"TCP\"，\"UDP\"，或者\"ICMP\"！";
            case ICMP_CODE_FORMAT_ERROR:
                return "ICMP Code错误！只能为0~255之间的数值。";
            case ICMP_TYPE_FORMAT_ERROR:
                return "ICMP Type错误！只能为0~255之间的数值。";
            case TIME_FORMAT_ERROR:
                return "时间格式错误！开始时间和结束时间格式为\"yyyy/MM/dd HH:mm\"，并且开始时间要早于结束时间。";
            case VERIFICATION_IS_RUNNING:
                return "批量验证已经开始！验证完成前不能再次开始验证！";
            case NO_TASK_ID_SELECTED_TO_DELETE:
                return "请至少选择一个任务删除！";
            case NO_TASK_ID_SELECTED_TO_START:
                return "请至少选择一个任务开始！";
            case PARSE_ID_LIST_FAIL:
                return "解析任务ID列表出错！";
            case PUSH_TASK_ERROR:
                return "下发异常！";
            case CONNECT_FAILED:
                return "连接设备失败！";
            case NO_CREDENTIAL:
                return "获取凭证失败！";
            case EFFECTIVE_TIME_ERROR:
                return "生效时间格式错误，正确格式示例: 2019/6/21 00:00:00 - 2019/6/22 00:00:00";
            case PROTOCOL_REPEAT_ERROR:
                return "协议类型不能重复！";
            case SERVICE_FORMAT_ERROR:
                return "服务格式错误！";
            case SERVICE_CANNOT_BE_ANY:
                return "服务不能为空，不能为ANY或包含ANY！";
            case NO_COMMAND_TO_DOWNLOAD:
                return "未生成命令行！";
            case NO_RECORD:
                return "命令行不存在！";
            case EMPTY_ID:
                return "序号不能为空！";
            case EMPTY_SERVICE:
                return "服务不能为空！";
            case INVALID_NUMBER:
                return "序号不合法！序号只能为纯数字";
            case INVALID_PORT_FORMAT:
                return "端口不合法，只能为0-65535的纯数字，或者数字范围！";
            case INVALID_TIME_VALUE:
                return "解析时间出错！";
            case IDLE_TIMEOUT_FORMAT_ERROR:
                return "长连接数据不合法，长连接数据应当为(1-24000)的数字！";
            case SAME_VENDOR_NAME_ERROR:
                return "请选择相同厂商下的2个设备！";
            case DEVICE_NUM_ERROR:
                return "请同时选择2个或2个以上设备！";
            case CREDENTIAL_VALIDATION_DUPLICATE_NAME:
                return "凭据名重复";
            case RESTART_PUSH_OR_REVERT_RUNNING:
                return "不能重复仿真正在下发或回滚中的任务!";
            case NO_NAT_MAPPING_ADDRESS:
                return "没有找到地址映射关系无法自动定位nat,请检查地址映射配置或工单中的地址参数";
            case DST_IP_FORMAT_ERROR_HAS_SPACE:
                return "目的地址格式错误！地址中间不允许有空格!";
            case SCENE_NOT_EXIST:
                return "应用发布场景不存在!";
            case ADDRESS_NOT_IP_HOST:
                return "IP地址只能为单ip!";
            case SRC_DST_FROM_SAME_SUBNET:
                return "源目的同网段";
            case SUBNET_MASK_ERROR:
                return "子网掩码格式错误";
            case PRIORITY_OR_MANAGERDISTANCE_ERROR:
                return "优先级/管理距离 格式错误";
            case NEXTHOP_FORMAT_ERROR:
                return "下一跳格式错误!";
            case OUTINTERFACE_AND_NEXTHOP_ALL_EMPTY:
                return "下一跳和出接口不能同时为空!";
            case PROTECT_NEWWORK_ERROR:
                return "防护网段格式错误！只能为IP，IP范围或者IP网段。";
            case OUTSIDEIP_ERROR:
                return "转换前IP格式错误！只能为IP，IP范围或者IP网段。";
            case INSIDEIP_ERROR:
                return "转换后IP格式错误！只能为IP，IP范围或者IP网段。";
            case NATTYPE_ERROR:
                return "NAT类型错误！只能为源Nat或者目的Nat。";
            case PROTECT_ICMP_TYPE_ERROR:
                return "协议为ICMP时，不能填写端口";
            case PROTECT_EMPTY_TYPE_ERROR:
                return "协议为空时，不能填写端口";
            case PROTECT_TCP_UDP_PORT_ERROR:
                return "协议为TCP或UDP时，端口不能为空";
            case SRC_NATTYPE_IPEMPTY_ERROR:
                return "类型为源nat时，转换前IP不能为空";
            case DST_NATTYPE_IPEMPTY_ERROR:
                return "类型为目的nat时，转换前IP及转换后IP不能为空";
            case ADDRESS_NOT_EXIST:
                return "地址对象名称中有不存在于地址对象列表的数据";
            case ADDRESS_LIST_EMPTY:
                return "地址对象列表为空";
            case OBJECT_PUSH_ERROR:
                return "对象下发失败! 对象所关联场景中的设备有存在采集中的设备,请稍后再试";
            case FILE_IS_EXIST_ERROR:
                return "文件已经存在,上传文件失败";
            case FILE_FORMAT_ERROR:
                return "当前上传文件格式不正确，不是python文件";
            case NO_NEXT_AVAILABLEIP:
                return "可用IP不足，请检查地址池配置";
            case ADDRESS_NOT_FULLY_CONTAINED:
                return "源IP或目的IP不在NAT规划中,请检查NAT规划配置";
            case FAILED:
                return "失败！";
            default:
                return "错误码不存在("+ String.valueOf(rc) + ")!";
        }
    }
}
