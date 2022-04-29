package com.abtnetworks.totems.advanced.service.impl;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abtnetworks.totems.advanced.dao.mysql.PushMappingNatMapper;
import com.abtnetworks.totems.advanced.dto.PushMappingNatDTO;
import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.abtnetworks.totems.advanced.service.PushMappingNatService;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.TwoMemberObject;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.SearchRangeOpEnum;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.IpMatchUtil;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/8
 */
@Service
public class PushMappingNatServiceImpl implements PushMappingNatService {

    @Autowired
    PushMappingNatMapper pushMappingNatMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int savePushMappingNatInfo(PushMappingNatDTO pushMappingNatDTO) {
        PushMappingNatEntity pushMappingNatEntity = new PushMappingNatEntity();
        BeanUtils.copyProperties(pushMappingNatDTO, pushMappingNatEntity);
        pushMappingNatEntity.setCreateTime(new Date());
        // 验证页面填写的转换前和转换后的地址
        int rc = verifyPreIpAndPostIp(pushMappingNatEntity);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return rc;
        }

        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(pushMappingNatEntity.getDeviceUuid());
        if (null == nodeEntity) {
            return 0;
        }
        // 对选择不指定域的情况前端传过来的为-1做特殊处理
        if (StringUtils.isNotBlank(pushMappingNatEntity.getSrcZone())) {
            pushMappingNatEntity
                    .setSrcZone("-1".equals(pushMappingNatEntity.getSrcZone()) ? null : pushMappingNatEntity.getSrcZone());
        }
        if (StringUtils.isNotBlank(pushMappingNatEntity.getDstZone())) {
            pushMappingNatEntity
                    .setDstZone("-1".equals(pushMappingNatEntity.getDstZone()) ? null : pushMappingNatEntity.getDstZone());
        }
        pushMappingNatEntity.setDeviceName(String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp()));
        pushMappingNatEntity.setDeviceVendorName(nodeEntity.getVendorName());
        int insertCount = pushMappingNatMapper.insert(pushMappingNatEntity);
        return insertCount;
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePushMappingNatInfo(String ids) {
        int i = pushMappingNatMapper.deletePushMappingNatInfo(ids);
        return i;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updatePushMappingNatInfo(PushMappingNatDTO pushMappingNatDTO) {
        PushMappingNatEntity pushMappingNatEntity = new PushMappingNatEntity();
        BeanUtils.copyProperties(pushMappingNatDTO, pushMappingNatEntity);
        pushMappingNatEntity.setUpdateTime(new Date());
        // 验证页面填写的转换前和转换后的地址
        int rc = verifyPreIpAndPostIp(pushMappingNatEntity);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return rc;
        }

        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(pushMappingNatEntity.getDeviceUuid());
        if (null == nodeEntity) {
            return 0;
        }
        // 对选择不指定域的情况前端传过来的为-1做特殊处理
        if (StringUtils.isNotBlank(pushMappingNatEntity.getSrcZone())) {
            pushMappingNatEntity
                .setSrcZone("-1".equals(pushMappingNatEntity.getSrcZone()) ? null : pushMappingNatEntity.getSrcZone());
        }
        if (StringUtils.isNotBlank(pushMappingNatEntity.getDstZone())) {
            pushMappingNatEntity
                .setDstZone("-1".equals(pushMappingNatEntity.getDstZone()) ? null : pushMappingNatEntity.getDstZone());
        }

        pushMappingNatEntity.setDeviceName(String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp()));
        pushMappingNatEntity.setDeviceVendorName(nodeEntity.getVendorName());
        int updateCount = pushMappingNatMapper.updateByPrimaryKey(pushMappingNatEntity);
        return updateCount;
    }


    @Override
    public PageInfo<PushMappingNatEntity> listPushMappingNatInfo(SearchPushMappingNatDTO searchPushMappingNatDTO) {
        PageHelper.startPage(searchPushMappingNatDTO.getCurrentPage(), searchPushMappingNatDTO.getPageSize());
        Map<String,Object> paramMap = new HashMap<>();
        if(StringUtils.isNotBlank(searchPushMappingNatDTO.getDeviceName())){
            paramMap.put("deviceName",searchPushMappingNatDTO.getDeviceName());
        }
        List<PushMappingNatEntity> pushMappingNatEntities = pushMappingNatMapper.listPushMappingNatInfo(paramMap);
        PageInfo<PushMappingNatEntity> pushMappingNatEntityPageInfo = new PageInfo<>(pushMappingNatEntities);

        List<PushMappingNatEntity> returnMappingList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(pushMappingNatEntities)){

            for (PushMappingNatEntity pushMappingNatEntity : pushMappingNatEntities) {
                if (StringUtils.isNotBlank(searchPushMappingNatDTO.getPreIp())) {
                    List<TwoMemberObject<Long, Long>> searchSrcIpList =
                        getListMemberByIp(searchPushMappingNatDTO.getPreIp());
                    List<TwoMemberObject<Long, Long>> preSrcIpList = getListMemberByIp(pushMappingNatEntity.getPreIp());

                    boolean srcOpMatch =
                        IpMatchUtil.rangeOpMatch(preSrcIpList, searchSrcIpList, SearchRangeOpEnum.CONTAINED_BY);
                    // 如果搜索条件填写了转换前的地址 而且不在范围内 则去匹配下一条数据
                    if (!srcOpMatch) {
                        continue;
                    }
                }
                if (StringUtils.isNotBlank(searchPushMappingNatDTO.getPostIp())) {
                    List<TwoMemberObject<Long, Long>> searchDstIpList =
                        getListMemberByIp(searchPushMappingNatDTO.getPostIp());
                    List<TwoMemberObject<Long, Long>> preDstIpList =
                        getListMemberByIp(pushMappingNatEntity.getPostIp());

                    boolean dstOpMatch =
                        IpMatchUtil.rangeOpMatch(preDstIpList, searchDstIpList, SearchRangeOpEnum.CONTAINED_BY);
                    // 如果搜索条件填写了转换后的地址 而且不在范围内 则去匹配下一条数据
                    if (!dstOpMatch) {
                        continue;
                    }
                }
                if (StringUtils.isNotBlank(pushMappingNatEntity.getDeviceUuid())) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(pushMappingNatEntity.getDeviceUuid());
                    String deviceName;
                    if (nodeEntity != null) {
                        deviceName = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }else{
                        deviceName = String.format("未知设备(%s)", pushMappingNatEntity.getDeviceUuid());
                    }
                    pushMappingNatEntity.setDeviceName(deviceName);
                }
                returnMappingList.add(pushMappingNatEntity);
            }
        }
        PageInfo<PushMappingNatEntity> pageInfo = new PageInfo<>(returnMappingList);
        pageInfo.setTotal(pushMappingNatEntityPageInfo.getTotal());
        pageInfo.setStartRow(pushMappingNatEntityPageInfo.getStartRow());
        pageInfo.setEndRow(pushMappingNatEntityPageInfo.getEndRow());
        pageInfo.setPageSize(pushMappingNatEntityPageInfo.getPageSize());
        pageInfo.setPageNum(pushMappingNatEntityPageInfo.getPageNum());
        pageInfo.setNavigatepageNums(pushMappingNatEntityPageInfo.getNavigatepageNums());
        return pageInfo;
    }

    /**
     * 对ip地址进行格式转换
     * @param ip
     * @return
     */
    private List<TwoMemberObject<Long,Long>> getListMemberByIp(String ip){
        List<TwoMemberObject<Long,Long>> infoList = new ArrayList<>();
        if(StringUtils.isNotBlank(ip)){
            //源转后nat
            String[] ips = ip.split(",");
            for (String  address : ips) {
                TwoMemberObject<Long, Long>  twoMemberObject = IpMatchUtil.commonConditionList(address);
                infoList.add(twoMemberObject);
            }
        }
        return infoList;
    }

    /**
     *  验证转换前ip和转换后ip
     * @param pushMappingNatEntity
     * @return
     */
    private int verifyPreIpAndPostIp(PushMappingNatEntity pushMappingNatEntity) {
        // 转换前数据校验
        int rc = InputValueUtils.checkIp(pushMappingNatEntity.getPreIp());
        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE
                && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
            return rc;
        }
        // 若出IP范围起始地址大于终止地址错误，则自动纠正
        if (rc == ReturnCode.INVALID_IP_RANGE) {
            pushMappingNatEntity.setPreIp(InputValueUtils.autoCorrect(pushMappingNatEntity.getPreIp()));
        }

        // 转换后数据校验
        rc = InputValueUtils.checkIp(pushMappingNatEntity.getPostIp());
        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE
                && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
            return rc;
        }
        // 若出IP范围起始地址大于终止地址错误，则自动纠正
        if (rc == ReturnCode.INVALID_IP_RANGE) {
            pushMappingNatEntity.setPostIp(InputValueUtils.autoCorrect(pushMappingNatEntity.getPostIp()));
        }
        return ReturnCode.POLICY_MSG_OK;
    }
}
