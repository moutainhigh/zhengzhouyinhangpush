package com.abtnetworks.totems.advanced.controller;

import com.abtnetworks.totems.advanced.dto.PushMappingNatDTO;
import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.abtnetworks.totems.advanced.service.PushMappingNatService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.vo.ResultResponseVO;
import com.alibaba.fastjson.JSONObject;
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
 * @author Administrator
 * @Title:
 * @Description: Nat自动映射关联controller
 * @date 2021/3/8
 */
@Slf4j
@Api("Nat自动映射关联controller")
@RestController
@RequestMapping(value = "/mapping-nat/")
public class PushMappingNatController extends BaseController {

    @Autowired
    PushMappingNatService pushMappingNatService;

    @ApiOperation("增加关联信息")
    @PostMapping("insert")
    public ResultResponseVO saveMappingNatInfo(@RequestBody PushMappingNatDTO pushMappingNatDTO, Authentication auth) {

        try {
            log.debug("增加关联nat信息接口START入参{}", JSONObject.toJSONString(pushMappingNatDTO));
            String userName = auth.getName();
            pushMappingNatDTO.setCreateUser(userName);
            validatorParam(pushMappingNatDTO);
            int i = pushMappingNatService.savePushMappingNatInfo(pushMappingNatDTO);
            log.debug("增加关联nat信息接口END返参{}", i);
            return returnResponseSuccess(i);
        } catch (Exception e) {

            log.error("增加关联nat信息接口异常END", e);
            return returnResponseFail("-2", "系统异常");
        }
    }

    @ApiOperation("修改关联信息")
    @PostMapping("update")
    public ResultResponseVO updateMappingNatInfo(@RequestBody PushMappingNatDTO pushMappingNatDTO,Authentication auth) {

        try {
            log.debug("修改关联nat信息接口START入参{}", JSONObject.toJSONString(pushMappingNatDTO));
            String userName = auth.getName();
            pushMappingNatDTO.setCreateUser(userName);
            validatorParam(pushMappingNatDTO);
            int i = pushMappingNatService.updatePushMappingNatInfo(pushMappingNatDTO);
            log.debug("修改关联nat信息接口END返参{}", i);
            return returnResponseSuccess(i);
        } catch (Exception e) {

            log.error("修改关联nat信息接口异常END", e);
            return returnResponseFail("-2", "系统异常");
        }
    }

    @ApiOperation("删除关联信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表,多个逗号分隔", required = true, dataType = "String")
    })
    @PostMapping("delete")
    public ResultResponseVO deleteMappingNatInfo(String ids) {

        try {
            log.debug("删除关联nat信息接口START入参{}", ids);

            int i = pushMappingNatService.deletePushMappingNatInfo(ids);
            log.debug("删除关联nat信息接口END返参{}", i);
            return returnResponseSuccess(i);
        } catch (Exception e) {

            log.error("删除关联nat信息接口异常END", e);
            return returnResponseFail("-2", "系统异常");
        }
    }

    @ApiOperation("查询nat关联信息")
    @PostMapping("list")
    public ResultResponseVO listMappingNatInfo(@RequestBody SearchPushMappingNatDTO searchPushMappingNatDTO) {

        try {
            log.debug("查询nat关联信息接口START入参{}", JSONObject.toJSONString(searchPushMappingNatDTO));

            PageInfo<PushMappingNatEntity> pageInfo = pushMappingNatService.listPushMappingNatInfo(searchPushMappingNatDTO);
            log.debug("查询nat关联信息接口END返参{}", JSONObject.toJSONString(pageInfo));
            return returnResponseSuccess(pageInfo);
        } catch (Exception e) {

            log.error("查询nat关联信息接口异常END", e);
            return returnResponseFail("-2", "系统异常");
        }
    }
}
