package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigDetailEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigSearchVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @desc    防护网段配置接口
 * @author liuchanghao
 * @date 2021-06-10 14:03
 */
public interface PushProtectNetworkConfigService {

    /**
     * 新增或编辑防护配置
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT addOrUpdateConfig(ProtectNetworkConfigVO vo) throws Exception;

    /**
     * 删除防护配置
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT delete(ProtectNetworkConfigVO vo) throws Exception;

    /**
     * 分页查询
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<ProtectNetworkConfigEntity> findList(ProtectNetworkConfigSearchVO vo, int pageNum, int pageSize);

    /**
     * 根据IP查询防护网段数据
     * @param ip
     * @param vo
     * @return
     */
    List<ProtectNetworkConfigEntity> findByIp(String ip, AutoRecommendTaskVO vo);

    /**
     * 查询导出excelList
     * @param vo
     * @return
     */
    List<ProtectNetworkConfigEntity> findExcelList(ProtectNetworkConfigSearchVO vo);

}
