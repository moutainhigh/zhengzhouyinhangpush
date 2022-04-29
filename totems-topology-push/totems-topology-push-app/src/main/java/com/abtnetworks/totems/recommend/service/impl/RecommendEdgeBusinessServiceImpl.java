package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.recommend.service.RecommendEdgeBusinessService;
import com.abtnetworks.totems.recommend.vo.RecommendLabelVO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchLabelDTO;
import com.abtnetworks.totems.whale.baseapi.ro.SearchLabelResultRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleSubnetObjectClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/20
 */
@Service
public class RecommendEdgeBusinessServiceImpl implements RecommendEdgeBusinessService {

    @Resource
    WhaleSubnetObjectClient whaleSubnetObjectClient;

    @Override
    public RecommendLabelVO getLabelNames(SearchLabelDTO searchLabelDTO) throws IllegalAccessException {

        ResultRO<List<SearchLabelResultRO>> resultRO =  whaleSubnetObjectClient.searchSubnetLabel(searchLabelDTO);
        if(resultRO.getSuccess()){
            RecommendLabelVO recommendLabelVO = new RecommendLabelVO();
            recommendLabelVO.setSearchLabelResultROList(resultRO.getData());
            return recommendLabelVO;
        }else{
            throw new IllegalAccessException("获取标签异常");
        }


    }
}
