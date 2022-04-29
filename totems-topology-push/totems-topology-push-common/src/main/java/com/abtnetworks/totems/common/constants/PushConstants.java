package com.abtnetworks.totems.common.constants;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 17:48
 * @Description: <p> 封堵常量 {@link DisposalConstants }
 *  策略仿真常量 {@link PolicyConstants}
 *  生成常量 {@link GenerateConstants}
 *  高级设置常量 {@link AdvancedSettingsConstants}
 *  公共类常量{@link CommonConstants}
 *  凭据常量{@link CredentialConstants}
 *  下发常量{@link PushConstants}
 *
 * </p>
 */
public class PushConstants {

    public static final String PUSH_STR_TASK_ORDER_NO = "orderNo";

    public static final String PUSH_STR_TASK_ORDER_TYPE = "orderType";

    public static final String PUSH_STR_TASK_POLICY_ID = "policyId";

    public static final String PUSH_STR_TASK_DEVICE_UUID = "deviceUuid";

    public static final String PUSH_STR_TASK_DEVICE_NAME = "deviceName";

    public static final String PUSH_STR_TASK_MANAGE_IP = "manageIp";

    public static final String PUSH_STR_TASK_USER_NAME = "userName";

    public static final String PUSH_STR_TASK_COMMAND = "command";

    //工单号生成时间格式
    public static final String PUSH_ORDER_NO_TIME_FORMAT = "yyyyMMddHHmmss";

    //策略下发任务状态
    public static int PUSH_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND = 1;

    public static int PUSH_INT_PUSH_TASK_TYPE_POLICY_CHECK = 2;

    public static int PUSH_INT_PUSH_TASK_NO_POLICY_ID = 0;

    //下发结果状态
    //未开始
    public static int PUSH_INT_PUSH_RESULT_STATUS_NOT_START = 0;

    //已完成
    public static int PUSH_INT_PUSH_RESULT_STATUS_DONE = 1;

    //下发失败
    public static int PUSH_INT_PUSH_RESULT_STATUS_ERROR = 2;

    //已停止
    public static int PUSH_INT_PUSH_RESULT_STATUS_STOPPED = 3;

    //队列中
    public static int PUSH_INT_PUSH_QUEUED = 4;

    //下发中
    public static int PUSH_INT_PUSH_RUNNING = 5;

    //正在生成命令行
    public static int PUSH_INT_PUSH_GENERATING = 6;

    //生成命令行出错
    public static int PUSH_INT_PUSH_GENERATING_ERROR = 7;

    //生成命令行成功
    public static int PUSH_INT_PUSH_GENERATING_SUCCESS = 8;

    public static String FORMAT_DATE = "yyyy-MM-dd HH:mm:ss";

    /**数字5**/
    public static int FIVE = 5;

    /**系统邮件参数，组名称mailServer**/
    public static final String EMAIL_PARAM_GROUP_NAME = "mailServer";

    public static volatile int COUNT = 0;

}
