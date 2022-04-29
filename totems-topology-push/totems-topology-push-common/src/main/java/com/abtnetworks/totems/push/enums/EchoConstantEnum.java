package com.abtnetworks.totems.push.enums;

/**
 * @author lifei
 * @desc 各个厂商查询主备命令回显常量枚举(定制化枚举,每个厂商匹配不一致)
 * @date 2021/5/13 11:26
 */
public enum EchoConstantEnum {

    /**
     * 回显格式
     * HA Group id=0
     *   state  Master
     *   priority  150
     *   preempt  N/A
     *   monitor  ha-track
     *   HA total peer number  1
     *   HA peer information:
     *      device id 2809227162007450
     *      ip  1.1.1.2
     *      state  Backup
     *      priority  100
     *  // 取 HA Group id=0 下的state的状态
     */
    HILLSTONE("backup","state","preempt","ha group id"),
    /**
     * 回显格式
     * <GD000DPFW10-WLAW121-M>show hotbackup state
     * hotbackup                : enable
     * hotbackup mode           : silence
     * state                    : master
     * local    priority        : 255
     * // 取 state后面value
     */
    DPTECHR("backup","mode","local","hotbackup mode"),
    /**
     * 回显格式
     * display hrp state
     * HRP_M<GD000HWFW66-WYAW131>display hrp state
     * 2021-04-25 12:00:14.619 +08:00
     *  Role: active, peer: standby (should be "standby-active")
     *  Running priority: 45000, peer: 45000
     *  // 取Role 后面的值
     */
    HUAWEI("standby","role","peer","running priority"),
    /**
     * 回显格式
     * fengguoyao@GD000JF36-YW02> show chassis cluster status
     * Cluster ID: 9
     * Node                  Priority          Status    Preempt  Manual failover
     *
     * Redundancy group: 0 , Failover count: 0
     *     node0                   200         primary        no       no
     *     node1                   50          secondary      no       no
     *
     * Redundancy group: 1 , Failover count: 4
     *     node0                   200         primary        no       no
     *     node1                   50          secondary      no       no
     *
     * {secondary:node1}
     *  // 判断输出最后一行是否提示primary
     */
    JUNIPER_SRX("secondary","{","}","redundancy group"),
    /**
     * 回显格式
     * group priority preempt holddown inelig   master       PB other members  myself uptime
     *  0   100    no     3        no     myself       3475145         143d;17:48:47
     * total number of vsd groups: 1
     * Total iteration=75316952,time=1382130135,max=116322,min=2813,average=18
     * // 判断master这一列第一行的值
     */
    JUNIPER_SSG("myself","group priority","total iteration","group priority"),

    /**
     * 回显格式
     * Heartbeat-Link: bond0,  established
     *
     * Vrid 100
     * Heartbeat-IP    Preempt  Runtime-Priority  Status  Metric  Interface
     * --------------------------------------------------------------------
     * 1.1.1.2         disable  100               MASTER  0       eth20,eth21
     * 1.1.1.1         disable  254               BACKUP  0       eth20,eth21
     *
     * 取 status第一行的状态
     */
    TOPSEC("backup","---","","heartbeat-link"),

    /**
     * NG2回显格式
     * HA-Status: ha enable
     * Heartbeat-Link: feth5,  established
     * Heartbeat-Local IP: 1.1.1.1
     * Heartbeat-Remote IP: 1.1.1.2
     *
     * Group 100
     * State       Preempt    Priority   Interface
     * STANDBY      disable    65000      feth1,feth2,feth3,feth4,vlan.0118,vlan.0120,vlan.0122,vlan.0128,vlan.0203,vlan.0206
     */
    TOPSECNG2("standby","state","","ha-status"),

    /**
     * FORTINET 回显格式
     * HA information
     *
     * Statistics
     *
     *         traffic.local = s:0 p:28251211685 b:3928225391340
     *
     *         traffic.total = s:0 p:28246008944 b:3926646242046
     *
     *         activity.fdb  = c:0 q:0
     *
     *
     *
     * Model=500, Mode=2 Group=0 Debug=0
     *
     * nvcluster=1, ses_pickup=1, delay=0
     *
     *
     *
     * [Debug_Zone HA information]
     *
     * HA group member information: is_manage_master=1.
     *
     * FG5H0E5818902405: Master, serialno_prio=0, usr_priority=200, hostname=WX-3CH10-YZMFW-M
     *
     * FG5H0E5818902165:  Slave, serialno_prio=1, usr_priority=128, hostname=FG5H0E5818902165
     */
    FORTINET("0","is_manage_master","","ha information")
    ;


    /**
     * 主设备标识
     */
    private String backupSign;

    /**
     * 前置字段
     */
    private String preField;

    /**
     * 后置字段
     */
    private String postField;

    /**
     * 正确回显标识字段
     */
    private String echoSign;


    EchoConstantEnum(String backupSign, String preField, String postField,String echoSign) {
        this.backupSign = backupSign;
        this.preField = preField;
        this.postField = postField;
        this.echoSign = echoSign;
    }

    public String getBackupSign() {
        return backupSign;
    }

    public void setBackupSign(String backupSign) {
        this.backupSign = backupSign;
    }

    public String getPreField() {
        return preField;
    }

    public void setPreField(String preField) {
        this.preField = preField;
    }

    public String getPostField() {
        return postField;
    }

    public void setPostField(String postField) {
        this.postField = postField;
    }

    public String getEchoSign() {
        return echoSign;
    }

    public void setEchoSign(String echoSign) {
        this.echoSign = echoSign;
    }
}
