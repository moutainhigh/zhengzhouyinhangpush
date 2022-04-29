package com.abtnetworks.totems.generate.service.platform.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratePlatformApiCmdDTO;
import com.abtnetworks.totems.generate.service.platform.GeneratePlatformApiCmdService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @desc    生成管理平台API对接命令行接口
 * @author liuchanghao
 * @date 2021-02-19 16:45
 */
@Slf4j
@Service
public class GeneratePlatformApiCmdServiceImpl implements GeneratePlatformApiCmdService {

    private static Logger logger = LoggerFactory.getLogger(GeneratePlatformApiCmdServiceImpl.class);

    @Override
    public String generateFortinetApiParams(CmdDTO cmdDTO) {
        logger.info("cmdDTO数据信息为：[{}]", JSON.toJSONString(cmdDTO));
        GeneratePlatformApiCmdDTO platformApiCmdDTO = new GeneratePlatformApiCmdDTO();
        platformApiCmdDTO.setTask(cmdDTO.getTask());
        platformApiCmdDTO.setDevice(cmdDTO.getDevice());
        platformApiCmdDTO.setPolicy(cmdDTO.getPolicy());
        platformApiCmdDTO.setSetting(cmdDTO.getSetting());
        String apiParams = JSON.toJSONString(platformApiCmdDTO);
        logger.info("生成的管理平台请求参数数据信息为：[{}]", apiParams);
        return apiParams;
    }

    @Override
    public String generateNsfocusApiParams(CmdDTO cmdDTO) {
        logger.info("绿盟cmdDTO数据信息为：[{}]", JSON.toJSONString(cmdDTO));
        GeneratePlatformApiCmdDTO platformApiCmdDTO = new GeneratePlatformApiCmdDTO();
        platformApiCmdDTO.setTask(cmdDTO.getTask());
        platformApiCmdDTO.setDevice(cmdDTO.getDevice());
        platformApiCmdDTO.setPolicy(cmdDTO.getPolicy());
        platformApiCmdDTO.setSetting(cmdDTO.getSetting());
        String apiParams = JSON.toJSONString(platformApiCmdDTO);
        logger.info("生成的绿盟api下发请求参数数据信息为：[{}]", apiParams);
        return apiParams;
    }

}
