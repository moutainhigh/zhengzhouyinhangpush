package com.abtnetworks.totems.auto.controller;

import com.abtnetworks.totems.auto.entity.AutoRecommendConflictPolicyEntity;
import com.abtnetworks.totems.auto.service.PushAutoRecommendConflictPolicyService;
import com.abtnetworks.totems.auto.vo.AutoRecommendConflictVo;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "自动开通策略相关策略管理")
@RestController
@RequestMapping(value = "/auto/conflict/")
@Slf4j
public class PushAutoRecommendConflictController {

    @Autowired
    private PushAutoRecommendConflictPolicyService policyService;

    @ApiOperation("新增")
    @PostMapping("insert")
    public ReturnT insert(@RequestBody AutoRecommendConflictVo vo) {
        try {
            if (CollectionUtils.isEmpty(vo.getPolicyVoList())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "传入数据为空");
            }
            return policyService.batchInsert(vo.getPolicyVoList());
        } catch (Exception e) {
            log.error("新建对象异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "新建对象管理任务异常");
        }
    }

    @ApiOperation("查询")
    @PostMapping("select")
    public ReturnT<PageInfo<AutoRecommendConflictPolicyEntity>> select(@RequestBody AutoRecommendConflictVo vo) {
        if (ObjectUtils.isEmpty(vo)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "传入的数据为空");
        }
        if (vo.getAutoTaskId() == null || StringUtils.isBlank(vo.getDeviceUuid())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "传入的自动工单id或设备uuid为空");
        }
        if (StringUtils.isBlank(vo.getPolicyType())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "传入的策略类型为空");
        }
        try {
            PageInfo<AutoRecommendConflictPolicyEntity> pageInfo = policyService.getBytaskIdAndDeviceUuid(vo);
            return new ReturnT<>(pageInfo);
        } catch (Exception e) {
            log.error("查询数据异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"查询数据异常");
        }
    }
}