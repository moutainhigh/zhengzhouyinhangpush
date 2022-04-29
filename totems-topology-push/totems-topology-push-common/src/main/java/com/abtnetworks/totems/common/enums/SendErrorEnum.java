package com.abtnetworks.totems.common.enums;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 请写类注释
 */
public enum SendErrorEnum {
    /**流程异常**/
    UN_FILL_REG(000, "没有找到该型号正确的终止符"),
    CONNECT_INVALID(001, "客户端无效连接会话"),
    TELNET_FALL(002, "telnet连接后命令行异常"),
    MATCH_CMD_ERROR(004, "下发命令时异常，可能没有找到该型号正确的终止符,或者网络io出现异常超时"),
    JSH_TELNET_EXCEPTION(005, "创建连接异常，请检查连接设备相关信息是否正确"),
    SYSTEM_ERROR(006, "未知错误"),
    PUSH_CMD_FAIL(007, "发现有命令行发送失败，请检查原因"),
    DEVICE_NOT_EXIST_FAIL(8, "设备不存在"),



    //执行后错误
    MATCH_CMD_OUT_TIME(-2, "命令回显超时，可能未找到正确的正则导致匹配超时"),
    ROLLBACK_MATCH_NUM(100,"回滚中，查询数量与匹配数量不相等"),
    RET_EOF(-3,"无法下发命令，可能已退出对应交互目录级，请检查命令是否正确"),
    //匹配到错误
    MATCH_ECHO_ERROR_CMD(010, "命令回显匹配到异常命令，停止剩余的命令下发(未填写匹配的反馈信息)"),
    MATCH_ECHO_ERROR_VALUE_CMD(011, "命令回显匹配到异常命令，停止剩余的命令下发"),

    // 以下为管理平台下发异常信息，编码从201开始
    PLATFORM_API_SYSTEM_ERROR(200,"管理平台下发系统异常，停止剩余的命令下发"),
    PLATFORM_API_CAN_NOT_FIND_NODE_ERROR(201,"管理平台下发未找到设备，停止剩余的命令下发"),
    PLATFORM_API_RESPONSE_ERROR(202,"管理平台API请求响应异常，停止剩余的命令下发"),
    PLATFORM_API_UN_SUPPORT_URL_ERROR(203,"管理平台暂不支持下发URL类型的策略，停止剩余的命令下发"),
    PLATFORM_API_DELETE_POLICY_ERROR(204,"管理平台回滚策略失败，停止剩余的命令下发"),
    PLATFORM_API_MOVE_POLICY_ERROR(205,"管理平台移动策略失败，停止剩余的命令下发"),
    PLATFORM_API_INSTALL_ERROR(206,"管理平台install安装策略失败，停止剩余的命令下发"),

    /**业务异常**/
    CHECKPOINT_LOGIN_PARAM(300,"没有获取checkpoint登录参数导致登录失败"),
    RULE_LIST_NOT_EXIST(301, "设备接口上所挂载的acl策略集不存在"),
    NO_USABLE_RULE_ID(301, "There is not enough ruleId available. Edit the command line without ruleId and delete this message to push try again (direct push may cause the policy to fail to take effect!)"),
    ITF_ACL_LIST_NOT_HAVE(302, "接口没有挂载ACL"),
    NOT_FIND_NAT_ADDRESS_MAPPING(303,"没有找到地址映射关系无法自动定位nat,请检查地址映射配置或工单中的地址参数"),
    SCENE_NAME_EXIST(304,"当前场景名称已经存在!"),
    FORTINET_VIRTUALIP_EXIST(305,"当前工单转换前的目的地址已在虚拟ip中存在,且工单中的转换后的目的地址和虚拟ip中的映射地址不一致,请确认!"),
    PYTHON_PUSH_ERROR(306,"调用python下发异常!"),

    ADDRESS_NOT_FULLY_CONTAINED(307,"源IP或目的IP不在NAT规划中,请检查NAT规划配置"),
    NO_NEXT_AVAILABLEIP(308,"可用IP不足，请检查地址池配置")
     ;

    private int code;

    private String message;

    SendErrorEnum(
            int code, String
            message) {

        this.code = code;
        this.message = message;

    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
