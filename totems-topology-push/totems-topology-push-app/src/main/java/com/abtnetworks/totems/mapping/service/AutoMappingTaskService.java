package com.abtnetworks.totems.mapping.service;

import com.abtnetworks.totems.auto.vo.AutoRecommendTaskSearchVO;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.OrderCheckVO;
import com.github.pagehelper.PageInfo;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @desc    地址映射自动匹配接口
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
public interface AutoMappingTaskService {

    /**
     * 进行工单检测
     * @param checkVO
     * @param auth
     * @return
     * @throws Exception
     */
    TotemsReturnT check(OrderCheckVO checkVO, Authentication auth) throws Exception;

    /**
     * 分页查询列表
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushAutoMappingTaskEntity> findList(AutoRecommendTaskSearchVO vo, int pageNum, int pageSize);

    /**
     * 批量删除
     * @param idList
     * @return
     * @throws Exception
     */
    int deleteIdList(List<Integer> idList) throws Exception;

    /**
     * 根据id搜索
     * @param id
     * @return
     */
    PushAutoMappingTaskEntity selectById(int id) ;

    /**
     * 加入到仿真任务中
     * @param idList
     * @param auth
     */
    TotemsReturnT addRecommendTask(List<Integer> idList, Authentication auth);

    /**
     * 根据NatId或者路由ID查询
     * @param autoIdVO
     * @return
     */
    TotemsReturnT getById(AutoIdVO autoIdVO);


}
