package com.abtnetworks.totems.mapping.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.TotemsIp4Utils;
import com.abtnetworks.totems.mapping.dto.PushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.dto.SearchPushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.service.PushAutoMappingPoolService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc    地址映射池controller
 * @author liuchanghao
 * @date 2022-02-16 10:31
 */
@Slf4j
@Api(tags = "自动映射地址池controller")
@RestController
@RequestMapping(value = "/mapping/pool/")
public class PushAutoMappingPoolController extends BaseController {

    @Autowired
    PushAutoMappingPoolService pushAutoMappingPoolService;

    @ApiOperation("增加关联信息")
    @PostMapping("insert")
    public TotemsReturnT saveMappingNatInfo(@RequestBody PushAutoMappingPoolDTO pushAutoMappingPoolDTO, Authentication auth) {

        try {
            log.debug("增加关联nat信息接口START入参{}", JSONObject.toJSONString(pushAutoMappingPoolDTO));
            String userName = auth.getName();
            pushAutoMappingPoolDTO.setCreateUser(userName);
            validatorParam(pushAutoMappingPoolDTO);
            return pushAutoMappingPoolService.savePushAutoMappingPoolInfo(pushAutoMappingPoolDTO);
        } catch (Exception e) {

            log.error("增加关联nat信息接口异常END", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "系统异常");
        }
    }

    @ApiOperation("修改关联信息")
    @PostMapping("update")
    public TotemsReturnT updateMappingNatInfo(@RequestBody PushAutoMappingPoolDTO pushAutoMappingPoolDTO,Authentication auth) {

        try {
            log.debug("修改关联nat信息接口START入参{}", JSONObject.toJSONString(pushAutoMappingPoolDTO));
            String userName = auth.getName();
            pushAutoMappingPoolDTO.setCreateUser(userName);
            validatorParam(pushAutoMappingPoolDTO);
            return pushAutoMappingPoolService.updatePushAutoMappingPoolInfo(pushAutoMappingPoolDTO);
        } catch (Exception e) {
            log.error("修改关联nat信息接口异常END", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "系统异常");
        }
    }

    @ApiOperation("删除关联信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "任务id列表,多个逗号分隔", required = true, dataType = "String")
    })
    @PostMapping("delete")
    public TotemsReturnT deleteMappingNatInfo(String ids) {

        try {
            log.debug("删除关联nat信息接口START入参{}", ids);

            int i = pushAutoMappingPoolService.deletePushAutoMappingPoolInfo(ids);
            log.debug("删除关联nat信息接口END返参{}", i);
            return TotemsReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("删除关联nat信息接口异常END", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "系统异常");
        }
    }

    @ApiOperation("查询nat关联信息")
    @PostMapping("list")
    public TotemsReturnT listMappingNatInfo(@RequestBody SearchPushAutoMappingPoolDTO searchPushAutoMappingPoolDTO) {

        try {
            log.debug("查询nat关联信息接口START入参{}", JSONObject.toJSONString(searchPushAutoMappingPoolDTO));
            boolean validateResult = validateParam(searchPushAutoMappingPoolDTO);
            if (!validateResult) {
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "请求参数格式不对,请检查!");
            }
            searchPushAutoMappingPoolDTO.getPostIp();
            PageInfo<PushAutoMappingPoolEntity> pageInfo = pushAutoMappingPoolService.listPushAutoMappingPoolInfo(searchPushAutoMappingPoolDTO);
            log.debug("查询nat关联信息接口END返参{}", JSONObject.toJSONString(pageInfo));
            return new TotemsReturnT(pageInfo);
        } catch (Exception e) {
            log.error("查询nat关联信息接口异常END", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "系统异常");
        }
    }

    /**
     * 校验地址参数
     * @param searchPushAutoMappingPoolDTO
     * @return
     */
    private boolean validateParam(@RequestBody SearchPushAutoMappingPoolDTO searchPushAutoMappingPoolDTO) {
        if(StringUtils.isNotBlank(searchPushAutoMappingPoolDTO.getPreIp())){
            String[] preIps = searchPushAutoMappingPoolDTO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String itemIp : preIps){
                if(!IpUtils.isValidIp(itemIp)){
                    return false;
                }
            }
            return true;
        }
        if(StringUtils.isNotBlank(searchPushAutoMappingPoolDTO.getPostIp())){
            String[] postIps = searchPushAutoMappingPoolDTO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String itemIp : postIps){
                if(!IpUtils.isValidIp(itemIp)){
                    return false;
                }
            }
            return true;
        }
        return true;
    }
}
