package com.abtnetworks.totems.mapping.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingSceneRuleService;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingSceneRuleVO;
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

@Api(tags ="场景规则")
@RestController
@RequestMapping(value="/auto/sceneRule")
public class PushAutoMappingSceneRuleController extends BaseController {

    private static Logger logger = Logger.getLogger(PushAutoMappingSceneRuleController.class);

    @Autowired
    private AutoMappingSceneRuleService autoMappingSceneRuleService;

    @ApiOperation(value = "新增或修改", httpMethod = "POST")
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public TotemsReturnT addTask(@RequestBody AutoMappingSceneRuleVO sceneRuleVO, Authentication auth) throws Exception {
        try{
            String authName = auth.getName();
            sceneRuleVO.setCreateUser(authName);
            return autoMappingSceneRuleService.addOrUpdate(sceneRuleVO);
        } catch ( Exception  e ) {
            logger.error("新增场景规则异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"新增场景规则异常");
        }
    }

    @ApiOperation(value = "分页查询场景规则", httpMethod = "POST")
    @RequestMapping(value = "/selectAll", method = RequestMethod.POST)
    public TotemsReturnT select(@RequestBody AutoMappingSceneRuleVO vo) {
        try{
            PageInfo<PushAutoMappingSceneRuleEntity> pageInfoList = autoMappingSceneRuleService.findList(vo, vo.getPage(), vo.getLimit());
            return new TotemsReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询场景规则，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "查询场景规则异常");
        }
    }

    @ApiOperation(value = "删除", httpMethod = "POST")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public TotemsReturnT delete(@RequestBody AutoIdVO vo) {
        try{
            if (CollectionUtils.isEmpty(vo.getIdList())){
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
            }
            for (Integer integer : vo.getIdList()) {
                PushAutoMappingSceneRuleEntity entity = autoMappingSceneRuleService.selectById(integer);
                if (ObjectUtils.isEmpty(entity)){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "id为"+integer+"的数据不存在");
                }
            }
            int i = autoMappingSceneRuleService.deleteIdList(vo.getIdList());
            return new TotemsReturnT(i);
        }catch (Exception e) {
            logger.error("删除，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "删除");
        }
    }


}
