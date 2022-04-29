package com.abtnetworks.totems.auto.controller;

import com.abtnetworks.totems.auto.entity.PushZoneLimitConfigEntity;
import com.abtnetworks.totems.auto.service.PushZoneLimitConfigService;
import com.abtnetworks.totems.auto.vo.PushZoneLimitConfigVO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc    高级设置——生成策略域限制配置
 * @note    本接口未放置在高级设置相关的controller下，原因为：
 *             1.高级设置的表结构不满足当前业务需求；
 *             2.本设置控制范围为自动开通相关的策略建议生成，因此放到auto目录下；
 * @author liuchanghao
 * @date 2021-11-16 11:16
 */
@Api(tags ="高级设置—生成策略域限制配置")
@RestController
@RequestMapping(value="/zone/limit")
public class PushZoneLimitConfigController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(PushZoneLimitConfigController.class);

    @Autowired
    private PushZoneLimitConfigService pushZoneLimitConfigService;


    @ApiOperation(value = "新建或修改策略域限制配置", httpMethod = "POST", notes = "新建自动开通任务，根据策略参数生成建议策略，并生成命令行")
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public ReturnT addTask(@RequestBody PushZoneLimitConfigVO pushZoneLimitConfigVO) throws Exception {
        try{
            if(StringUtils.isBlank(pushZoneLimitConfigVO.getSrcZone()) || StringUtils.isBlank(pushZoneLimitConfigVO.getDstZone()) ||
                    StringUtils.isBlank(pushZoneLimitConfigVO.getDeviceInfo())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushZoneLimitConfigService.addOrUpdate(pushZoneLimitConfigVO);
        } catch ( Exception  e ) {
            logger.error("新建自动开通任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"新建或修改策略域限制配置异常");
        }
    }


    @ApiOperation(value = "删除策略域限制配置", httpMethod = "POST")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ReturnT delete(@RequestBody PushZoneLimitConfigVO pushZoneLimitConfigVO) {
        try{
            if(ObjectUtils.isEmpty(pushZoneLimitConfigVO.getId())){
                return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
            }
            return pushZoneLimitConfigService.delete(pushZoneLimitConfigVO);
        }catch (Exception e) {
            logger.error("删除策略域限制配置异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "策略域限制配置异常");
        }
    }

    @ApiOperation(value = "分页查询域限制配置列表", httpMethod = "POST")
    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public ReturnT select(@RequestBody PushZoneLimitConfigVO pushZoneLimitConfigVO) {
        try{
            PageInfo<PushZoneLimitConfigEntity> pageInfoList = pushZoneLimitConfigService.findList(pushZoneLimitConfigVO,pushZoneLimitConfigVO.getPage(), pushZoneLimitConfigVO.getLimit());
            return new ReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询域限制配置列表异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "分页查询域限制配置列表异常");
        }
    }

}
