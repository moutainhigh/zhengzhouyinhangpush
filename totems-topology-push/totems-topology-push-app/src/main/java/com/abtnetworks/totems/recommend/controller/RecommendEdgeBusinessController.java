package com.abtnetworks.totems.recommend.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushRecommendTaskExpandDTO;
import com.abtnetworks.totems.push.vo.FivePushInfoVo;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.service.RecommendEdgeBusinessService;
import com.abtnetworks.totems.recommend.service.RecommendRelevanceSceneService;
import com.abtnetworks.totems.recommend.vo.RecommendLabelVO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchLabelDTO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Administrator
 * @Title:
 * @Description: 边缘业务控制层
 * @date 2021/1/20
 */
@Slf4j
@Api(value = "策略开通边缘业务控制层")
@RestController
@RequestMapping(value = "/recommend/")
public class RecommendEdgeBusinessController extends BaseController {

    @Resource
    RecommendEdgeBusinessService recommendEdgeBusinessService;

    @Resource
    RecommendRelevanceSceneService recommendRelevanceSceneService;

    @ApiOperation("搜索标签接口")
    @PostMapping("label/list")
    public JSONObject listRecommendInternetLabel(@RequestBody SearchLabelDTO searchLabelDTO) {
        try {
            log.debug("开始查询标签{}", JSONObject.toJSONString(searchLabelDTO));
            RecommendLabelVO recommendLabelVO = recommendEdgeBusinessService.getLabelNames(searchLabelDTO);
            log.debug("查询标签接口正常结束{}", JSONObject.toJSONString(recommendLabelVO));
            return getReturnJSON(ReturnCode.POLICY_MSG_OK, recommendLabelVO);
        } catch (IllegalAccessException e) {
            log.error("查询标签接口异常结束", e);
            return getReturnJSON(-1, e.getMessage());
        } catch (Exception e) {
            log.error("查询标签接口异常结束", e);
            return getReturnJSON(-2, "系统异常");
        }

    }


    @ApiOperation("新增仿真关联飞塔nat场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "add", name = "name", value = "场景名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ipType", value = "ip类型", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "服务", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "additionInfo", value = "附加条件", required = false, dataType = "String")
    })
    @PostMapping("/addRelevanceScene")
    public TotemsReturnT addRelevanceScene(@RequestBody RecommendRelevanceSceneDTO recommendRelevanceSceneDTO, Authentication auth) {
        try {
            validatorParam(recommendRelevanceSceneDTO);
            recommendRelevanceSceneDTO.setCreateUser(auth.getName());
            recommendRelevanceSceneDTO.setCreateTime(new Date());
            int rc = recommendRelevanceSceneService.createRecommendRelevanceScene(recommendRelevanceSceneDTO);
            return new TotemsReturnT(ReturnCode.getMsg(rc));
        } catch (Exception e) {
            logger.error("新增仿真关联飞塔nat场景异常,异常原因:{}",e);
            return new TotemsReturnT("-2", "系统异常");
        }
    }

    @ApiOperation("查询关联场景列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", value = "第几页", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "一页多少条", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ids", value = "场景ids", required = false, dataType = "String")

    })
    @PostMapping("/getRelevanceSceneList")
    public TotemsReturnT getRelevanceSceneList(Integer page, Integer psize, String ids, String name, String deviceUuid, Authentication auth) {
        try {
            PageInfo<RecommendRelevanceSceneDTO> sceneDTOPageInfo = recommendRelevanceSceneService.getRecommendRelevanceScene(page, psize, ids,name,deviceUuid,auth.getName());
            return new TotemsReturnT(sceneDTOPageInfo);
        } catch (Exception e) {
            logger.error("查询仿真关联飞塔nat场景异常,异常原因:{}",e);
            return new TotemsReturnT("-2", "系统异常");
        }
    }

    @ApiOperation("根据id查询飞塔关联场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", value = "主键id", required = true, dataType = "String")

    })
    @PostMapping("/getRelevanceSceneById")
    public TotemsReturnT getRelevanceSceneList(String id) {
        try {
            RecommendRelevanceSceneDTO sceneDTO = recommendRelevanceSceneService.queryById(Integer.valueOf(id));
            return new TotemsReturnT(sceneDTO);
        } catch (Exception e) {
            logger.error("查询仿真关联飞塔nat场景异常,异常原因:{}", e);
            return new TotemsReturnT("-2", "系统异常");
        }
    }


    @ApiOperation("更新NAT场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "add", name = "name", value = "场景名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ipType", value = "ip类型", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "服务", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "additionInfo", value = "附加条件", required = false, dataType = "String")
    })
    @PostMapping("/updateRelevanceSceneById")
    public TotemsReturnT updateRelevanceSceneById(@RequestBody RecommendRelevanceSceneDTO recommendRelevanceSceneDTO, Authentication auth) {
        try {
            recommendRelevanceSceneDTO.setUpdateTime(new Date());
            recommendRelevanceSceneDTO.setCreateUser(auth.getName());
            int rc = recommendRelevanceSceneService.updateRecommendRelevanceScene(recommendRelevanceSceneDTO);
            return new TotemsReturnT(rc);
        } catch (Exception e) {
            log.error("更新NAT场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }

    @ApiOperation("删除NAT场景")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "主键id集合", required = true, dataType = "String")
    })
    @PostMapping("/deleteRelevanceSceneById")
    public TotemsReturnT deleteRelevanceSceneById(String ids) {
        try {
            int rc = recommendRelevanceSceneService.deleteRecommendRelevanceScene(ids);
            return new TotemsReturnT(rc);
        } catch (Exception e) {
            log.error("删除NAT场景异常,异常原因:{}", e);
            return new TotemsReturnT(-1, e.getMessage());
        }
    }


}
