package com.abtnetworks.totems.advanced.controller;

import com.abtnetworks.totems.common.ro.ResultRO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luwei
 * @date 2020/8/12
 */
@Api(tags = {"nginz环境设置"})
@RestController
@RequestMapping(value="/nginz-setting/")
public class NginzSettingsController {

    @Value("${push.whale}")
    private Boolean nginzFlag;

    @ApiOperation("获取nginz环境标识")
    @PostMapping("get-nginz")
    public ResultRO<Boolean> getNginz() {
        ResultRO<Boolean> resultRO = new ResultRO(true);
        if (nginzFlag == null) {
            resultRO.setData(false);
        } else {
            resultRO.setData(nginzFlag);
        }
        return resultRO;
    }
}
