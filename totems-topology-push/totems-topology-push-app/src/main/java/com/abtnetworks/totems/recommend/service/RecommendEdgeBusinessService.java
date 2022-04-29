package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.vo.RecommendLabelVO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchLabelDTO;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/20
 */
public interface RecommendEdgeBusinessService {
    /***
     * 参数标签搜索值
     * @param searchLabelDTO
     * @throws IllegalAccessException
     * @return
     */
    RecommendLabelVO  getLabelNames(SearchLabelDTO searchLabelDTO) throws IllegalAccessException;
}
