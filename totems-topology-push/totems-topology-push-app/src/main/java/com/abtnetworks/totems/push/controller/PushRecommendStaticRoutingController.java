package com.abtnetworks.totems.push.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.service.PushTaskStaticRoutingService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lb
 * @date 2021/8/2 18:09
 */
@Slf4j
@Api(value = "静态路由任务生成控制层")
@RestController
@RequestMapping(value = "/task/static")
public class PushRecommendStaticRoutingController extends BaseController {


    @Autowired
    private PushTaskStaticRoutingService pushTaskStaticRoutingService;


    @ApiOperation("增加静态路由生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "deviceName", value = "设备名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "工单号", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcVirtualRouter", value = "所属虚拟路由器", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstVirtualRouter", value = "目的虚拟路由器", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "outInterface", value = "出接口", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ipType", value = "ip类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "nextHop", value = "下一跳", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "priority", value = "优先级", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "mark", value = "备注", required = false, dataType = "String"),
    })
    @PostMapping("/addStaticRoutingTask")
    public TotemsReturnT addStaticRoutingTask(@RequestBody PushRecommendStaticRoutingDTO pushRecommendStaticRoutingDTO, Authentication auth) {
        try {
            validatorParam(pushRecommendStaticRoutingDTO);
            pushRecommendStaticRoutingDTO.setCreateUser(auth.getName());
            int rc = pushTaskStaticRoutingService.createPushTaskStaticRouting(pushRecommendStaticRoutingDTO);

            if(rc != ReturnCode.POLICY_MSG_OK) {
                return new TotemsReturnT(-1,ReturnCode.getMsg(rc));
            }

            return new TotemsReturnT(ReturnCode.getMsg(rc));
        } catch (Exception e) {
            log.error("增加静态路由策略生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }


    @ApiOperation("删除静态路由生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表,多个逗号分隔", required = true, dataType = "String")
    })
    @PostMapping("/delete")
    public TotemsReturnT deleteTask(String ids) {
        try {
            int i = pushTaskStaticRoutingService.deletePushTaskStaticRouting(ids);
            return new TotemsReturnT(i);
        } catch (Exception e) {
            log.error("删除静态路由生成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("查询静态路由生成任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "currentPage", value = "当前页", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = true, dataType = "Integer"),
    })
    @PostMapping("/queryStaticRoutingList")
    public TotemsReturnT queryFiveBalanceList(@RequestBody PushRecommendStaticRoutingDTO pushRecommendStaticRoutingDTO, Authentication auth) {
        try {
            pushRecommendStaticRoutingDTO.setCreateUser(auth.getName());

            PageInfo<PushRecommendStaticRoutingDTO> list = pushTaskStaticRoutingService.findPushTaskStaticRoutingPage(pushRecommendStaticRoutingDTO);

            return new TotemsReturnT(list);
        } catch (Exception e) {
            log.error("查询静态路由成任务异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }


}
