package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.dto.PushRecommendTaskExpandDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.github.pagehelper.PageInfo;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/**
* F5策略生成表服务service
*
* @author Administrator
* @since 2021年08月02日
*/
public interface PushTaskFiveBalanceService {

    /**
     * 单个新增
     * 
     * @param dto
     * @return
     */
    int createPushTaskFiveBalance(PushRecommendTaskExpandDTO dto) ;


    /**
     * 批量删除
     * 
     * @param ids
     * @return
     */
    int deletePushTaskFiveBalance(String ids) ;


    /**
     * 单个更新
     * 
     * @param dto
     * @return
     */
    int updatePushTaskFiveBalance(PushRecommendTaskExpandDTO dto) ;


    /**
     * 单个查询
     * 
     * @param dto
     * @return
     */
    PushRecommendTaskExpandDTO getPushTaskFiveBalance(PushRecommendTaskExpandDTO dto) ;

    /**
     * 分页查询
     * 
     * @param dto
     * @return
     */
    PageInfo<PushRecommendTaskExpandDTO> findPushTaskFiveBalancePage(PushRecommendTaskExpandDTO dto) ;


    /**
     * 批量导入F5策略生成数据
     * @param file
     * @param auth
     * @return
     */
    String batchImportFivePolicy(MultipartFile file, Authentication auth);


}
