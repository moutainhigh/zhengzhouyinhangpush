package com.abtnetworks.totems.push.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.vo.ResultResponseVO;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushParamDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.service.PushBussExtendService;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.push.vo.CheckRelevancyNatOrderVO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import com.abtnetworks.totems.push.vo.FivePushInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: push下发业务扩展
 * @date 2021/2/24
 */
@Slf4j
@Api(value = "策略下发业务扩展控制层")
@RestController
@RequestMapping(value = "/task/")
public class PushBussExtendController extends BaseController {

    @Autowired
    PushBussExtendService pushBussExtendService;

    @Autowired
    private PushService pushService;

    @Autowired
    private SendCommandService sendCommandService;



    @ApiOperation("批量开始下发任务时校验是否关联nat")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表", required = true, dataType = "String")
    })
    @PostMapping("checkNatOrder")
    public ResultResponseVO checkRelevancyNatOrder(@RequestParam String ids) {
        log.debug("批量开始下发任务时校验是否关联nat开始{}", ids);
        try {
            List<CheckRelevancyNatOrderVO> checkRelevancyNatOrderVOS = pushBussExtendService.checkRelevancyNatOrder(ids);
            log.debug("批量开始下发任务时校验是否关联nat结束");
            return returnResponseSuccess(checkRelevancyNatOrderVOS);
        } catch (Exception e) {
            log.error("批量开始下发任务时校验是否关联nat异常结束", e);
            return returnResponseFail("-2", "系统异常");
        }
    }

    @ApiOperation("下发命令行")
    @PostMapping("pushCommandLine")
    public ReturnT pushCommandLine(@ApiParam(name="PushParamDTO", value="下发命令行", required=true) @RequestBody PushParamDTO pushParamDTO) {
        String pushKey = pushParamDTO.getPushKey();
        String deviceUuid = pushParamDTO.getDeviceUuid();
        String commandLines = pushParamDTO.getCommandLine();
        log.info("进入pushCmdDTO的参数构建");
        PushCmdDTO pushCmdDTO=  pushService.buildPushCmdDTO(deviceUuid,commandLines);
        logger.info("pushCmdDTO:{}", JSONObject.toJSONString(pushCmdDTO));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO, pushKey);
        return new ReturnT(pushResultDTO);
    }


    @ApiOperation("动态展示展示下发过程命令行")
    @PostMapping("showPushCommandLine")
    public ReturnT showPushCommandLine(@ApiParam(name="pushKey", value="下发命令行的randomKey", required=true) @RequestBody String pushKey) {
        if(StringUtils.isBlank(pushKey)){
            return new ReturnT(ReturnT.FAIL_CODE,"下发命令行的randomKey为空");
        }
        List<String> commandLines = SendCommandStaticAndConstants.echoCmdMap.get(pushKey);
        return new ReturnT(commandLines);
    }



    @ApiOperation("获取pool和snatpool集合名称和profileName集合")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String")
    })
    @PostMapping("/queryPoolNameAndProfileName")
    public TotemsReturnT queryPoolNameAndProfileName(@RequestParam String deviceUuid) {
        try {
            FivePushInfoVo fivePushInfoVo = pushBussExtendService.queryPoolNameForFive(deviceUuid);
            return new TotemsReturnT(fivePushInfoVo);
        } catch (Exception e) {
            return new TotemsReturnT("-2", "系统异常");
        }
    }

    @ApiOperation("获取虚拟路由器名称")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String")
    })
    @PostMapping("/queryRouteNames")
    public TotemsReturnT queryRouteNames(@RequestParam String deviceUuid) {
        try {
            List<String> routeNames = pushBussExtendService.queryRouteNames(deviceUuid);
            return new TotemsReturnT(routeNames);
        } catch (Exception e) {
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("获取出接口名称")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String")
    })
    @PostMapping("/queryInterfaceNames")
    public TotemsReturnT queryInterfaceNames(@RequestParam String deviceUuid) {
        try {
            List<String> interfaceNames = pushBussExtendService.queryInterfaceNames(deviceUuid);
            return new TotemsReturnT(interfaceNames);
        } catch (Exception e) {
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

}
