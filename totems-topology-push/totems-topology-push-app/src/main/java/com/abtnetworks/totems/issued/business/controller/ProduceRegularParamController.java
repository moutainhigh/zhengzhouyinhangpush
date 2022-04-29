package com.abtnetworks.totems.issued.business.controller;

import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.issued.business.service.RegularParamForMatchService;
import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;





/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/6/30
 */
@Slf4j
@Api(value = "正则表达式提供远程调用")
@RestController
@RequestMapping(value = "${startPath}/remote/regular")
public class ProduceRegularParamController {

    @Resource
    RegularParamForMatchService regularParamForMatchService;

    /**
     * 收集型号的命令参数
     *
     * @param pushCmdDTO
     * @return
     */
    @ApiOperation("远程提供正则表达式参数")
    @PostMapping("/getParam")
    public ReturnResult produceCommandRegParam(@RequestBody PushCmdDTO pushCmdDTO) {

        try {
            log.info("远程提供正则表达式参数接口START{}", JSONObject.toJSONString(pushCmdDTO));
            PushCommandRegularParamDTO pushCommandRegularParamDTO = regularParamForMatchService.produceRegexParam(pushCmdDTO);
            log.info("远程提供正则表达式参数接口END{}", pushCommandRegularParamDTO);
            return new ReturnResult(pushCommandRegularParamDTO);
        } catch (Exception e) {
            log.error("系统异常", e);
            return ReturnResult.FAIL;
        }
    }


    @ApiOperation("远程的api接口")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ip", value = "主机ip", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "username", value = "登录用户名", required = true, dataType = "String")
    })
    @PostMapping("/getFortressApi")
    public ReturnResult produceApi(String ip,String username) {
        try {
            String s = regularParamForMatchService.python2Fortress(ip, username);
            if ( StringUtils.isNotBlank(s)) {
                return new ReturnResult(s);
            } else {
                log.error("没有查到密码");

            }
            log.info("远程的api接口END");
            return new ReturnResult();
        } catch (Exception e) {
            log.error("系统异常", e);
            return ReturnResult.FAIL;
        }

    }
}
