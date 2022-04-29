package com.abtnetworks.totems.common.constants;

/**
 * @Author: zhoumuhua
 * @Date: 2021/5/14
 * @Description: <p>
 *  下发状态常量{@link PushStatusConstans}
 */
public class PushStatusConstans {
    /**下发未开始**/
    public static final int PUSH_STATUS_NOT_START = 0;
    /**下发中**/
    public static final int PUSH_STATUS_PUSHING = 1;
    /**下发完成**/
    public static final int PUSH_STATUS_FINISHED = 2;
    /**下发失败**/
    public static final int PUSH_STATUS_FAILED = 3;
    /**下发等待中/队列中**/
    public static final int PUSH_INT_PUSH_QUEUED = 4;
    /**下发部分完成**/
    public static final int PUSH_STATUS_PART_FINISHED = 5;
}
