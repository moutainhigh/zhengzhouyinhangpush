package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushForbidDTO;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 命令构建器接口
 */
public interface SendCommandBeforeBuilderService {
    /**
     * 思科命令行构建方法
     *
     * @param commandLine
     * @param password
     * @return
     */
    String ciscoCommandBuild(String commandLine, String password);

    /**
     * 华为6000
     *
     * @param commandLine
     * @param pushForbidDTO
     * @return
     */
    String u6000CommandBuild(String commandLine, PushForbidDTO pushForbidDTO);

    /**
     * 中兴构建命令
     *
     * @param commandLine
     * @param password
     * @return
     */
    String zteRouterCommandBuild(String commandLine, String password);

    /**
     * 山石构建
     *
     * @param commandLine
     * @param pushForbidDTO
     * @return
     */
    String hillStoneCommandBuild(String commandLine, PushForbidDTO pushForbidDTO);

    /**
     * 天融信
     *
     * @param commandLine
     * @return
     */
    String topsecCommandBuild(String commandLine);

    /**
     * 新华三
     *
     * @param commandLine
     * @return
     */
    String h3cCommandBuild(String commandLine);

    /**
     * 迪普
     *
     * @param commandLine
     * @return
     */
    String dpTechCommandBuild(String commandLine);

    /**
     * 获取构建之后的命令行统一方法
     *
     * @param pushCmdDTO
     * @return
     */
    String getSshBuildCommand(PushCmdDTO pushCmdDTO);

    /**
     * 根据设备删除指定命令行统一方法
     *
     * @param lastDeviceModelNumberEnum
     * @return
     */
    String delSshCommand(DeviceModelNumberEnum lastDeviceModelNumberEnum, String lastCommandLine, String enablePassword, PushForbidDTO pushForbidDTO);

    /**
     * JUNIPER_SSG 命令构建
     *
     * @param pushCmdDTO
     * @return
     */
    String junSsgBuildCommand(String pushCmdDTO);

    /**
     * telnet构建命令行
     * 先输入用户
     * 再输入密码
     *
     * @param pushCmdDTO
     * @return
     */
    String getTelnetBuildCommand(PushCmdDTO pushCmdDTO);

    /**
     * 启明 命令行构建
     *
     * @param pushCmdDTO
     * @return
     */

    String getVenusTchBuildCommand(String pushCmdDTO);

    /**
     * srx 命令行构建
     *
     * @param commandLine
     * @return
     */
    String junSrxBuildCommand(String commandLine);

    /**
     * checkPoint 命令构建
     *
     * @param pushCmdDTO
     * @return
     */
    String checkPointBuildCommand(PushCmdDTO pushCmdDTO);

    /**
     * h华为u2000拼接命令
     *
     * @param commandLine
     * @return
     */
    String u2000BuildCommand(String commandLine);

    /**
     * 中科网威
     *
     * @param pushCmdDTO
     * @return
     */
    String netPowerSaveCommand(PushCmdDTO pushCmdDTO);

}
