package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PolicyRecommendCredentialEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 11:05
 */
@Mapper
@Repository
public interface PolicyRecommendCredentialMapper {

    /**
     * 获取认证信息对象
     * @param uuid 认证信息对象uuid
     * @return 认证信息对象
     */
    PolicyRecommendCredentialEntity get(String uuid);
}
