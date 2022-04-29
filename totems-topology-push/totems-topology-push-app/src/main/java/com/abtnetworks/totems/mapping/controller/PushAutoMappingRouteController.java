package com.abtnetworks.totems.mapping.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingRouteEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingRouteService;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingRouteSearchVO;
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

@Api(tags ="路由匹配")
@RestController
@RequestMapping(value="/auto/Route")
public class PushAutoMappingRouteController extends BaseController {

    private static Logger logger = Logger.getLogger(PushAutoMappingRouteController.class);

    @Autowired
    private AutoMappingRouteService autoMappingRouteService;

    @ApiOperation(value = "新增", httpMethod = "POST")
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public TotemsReturnT addOrUpdate(@RequestBody PushAutoMappingRouteEntity routeEntity, Authentication auth) throws Exception {
        try{
            String authName = auth.getName();
            routeEntity.setCreateUser(authName);
            return autoMappingRouteService.addOrUpdate(routeEntity);
        } catch ( Exception  e ) {
            logger.error("工单检测异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"工单检测异常");
        }
    }

    @ApiOperation(value = "分页查询", httpMethod = "POST")
    @RequestMapping(value = "/selectAll", method = RequestMethod.POST)
    public TotemsReturnT select(@RequestBody AutoMappingRouteSearchVO vo) {
        try{
            PageInfo<PushAutoMappingRouteEntity> pageInfoList = autoMappingRouteService.findList(vo, vo.getPage(), vo.getLimit());
            return new TotemsReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "查询工单异常");
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
                PushAutoMappingRouteEntity entity = autoMappingRouteService.selectById(integer);
                if (ObjectUtils.isEmpty(entity)){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "id为"+integer+"的数据不存在");
                }
            }
            int i = autoMappingRouteService.deleteIdList(vo.getIdList());
            return new TotemsReturnT(i);
        }catch (Exception e) {
            logger.error("删除，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "删除异常");
        }
    }


}
