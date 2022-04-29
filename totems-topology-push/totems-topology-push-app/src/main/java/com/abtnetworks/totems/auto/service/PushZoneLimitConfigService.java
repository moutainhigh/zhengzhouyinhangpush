package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.entity.PushZoneLimitConfigEntity;
import com.abtnetworks.totems.auto.vo.PushZoneLimitConfigVO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;

/**
 * @desc    高级设置—生成策略域限制配置接口
 * @author liuchanghao
 * @date 2021-11-16 14:03
 */
public interface PushZoneLimitConfigService {

    /**
     * 添加或修改高级设置—生成策略域限制配置
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT addOrUpdate(PushZoneLimitConfigVO vo) throws Exception;

    /**
     * 添加自动开通工单任务
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT delete(PushZoneLimitConfigVO vo) throws Exception;

    /**
     * 分页查询
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushZoneLimitConfigEntity> findList(PushZoneLimitConfigVO vo, int pageNum, int pageSize);


}
