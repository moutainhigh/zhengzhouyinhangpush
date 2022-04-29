package com.abtnetworks.totems.mapping.service.impl;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.TwoMemberObject;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.SearchRangeOpEnum;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.IpMatchUtil;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingPoolMapper;
import com.abtnetworks.totems.mapping.dto.PushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.dto.SearchPushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.service.PushAutoMappingPoolService;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/8
 */
@Service
public class PushAutoMappingPoolServiceImpl implements PushAutoMappingPoolService {

    @Autowired
    PushAutoMappingPoolMapper pushAutoMappingPoolMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TotemsReturnT savePushAutoMappingPoolInfo(PushAutoMappingPoolDTO PushAutoMappingPoolDTO) {
        PushAutoMappingPoolEntity PushAutoMappingPoolEntity = new PushAutoMappingPoolEntity();
        BeanUtils.copyProperties(PushAutoMappingPoolDTO, PushAutoMappingPoolEntity);
        PushAutoMappingPoolEntity.setCreateTime(new Date());
        // 验证页面填写的转换前和转换后的地址
        int rc = verifyPreIpAndPostIp(PushAutoMappingPoolEntity);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return TotemsReturnT.FAIL;
        }

        Map<String, Object> params = new HashMap<>();
        List<PushAutoMappingPoolEntity> poolList = pushAutoMappingPoolMapper.listPushAutoMappingPoolInfo(params);
        if(CollectionUtils.isNotEmpty(poolList)){
            for (PushAutoMappingPoolEntity poolEntity : poolList){
                if(IpUtils.isIntersection(poolEntity.getPreIp(), PushAutoMappingPoolDTO.getPreIp()) || IpUtils.isIntersection(poolEntity.getPostIp(), PushAutoMappingPoolDTO.getPostIp())){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"当前输入NAT规划与："+ poolEntity.getPreStandardDesc() +"数据存在交集，请确认");
                }
            }
        }

        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(PushAutoMappingPoolEntity.getDeviceUuid());
        if (null == nodeEntity) {
            return TotemsReturnT.FAIL;
        }
        // 对选择不指定域的情况前端传过来的为-1做特殊处理
        if (StringUtils.isNotBlank(PushAutoMappingPoolEntity.getSrcZone())) {
            PushAutoMappingPoolEntity
                    .setSrcZone("-1".equals(PushAutoMappingPoolEntity.getSrcZone()) ? null : PushAutoMappingPoolEntity.getSrcZone());
        }
        if (StringUtils.isNotBlank(PushAutoMappingPoolEntity.getDstZone())) {
            PushAutoMappingPoolEntity
                    .setDstZone("-1".equals(PushAutoMappingPoolEntity.getDstZone()) ? null : PushAutoMappingPoolEntity.getDstZone());
        }
        PushAutoMappingPoolEntity.setDeviceName(String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp()));
        PushAutoMappingPoolEntity.setDeviceVendorName(nodeEntity.getVendorName());

        // 更新下一个可用IP
        String nextAvailableIp = MappingUtils.getNextAvailableIp(PushAutoMappingPoolEntity.getPostIp(), null);
        PushAutoMappingPoolEntity.setNextAvailableIp(nextAvailableIp);
        int insertCount = pushAutoMappingPoolMapper.insert(PushAutoMappingPoolEntity);
        return new TotemsReturnT(insertCount);
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePushAutoMappingPoolInfo(String ids) {
        int i = pushAutoMappingPoolMapper.deletePushAutoMappingPoolInfo(ids);
        return i;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TotemsReturnT updatePushAutoMappingPoolInfo(PushAutoMappingPoolDTO PushAutoMappingPoolDTO) {
        PushAutoMappingPoolEntity pushAutoMappingPoolEntity = new PushAutoMappingPoolEntity();
        BeanUtils.copyProperties(PushAutoMappingPoolDTO, pushAutoMappingPoolEntity);
        pushAutoMappingPoolEntity.setUpdateTime(new Date());

        // 验证页面填写的转换前和转换后的地址
        int rc = verifyPreIpAndPostIp(pushAutoMappingPoolEntity);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return TotemsReturnT.FAIL;
        }

        Map<String, Object> params = new HashMap<>();
        List<PushAutoMappingPoolEntity> poolList = pushAutoMappingPoolMapper.listPushAutoMappingPoolInfo(params);
        if(CollectionUtils.isNotEmpty(poolList)){
            for (PushAutoMappingPoolEntity poolEntity : poolList){
                if(PushAutoMappingPoolDTO.getId().intValue() == poolEntity.getId()){
                    // 跳过自身比较
                    continue;
                }
                if(IpUtils.isIntersection(poolEntity.getPreIp(), PushAutoMappingPoolDTO.getPreIp()) || IpUtils.isIntersection(poolEntity.getPostIp(), PushAutoMappingPoolDTO.getPostIp())){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"当前输入NAT规划与："+ poolEntity.getPreStandardDesc() +"数据存在交集，请确认");
                }
            }
        }

        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(pushAutoMappingPoolEntity.getDeviceUuid());
        if (null == nodeEntity) {
            return TotemsReturnT.FAIL;
        }
        // 对选择不指定域的情况前端传过来的为-1做特殊处理
        if (StringUtils.isNotBlank(pushAutoMappingPoolEntity.getSrcZone())) {
            pushAutoMappingPoolEntity
                .setSrcZone("-1".equals(pushAutoMappingPoolEntity.getSrcZone()) ? null : pushAutoMappingPoolEntity.getSrcZone());
        }
        if (StringUtils.isNotBlank(pushAutoMappingPoolEntity.getDstZone())) {
            pushAutoMappingPoolEntity
                .setDstZone("-1".equals(pushAutoMappingPoolEntity.getDstZone()) ? null : pushAutoMappingPoolEntity.getDstZone());
        }

        pushAutoMappingPoolEntity.setDeviceName(String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp()));
        pushAutoMappingPoolEntity.setDeviceVendorName(nodeEntity.getVendorName());
        int updateCount = pushAutoMappingPoolMapper.updateByPrimaryKey(pushAutoMappingPoolEntity);
        return new TotemsReturnT(updateCount);
    }

    @Override
    public int updateNextAvailableIp(PushAutoMappingPoolDTO PushAutoMappingPoolDTO) {
        PushAutoMappingPoolEntity pushAutoMappingPoolEntity = new PushAutoMappingPoolEntity();
        BeanUtils.copyProperties(PushAutoMappingPoolDTO, pushAutoMappingPoolEntity);
        pushAutoMappingPoolEntity.setUpdateTime(new Date());
        return pushAutoMappingPoolMapper.updateByEntity(pushAutoMappingPoolEntity);
    }

    @Override
    public PageInfo<PushAutoMappingPoolEntity> listPushAutoMappingPoolInfo(SearchPushAutoMappingPoolDTO searchPushAutoMappingPoolDTO) {
        PageHelper.startPage(searchPushAutoMappingPoolDTO.getCurrentPage(), searchPushAutoMappingPoolDTO.getPageSize());
        Map<String,Object> paramMap = new HashMap<>();
        if(StringUtils.isNotBlank(searchPushAutoMappingPoolDTO.getDeviceName())){
            paramMap.put("deviceName",searchPushAutoMappingPoolDTO.getDeviceName());
        }
        List<PushAutoMappingPoolEntity> pushAutoMappingPoolEntities = pushAutoMappingPoolMapper.listPushAutoMappingPoolInfo(paramMap);
        PageInfo<PushAutoMappingPoolEntity> pushAutoMappingPoolEntityPageInfo = new PageInfo<>(pushAutoMappingPoolEntities);

        List<PushAutoMappingPoolEntity> returnMappingList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(pushAutoMappingPoolEntities)){

            for (PushAutoMappingPoolEntity pushAutoMappingPoolEntity : pushAutoMappingPoolEntities) {
                if (StringUtils.isNotBlank(searchPushAutoMappingPoolDTO.getPreIp())) {
                    List<TwoMemberObject<Long, Long>> searchSrcIpList =
                        getListMemberByIp(searchPushAutoMappingPoolDTO.getPreIp());
                    List<TwoMemberObject<Long, Long>> preSrcIpList = getListMemberByIp(pushAutoMappingPoolEntity.getPreIp());

                    boolean srcOpMatch =
                        IpMatchUtil.rangeOpMatch(preSrcIpList, searchSrcIpList, SearchRangeOpEnum.CONTAINED_BY);
                    // 如果搜索条件填写了转换前的地址 而且不在范围内 则去匹配下一条数据
                    if (!srcOpMatch) {
                        continue;
                    }
                }
                if (StringUtils.isNotBlank(searchPushAutoMappingPoolDTO.getPostIp())) {
                    List<TwoMemberObject<Long, Long>> searchDstIpList =
                        getListMemberByIp(searchPushAutoMappingPoolDTO.getPostIp());
                    List<TwoMemberObject<Long, Long>> preDstIpList =
                        getListMemberByIp(pushAutoMappingPoolEntity.getPostIp());

                    boolean dstOpMatch =
                        IpMatchUtil.rangeOpMatch(preDstIpList, searchDstIpList, SearchRangeOpEnum.CONTAINED_BY);
                    // 如果搜索条件填写了转换后的地址 而且不在范围内 则去匹配下一条数据
                    if (!dstOpMatch) {
                        continue;
                    }
                }
                if (StringUtils.isNotBlank(pushAutoMappingPoolEntity.getDeviceUuid())) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(pushAutoMappingPoolEntity.getDeviceUuid());
                    String deviceName;
                    if (nodeEntity != null) {
                        deviceName = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }else{
                        deviceName = String.format("未知设备(%s)", pushAutoMappingPoolEntity.getDeviceUuid());
                    }
                    pushAutoMappingPoolEntity.setDeviceName(deviceName);
                }
                returnMappingList.add(pushAutoMappingPoolEntity);
            }
        }
        PageInfo<PushAutoMappingPoolEntity> pageInfo = new PageInfo<>(returnMappingList);
        pageInfo.setTotal(pushAutoMappingPoolEntityPageInfo.getTotal());
        pageInfo.setStartRow(pushAutoMappingPoolEntityPageInfo.getStartRow());
        pageInfo.setEndRow(pushAutoMappingPoolEntityPageInfo.getEndRow());
        pageInfo.setPageSize(pushAutoMappingPoolEntityPageInfo.getPageSize());
        pageInfo.setPageNum(pushAutoMappingPoolEntityPageInfo.getPageNum());
        pageInfo.setNavigatepageNums(pushAutoMappingPoolEntityPageInfo.getNavigatepageNums());
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
     * @param pushAutoMappingPoolEntity
     * @return
     */
    private int verifyPreIpAndPostIp(PushAutoMappingPoolEntity pushAutoMappingPoolEntity) {
        // 转换前数据校验
        int rc = InputValueUtils.checkIp(pushAutoMappingPoolEntity.getPreIp());
        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE
                && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
            return rc;
        }
        // 若出IP范围起始地址大于终止地址错误，则自动纠正
        if (rc == ReturnCode.INVALID_IP_RANGE) {
            pushAutoMappingPoolEntity.setPreIp(InputValueUtils.autoCorrect(pushAutoMappingPoolEntity.getPreIp()));
        }

        // 转换后数据校验
        rc = InputValueUtils.checkIp(pushAutoMappingPoolEntity.getPostIp());
        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE
                && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
            return rc;
        }
        // 若出IP范围起始地址大于终止地址错误，则自动纠正
        if (rc == ReturnCode.INVALID_IP_RANGE) {
            pushAutoMappingPoolEntity.setPostIp(InputValueUtils.autoCorrect(pushAutoMappingPoolEntity.getPostIp()));
        }
        return ReturnCode.POLICY_MSG_OK;
    }
}
