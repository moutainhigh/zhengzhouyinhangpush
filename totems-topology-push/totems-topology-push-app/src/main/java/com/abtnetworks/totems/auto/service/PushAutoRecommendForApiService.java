package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.vo.AutoRecommendTaskApiVO;
import com.abtnetworks.totems.common.signature.ReturnT;

import java.util.List;

/**
 * @desc    自动开通工单对接接口
 * @author liuchanghao
 * @date 2022-01-11 14:03
 */
public interface PushAutoRecommendForApiService {

    /**
     * 添加自动开通工单任务
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT<List<String>> createTask(AutoRecommendTaskApiVO vo) throws Exception;

}
