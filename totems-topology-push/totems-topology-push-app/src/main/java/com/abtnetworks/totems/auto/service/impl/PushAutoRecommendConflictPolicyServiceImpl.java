package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendConflictPolicyMapper;
import com.abtnetworks.totems.auto.entity.AutoRecommendConflictPolicyEntity;
import com.abtnetworks.totems.auto.service.PushAutoRecommendConflictPolicyService;
import com.abtnetworks.totems.auto.vo.AutoRecommendConflictPolicyVo;
import com.abtnetworks.totems.auto.vo.AutoRecommendConflictVo;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PushAutoRecommendConflictPolicyServiceImpl implements PushAutoRecommendConflictPolicyService {

    @Resource
    private AutoRecommendConflictPolicyMapper policyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT batchInsert(List<AutoRecommendConflictPolicyVo> policyVoList) {
        List<AutoRecommendConflictPolicyEntity> policyEntityList = new ArrayList<>();
        for (AutoRecommendConflictPolicyVo policyVo : policyVoList) {
            ReturnT stringReturnT = parameterValidate(policyVo);
            if (stringReturnT != null) {
                return stringReturnT;
            }
            AutoRecommendConflictPolicyEntity policyEntity = new AutoRecommendConflictPolicyEntity();
            BeanUtils.copyProperties(policyVo, policyEntity);
            policyEntityList.add(policyEntity);
        }
        List<List<AutoRecommendConflictPolicyEntity>> partitionLists = Lists.partition(policyEntityList, 500);
        int insertStatus = 0;
        for (List<AutoRecommendConflictPolicyEntity> partitionList : partitionLists) {
            insertStatus = policyMapper.batchInsert(partitionList);
        }
        return insertStatus > 0 ? new ReturnT(ReturnT.SUCCESS_CODE, "添加成功") : new ReturnT(ReturnT.FAIL_CODE, "添加失败");
    }

    @Override
    public PageInfo<AutoRecommendConflictPolicyEntity> getBytaskIdAndDeviceUuid(AutoRecommendConflictVo vo) {
        PageHelper.startPage(vo.getPage(), vo.getLimit());

        List<AutoRecommendConflictPolicyEntity> list = policyMapper.queryTaskIdAndUuid(vo.getAutoTaskId(), vo.getDeviceUuid(), vo.getPolicyType());
        return new PageInfo<>(list);

    }


    /**
     * 参数校验
     * @param vo    参数实体
     * @return      检验结果
     */
    private ReturnT parameterValidate(AutoRecommendConflictPolicyVo vo) {
        if (ObjectUtils.isEmpty(vo)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "传入数据为空");
        }
        if (StringUtils.isBlank(vo.getTheme())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "主题不能为空");
        }
        if (vo.getAutoTaskId() == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "自动开通ID不能为空");
        }
        if (StringUtils.isBlank(vo.getDeviceUuid())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "设备UUID不能为空");
        }
        return null;
    }
}