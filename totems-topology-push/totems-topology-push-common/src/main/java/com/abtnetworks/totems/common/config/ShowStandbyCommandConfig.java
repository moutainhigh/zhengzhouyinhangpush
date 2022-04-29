package com.abtnetworks.totems.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author lifei
 * @desc show主备命令行配置
 * @date 2022/1/20 18:09
 */
@Configuration
@Data
public class ShowStandbyCommandConfig {

    /***查询思科主备命令行**/
    @Value("${show-state-commandline.cisco:''}")
    private String checkCiscoCommandline;

    /***查询华为主备命令行**/
    @Value("${show-state-commandline.huawei:''}")
    private String checkHuaWeiCommandline;

    /***查询juniper-srx主备命令行**/
    @Value("${show-state-commandline.juniper-srx:''}")
    private String checkJuniperSrxCommandline;

    /***查询juniper-ssg主备命令行**/
    @Value("${show-state-commandline.juniper-ssg:''}")
    private String checkJuniperSsgCommandline;

    /***查询天融信主备命令行**/
    @Value("${show-state-commandline.topsec:''}")
    private String checkTopsecCommandline;

    /***查询迪普主备命令行**/
    @Value("${show-state-commandline.dptech:''}")
    private String checkDptechCommandline;

    /***查询山石主备命令行**/
    @Value("${show-state-commandline.hillstone:''}")
    private String checkHillstoneCommandline;

    /***查询飞塔主备命令行**/
    @Value("${show-state-commandline.fortinet:''}")
    private String checkFortinetCommandline;


    /***飞塔主备匹配正则**/
    @Value("${Cisco.match-standby:''}")
    private String ciscoMatchStandby;
}
