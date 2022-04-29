package com.abtnetworks.totems.mapping.manager;

import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpVO;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author lifei
 * @desc ip映射管理类
 * @date 2022/2/10 18:47
 */
public interface AutoIpMappingManager {

    /**
     * 查询IP映射关系（whale）
     * @param autoMappingIpSearchVO
     * @return
     */
    List<AutoMappingIpVO> queryIpMappingFromQt(AutoMappingIpSearchVO autoMappingIpSearchVO) throws UnknownHostException;

    /**
     * 查询IP映射关系（whale）
     * @param autoMappingIpSearchVO
     * @return
     */
    List<AutoMappingIpVO> queryIpMappingFromQtAll(AutoMappingIpSearchVO autoMappingIpSearchVO);

    /**
     * 查询IP映射关系（push）
     * @param autoMappingIpSearchVO
     * @return
     */
    List<AutoMappingIpVO> queryIpMappingFromPush(AutoMappingIpSearchVO autoMappingIpSearchVO);
}
