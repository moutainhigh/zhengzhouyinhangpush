package com.abtnetworks.totems.advanced.controller;

import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.advanced.entity.SceneForFiveBalanceEntity;
import com.abtnetworks.totems.advanced.service.SceneForFiveBalanceService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
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
 * @author lifei
 * @desc F5场景 控制类
 * @date 2021/7/30 17:30
 */
@Slf4j
@Api("F5场景controller")
@RestController
@RequestMapping(value = "/scene-five/")
public class SceneForFiveBalanceController extends BaseController {

    @Autowired
    private SceneForFiveBalanceService sceneForFiveBalanceService;


    @ApiOperation("增加F5场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "applyType", value = "应用发布类型", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "loadBlanaceMode", value = "节点负载模式", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "persist", value = "节点回话保持", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "monitor", value = "健康检查", required = false, dataType = "String"),

    })
    @PostMapping("/insert")
    public TotemsReturnT saveScene(@RequestBody SceneForFiveBalanceDTO sceneForFiveBalanceDTO, Authentication auth) {
        try {
            validatorParam(sceneForFiveBalanceDTO);
            sceneForFiveBalanceDTO.setCreateUser(auth.getName());
            int rc= sceneForFiveBalanceService.createSceneForFiveBalance(sceneForFiveBalanceDTO);
            if(ReturnCode.POLICY_MSG_OK == rc){
                return new TotemsReturnT(rc);
            }else {
                return new TotemsReturnT(-1,ReturnCode.getMsg(rc));
            }
        } catch (Exception e) {
            log.error("增加F5场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("更新F5场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "applyType", value = "应用发布类型", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "loadBlanaceMode", value = "节点负载模式", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "persist", value = "节点回话保持", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "monitor", value = "健康检查", required = false, dataType = "String"),
    })
    @PostMapping("/update")
    public TotemsReturnT updateScene(@RequestBody SceneForFiveBalanceDTO sceneForFiveBalanceDTO, Authentication auth) {
        try {
            sceneForFiveBalanceDTO.setCreateUser(auth.getName());
            long i = sceneForFiveBalanceService.updateSceneForFiveBalance(sceneForFiveBalanceDTO);
            return new TotemsReturnT(i);
        } catch (Exception e) {
            log.error("更新F5场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("删除F5场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表,多个逗号分隔", required = true, dataType = "String")
    })
    @PostMapping("/delete")
    public TotemsReturnT deleteScene(String ids) {
        try {
            long i = sceneForFiveBalanceService.deleteById(ids);
            return new TotemsReturnT(i);
        } catch (Exception e) {
            log.error("更新F5场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("查询F5场景列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sceneName", value = "场景名称", required = false, dataType = "String"),
    })
    @PostMapping("/querySceneList")
    public TotemsReturnT querySceneList(@RequestBody SceneForFiveBalanceDTO sceneForFiveBalanceDTO) {
        try {
            PageInfo<SceneForFiveBalanceEntity> list = sceneForFiveBalanceService.querySceneForFiveBalanceList(sceneForFiveBalanceDTO);
            return new TotemsReturnT(list);
        } catch (Exception e) {
            log.error("查询F5场景列表异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

}
