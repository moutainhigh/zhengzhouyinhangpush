package com.abtnetworks.totems.mapping.controller;

import com.abtnetworks.totems.auto.vo.AutoRecommendTaskSearchVO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingTaskService;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.OrderCheckVO;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc    地址映射自动匹配controller
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */
@Api(tags ="地址映射自动匹配")
@RestController
@RequestMapping(value="/auto/mapping")
public class PushAutoMappingTaskController extends BaseController {

    private static Logger logger = Logger.getLogger(PushAutoMappingTaskController.class);

    @Autowired
    private AutoMappingTaskService autoMappingTaskService;

    @ApiOperation(value = "工单检测", httpMethod = "POST", notes = "新建工单检测数据，根据输入数据生成工单检测任务")
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public TotemsReturnT addTask(@RequestBody OrderCheckVO checkVO, Authentication auth) throws Exception {
        try{
            return autoMappingTaskService.check(checkVO,auth);
        } catch ( Exception  e ) {
            logger.error("工单检测异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"工单检测异常");
        }
    }

    @ApiOperation(value = "分页查询工单检测任务表", httpMethod = "POST")
    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public TotemsReturnT select(@RequestBody AutoRecommendTaskSearchVO vo) {
        try{
            PageInfo<PushAutoMappingTaskEntity> pageInfoList = autoMappingTaskService.findList(vo, vo.getPage(), vo.getLimit());
            return new TotemsReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询自动开通任务列表，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "查询自动开通列表异常");
        }
    }

    @ApiOperation(value = "删除工单检测任务表", httpMethod = "POST")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public TotemsReturnT delete(@RequestBody AutoIdVO vo) {
        try{
            if (CollectionUtils.isEmpty(vo.getIdList())){
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
            }
            for (Integer integer : vo.getIdList()) {
                PushAutoMappingTaskEntity entity = autoMappingTaskService.selectById(integer);
                if (ObjectUtils.isEmpty(entity)){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "id为"+integer+"的数据不存在");
                }
            }

            int i = autoMappingTaskService.deleteIdList(vo.getIdList());
            return new TotemsReturnT(i);
        }catch (Exception e) {
            logger.error("删除自动开通任务列表，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "删除自动开通列表异常");
        }
    }

    @ApiOperation(value = "加入仿真任务", httpMethod = "POST", notes = "将新建工单检测数据加入到仿真任务中")
    @RequestMapping(value = "/addRecommend", method = RequestMethod.POST)
    public TotemsReturnT addRecommend(@RequestBody AutoIdVO autoIdVO, Authentication auth) throws Exception {
        try{
            if (CollectionUtils.isEmpty(autoIdVO.getIdList())){
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
            }
            return autoMappingTaskService.addRecommendTask(autoIdVO.getIdList(), auth);
        } catch ( Exception  e ) {
            logger.error("工单检测异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"工单检测异常");
        }
    }

    @ApiOperation(value = "根据NatId或者路由ID查询", httpMethod = "POST")
    @RequestMapping(value = "/getByd", method = RequestMethod.POST)
    public TotemsReturnT getByd(@RequestBody AutoIdVO autoIdVO, Authentication auth) throws Exception {
        try{
            if (ObjectUtils.isEmpty(autoIdVO.getNatOrRouteId())){
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
            }
            return autoMappingTaskService.getById(autoIdVO);
        } catch ( Exception  e ) {
            logger.error("根据NatId或者路由ID查询任务异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"根据NatId或者路由ID查询异常");
        }
    }

}
