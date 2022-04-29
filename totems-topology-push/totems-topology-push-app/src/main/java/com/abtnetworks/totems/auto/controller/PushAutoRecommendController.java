package com.abtnetworks.totems.auto.controller;

import com.abtnetworks.totems.auto.dto.AutoRecommendTaskNatInfoDTO;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.enums.AutoRecommendStatusEnum;
import com.abtnetworks.totems.auto.service.PushAutoRecommendForApiService;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.vo.*;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.constants.PushStatusConstans;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc    自动开通工单
 * @author liuchanghao
 * @date 2021-06-09 14:00
 */
@Api(tags ="自动开通工单")
@RestController
@RequestMapping(value="/auto")
public class PushAutoRecommendController extends BaseController {

    private static Logger logger = Logger.getLogger(PushAutoRecommendController.class);

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Autowired
    private PushAutoRecommendForApiService pushAutoRecommendForApiService;


    @ApiOperation(value = "新建自动开通任务", httpMethod = "POST", notes = "新建自动开通任务，根据策略参数生成建议策略，并生成命令行")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ReturnT addTask(@RequestBody AutoRecommendTaskVO autoRecommendTaskVO, Authentication auth) throws Exception {
        try{
            autoRecommendTaskVO.setUserName(auth.getName());
            return pushAutoRecommendService.addTask(autoRecommendTaskVO);
        } catch ( Exception  e ) {
            logger.error("新建自动开通任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"新建自动开通任务异常");
        }
    }


    @ApiOperation(value = "删除自动开通任务", httpMethod = "POST")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ReturnT delete(@RequestBody AutoRecommendTaskVO autoRecommendTaskVO) {
        try{
            return pushAutoRecommendService.delete(autoRecommendTaskVO);
        }catch (Exception e) {
            logger.error("删除防护网段配置异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "删除自动开通任务异常");
        }
    }


    @ApiOperation(value = "分页查询自动开通任务列表", httpMethod = "POST")
    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public ReturnT select(@RequestBody AutoRecommendTaskSearchVO vo) {
        try{
            PageInfo<AutoRecommendTaskEntity> pageInfoList = pushAutoRecommendService.findList(vo, vo.getPage(), vo.getLimit());
            return new ReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询自动开通任务列表，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "查询自动开通列表异常");
        }
    }

    @ApiOperation(value = "获取Nat信息", httpMethod = "POST")
    @RequestMapping(value = "/getNatInfo", method = RequestMethod.POST)
    public ReturnT getNatInfo(@RequestBody AutoRecommendTaskGetInfoVO getInfoVO) {
        try{
            AutoRecommendTaskNatInfoDTO natInfo = pushAutoRecommendService.getNatInfo(getInfoVO);
            if(ObjectUtils.isEmpty(natInfo)){
                return new ReturnT(ReturnT.FAIL_CODE, "获取工单信息失败，策略建议不存在或已删除");
            }
            return new ReturnT(natInfo);
        }catch (Exception e) {
            logger.error("获取Nat信息异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "获取Nat信息异常");
        }
    }


    @ApiOperation(value = "自动开通任务下发", httpMethod = "POST")
    @RequestMapping(value = "/autoPush", method = RequestMethod.POST)
    public ReturnT autoPush(@RequestBody AutoRecommendTaskVO autoRecommendTaskVO) {
        try{
            if(ObjectUtils.isEmpty(autoRecommendTaskVO) || ObjectUtils.isEmpty(autoRecommendTaskVO.getIdList())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendService.autoPush(autoRecommendTaskVO);
        }catch (Exception e) {
            logger.error("自动开通任务下发异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "自动开通任务下发异常");
        }
    }

    @ApiOperation(value = "设备任务下发", httpMethod = "POST")
    @RequestMapping(value = "/devicePush", method = RequestMethod.POST)
    public ReturnT devicePush(@RequestBody AutoRecommendTaskVO autoRecommendTaskVO) {
        try{
            if(ObjectUtils.isEmpty(autoRecommendTaskVO) || ObjectUtils.isEmpty(autoRecommendTaskVO.getIdList()) || ObjectUtils.isEmpty(autoRecommendTaskVO.getTaskIdList())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendService.autoPush(autoRecommendTaskVO);
        }catch (Exception e) {
            logger.error("自动开通设备下发异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "自动开通设备下发异常");
        }
    }

    @ApiOperation(value = "查询自动开通结果报告页详情", httpMethod = "POST")
    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public ReturnT result(@RequestBody AutoRecommendTaskVO autoRecommendTaskVO) {
        try{
            if(ObjectUtils.isEmpty(autoRecommendTaskVO)){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendService.getResult(autoRecommendTaskVO);
        }catch (Exception e) {
            logger.error("查询自动开通结果报告页详情异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "查询自动开通结果报告页详情异常");
        }
    }

    @ApiOperation(value = "中兴通讯OA对接创建任务接口", httpMethod = "POST")
    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    public ReturnT createTask(@RequestBody AutoRecommendTaskApiVO autoRecommendTaskVO) {
        try{
            if(ObjectUtils.isEmpty(autoRecommendTaskVO) || ObjectUtils.isEmpty(autoRecommendTaskVO.getTheme())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendForApiService.createTask(autoRecommendTaskVO);
        }catch (Exception e) {
            logger.error("OA对接创建工单任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "OA对接创建工单任务异常");
        }
    }

    @ApiOperation(value = "获取自动开通工单状态及策略日志信息", httpMethod = "POST")
    @RequestMapping(value = "/getStatusInfo", method = RequestMethod.POST)
    public ReturnT getStatusInfo(@RequestBody AutoRecommendConflictVo conflictVo) {
        try{
            if(ObjectUtils.isEmpty(conflictVo)){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendService.getStatusInfo(conflictVo);
        }catch (Exception e) {
            logger.error("获取自动开通工单状态及策略日志信息异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "获取自动开通工单状态及策略日志信息异常");
        }
    }

    @ApiOperation(value = "根据设备查询自动开通详情", httpMethod = "POST")
    @RequestMapping(value = "/getByDevice", method = RequestMethod.POST)
    public ReturnT getByDevice(@RequestBody AutoRecommendConflictVo conflictVo) {
        try{
            if(ObjectUtils.isEmpty(conflictVo) || ObjectUtils.isEmpty(conflictVo.getAutoTaskId()) || ObjectUtils.isEmpty(conflictVo.getDeviceUuid())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushAutoRecommendService.getByDevice(conflictVo);
        }catch (Exception e) {
            logger.error("根据设备查询自动开通详情异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "根据设备查询自动开通详情异常");
        }
    }


}
