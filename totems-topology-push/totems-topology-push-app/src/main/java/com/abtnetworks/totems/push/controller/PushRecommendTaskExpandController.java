package com.abtnetworks.totems.push.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushRecommendTaskExpandDTO;
import com.abtnetworks.totems.push.service.PushTaskFiveBalanceService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lifei
 * @desc F5设备策略任务生成
 * @date 2021/8/2 18:09
 */
@Slf4j
@Api(value = "F5策略任务生成控制层")
@RestController
@RequestMapping(value = "/task/five")
public class PushRecommendTaskExpandController extends BaseController {

    @Autowired
    private PushTaskFiveBalanceService pushTaskFiveBalanceService;

    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${importFiveNatExcelFile}")
    private String fiveNatExcelFile;

    @ApiOperation("增加F5策略生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "deviceName", value = "设备名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "工单号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "sceneUuid", value = "场景uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "poolInfo", value = "pool信息", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "snatType", value = "snat类型", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "snatPoolInfo", value = "snatPool信息", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源ip", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的ip", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceInfo", value = "服务信息", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "httpProfile", value = "httpProfile", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "sslProfile", value = "证书名称", required = false, dataType = "String"),
    })
    @PostMapping("/addFiveBalanceTask")
    public TotemsReturnT addFiveBalanceTask(@RequestBody PushRecommendTaskExpandDTO pushRecommendTaskExpandDTO, Authentication auth) {
        try {
            validatorParam(pushRecommendTaskExpandDTO);
            pushRecommendTaskExpandDTO.setCreateUser(auth.getName());
            int rc = pushTaskFiveBalanceService.createPushTaskFiveBalance(pushRecommendTaskExpandDTO);
            return new TotemsReturnT(ReturnCode.getMsg(rc));
        } catch (Exception e) {
            log.error("增加F5策略生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("更新F5策略生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", value = "主键id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "deviceName", value = "设备名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "工单号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "sceneUuid", value = "场景uuid", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "poolInfo", value = "pool信息(包括snatPool和pool)", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源ip", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "源ip", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceInfo", value = "服务信息", required = false, dataType = "String"),
    })
    @PostMapping("/update")
    public TotemsReturnT updateTask(@RequestBody PushRecommendTaskExpandDTO pushRecommendTaskExpandDTO, Authentication auth) {
        try {
            pushRecommendTaskExpandDTO.setCreateUser(auth.getName());
            long i = pushTaskFiveBalanceService.updatePushTaskFiveBalance(pushRecommendTaskExpandDTO);
            return new TotemsReturnT(i);
        } catch (Exception e) {
            log.error("更新F5场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("删除F5策略生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表,多个逗号分隔", required = true, dataType = "String")
    })
    @PostMapping("/delete")
    public TotemsReturnT deleteTask(String ids) {
        try {
            long i = pushTaskFiveBalanceService.deletePushTaskFiveBalance(ids);
            return new TotemsReturnT(i);
        } catch (Exception e) {
            log.error("删除F5策略生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("查询F5策略生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = false, dataType = "String"),
    })
    @PostMapping("/queryFiveBalanceList")
    public TotemsReturnT queryFiveBalanceList(@RequestBody PushRecommendTaskExpandDTO pushRecommendTaskExpandDTO, Authentication auth) {
        try {
            pushRecommendTaskExpandDTO.setCreateUser(auth.getName());
            PageInfo<PushRecommendTaskExpandDTO> list = pushTaskFiveBalanceService.findPushTaskFiveBalancePage(pushRecommendTaskExpandDTO);
            return new TotemsReturnT(list);
        } catch (Exception e) {
            log.error("查询F5策略生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }


    @ApiOperation("下载批量F5策略生成任务模板")
    @PostMapping("/downloadFiveNatTemplate")
    public TotemsReturnT downloadFiveNatTemplate() {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + fiveNatExcelFile);
            status = "0";
        } catch (Exception e) {
            errmsg += e;
            logger.error("downloadHostTemplate：" + e);
            return new TotemsReturnT(-1, e.getMessage());
        }

        return new TotemsReturnT(jsonObject);
    }


    @ApiOperation(value = "批量F5策略生成", httpMethod = "POST", notes = "根据导入Excel表，批量生成命令行")
    @PostMapping("/batchImportFivePolicy")
    public TotemsReturnT batchImportFivePolicy(MultipartFile file, Authentication auth){
        try {
            String msg = pushTaskFiveBalanceService.batchImportFivePolicy(file,auth);
            if ("导入数据成功".equals(msg)) {
                return new TotemsReturnT(msg);
            } else {
                return new TotemsReturnT(-1, msg);
            }
        } catch (Exception e) {
            log.error("查询F5策略生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }


}
