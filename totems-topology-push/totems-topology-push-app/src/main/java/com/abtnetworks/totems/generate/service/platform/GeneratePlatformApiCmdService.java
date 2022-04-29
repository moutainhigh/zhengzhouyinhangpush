package com.abtnetworks.totems.generate.service.platform;


import com.abtnetworks.totems.common.dto.CmdDTO;

/**
 * @desc    生成管理平台API对接命令行接口
 * @author liuchanghao
 * @date 2021-02-19 16:45
 */
public interface GeneratePlatformApiCmdService {

    /**
     * 生成飞塔管理平台API请求参数，用于命令行窗口显示
     * @return
     */
    String generateFortinetApiParams(CmdDTO cmdDTO);

    /**
     * 生成绿盟API请求参数，用于命令行窗口显示
     * @return
     */
    String generateNsfocusApiParams(CmdDTO cmdDTO);

}
