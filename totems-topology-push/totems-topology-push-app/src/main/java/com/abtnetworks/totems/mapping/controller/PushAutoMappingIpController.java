package com.abtnetworks.totems.mapping.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingIpService;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
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

@Api(tags ="ip匹配")
@RestController
@RequestMapping(value="/auto/ip")
public class PushAutoMappingIpController extends BaseController {

    private static Logger logger = Logger.getLogger(PushAutoMappingIpController.class);

    @Autowired
    private AutoMappingIpService autoMappingIpService;

    @ApiOperation(value = "分页查询ip匹配表", httpMethod = "POST")
    @RequestMapping(value = "/selectAll", method = RequestMethod.POST)
    public TotemsReturnT select(@RequestBody AutoMappingIpSearchVO vo) {
        try{
            PageInfo<PushAutoMappingIpEntity> pageInfoList = autoMappingIpService.findList(vo, vo.getPage(), vo.getLimit());
            return new TotemsReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询ip匹配表，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "查询ip匹配表异常");
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
                PushAutoMappingIpEntity entity = autoMappingIpService.selectById(integer);
                if (ObjectUtils.isEmpty(entity)){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "id为"+integer+"的数据不存在");
                }
            }
            int i = autoMappingIpService.deleteIdList(vo.getIdList());
            return new TotemsReturnT(i);
        }catch (Exception e) {
            logger.error("删除ip匹配表，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "删除ip匹配表异常");
        }
    }

    @ApiOperation(value = "获取策略", httpMethod = "POST")
    @RequestMapping(value = "/selectOne", method = RequestMethod.POST)
    public TotemsReturnT selectOne(@RequestBody AutoIdVO vo) {
        try{
            if (CollectionUtils.isEmpty(vo.getIdList())){
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
            }
            PushAutoMappingIpEntity pageInfoList = autoMappingIpService.selectById(vo.getIdList().get(0));
            return new TotemsReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("获取策略，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "获取策略异常");
        }
    }

    @ApiOperation(value = "新增或修改", httpMethod = "POST")
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public TotemsReturnT addOrUpdate(@RequestBody PushAutoMappingIpEntity vo, Authentication auth) {
        try{
            String authName = auth.getName();
            vo.setCreateUser(authName);
            return autoMappingIpService.addOrUpdate(vo);
        }catch (Exception e) {
            logger.error("新增ip匹配数据，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "新增ip匹配数据异常");
        }
    }

}
