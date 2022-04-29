package com.abtnetworks.totems.commandLine.controller;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.DeviceModelNumberEnumExtended;
import com.abtnetworks.totems.commandLine.vo.BaseCommandLineParamVO;
import com.abtnetworks.totems.common.TotemsReturnT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/11
 */
@Slf4j
@Api(tags = {"原子化命令行>>基础命令行"})
@RestController
@RequestMapping(value = "${startPath}/commandLine/base")
public class BaseCommandLineController {

    @ApiOperation("生成前置命令行")
    @PostMapping("generatePreCommandline")
    public TotemsReturnT generatePreCommandline(@RequestBody BaseCommandLineParamVO baseCommandLineParamVO) {
        if (baseCommandLineParamVO == null || StringUtils.isBlank(baseCommandLineParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(baseCommandLineParamVO.getModelNumber());
        if (deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }

        String preCommandLine = null;
        try {
            preCommandLine = overAllGeneratorAbstractBean.generatePreCommandline(baseCommandLineParamVO.getIsVsys(),baseCommandLineParamVO.getVsysName(),baseCommandLineParamVO.getMap(),baseCommandLineParamVO.getArgs());

        } catch (Exception e) {
            log.error("前置命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(preCommandLine);
    }

    @ApiOperation("生成后置命令行")
    @PostMapping("generatePostCommandline")
    public TotemsReturnT generatePostCommandline(@RequestBody BaseCommandLineParamVO baseCommandLineParamVO) {
        if (baseCommandLineParamVO == null || StringUtils.isBlank(baseCommandLineParamVO.getModelNumber())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "必要参数缺失");
        }
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(baseCommandLineParamVO.getModelNumber());
        if (deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }

        String postCommandLine = null;
        try {
            postCommandLine = overAllGeneratorAbstractBean.generatePostCommandline(baseCommandLineParamVO.getMap(),baseCommandLineParamVO.getArgs());

        } catch (Exception e) {
            log.error("后置命令行生成异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        }

        return new TotemsReturnT(postCommandLine);
    }
}
