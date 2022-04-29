package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigDetailMapper;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigMapper;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkNatMappingMapper;
import com.abtnetworks.totems.auto.dto.AutoRecommendNatMappingDTO;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigDetailEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkNatMappingEntity;
import com.abtnetworks.totems.auto.enums.PushAccessTypeEnum;
import com.abtnetworks.totems.auto.enums.PushNatFlagEnum;
import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.auto.service.PushProtectNetworkConfigService;
import com.abtnetworks.totems.auto.utils.IpAddress;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigSearchVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @desc    防护网段配置实现类
 * @author liuchanghao
 * @date 2021-06-09 14:05
 */
@Service
public class PushProtectNetworkConfigServiceImpl implements PushProtectNetworkConfigService {

    private static Logger logger = LoggerFactory.getLogger(PushProtectNetworkConfigServiceImpl.class);

    @Autowired
    private ProtectNetworkConfigMapper protectNetworkConfigMapper;

    @Autowired
    private ProtectNetworkConfigDetailMapper protectNetworkConfigDetailMapper;

    @Autowired
    private ProtectNetworkNatMappingMapper protectNetworkNatMappingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT addOrUpdateConfig(ProtectNetworkConfigVO vo) throws Exception {
        try{
            if(ObjectUtils.isEmpty(vo.getId())){
                // 新增
                // 判断防火墙是否已存在
                ProtectNetworkConfigEntity existConfigEntity = protectNetworkConfigMapper.selectByDeviceUuid(vo.getDeviceUuid());
                if(null != existConfigEntity ){
                    return new ReturnT(ReturnT.FAIL_CODE, "防护防火墙已存在");
                }
                ProtectNetworkConfigEntity record = new ProtectNetworkConfigEntity();
                BeanUtils.copyProperties(vo, record);
                record.setUuid(IdGen.uuid());
                record.setSameZoneFlag(PushNatFlagEnum.NAT_FLAG_N.getCode());
                protectNetworkConfigMapper.insert(record);
                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.getByUuid(record.getUuid());
                // 添加防护网段配置Nat映射关系
                if (CollectionUtils.isNotEmpty(vo.getNatMappingDTOList()) && PushNatFlagEnum.NAT_FLAG_Y.getCode().equalsIgnoreCase(vo.getNatFlag())) {
                    for (AutoRecommendNatMappingDTO natMappingDTO : vo.getNatMappingDTOList()) {
                        ProtectNetworkNatMappingEntity natMappingEntity = new ProtectNetworkNatMappingEntity();
                        natMappingEntity.setConfigId(configEntity.getId());
                        BeanUtils.copyProperties(natMappingDTO, natMappingEntity);
                        protectNetworkNatMappingMapper.insert(natMappingEntity);
                    }
                }
                this.converntIP2StartAndEnd(configEntity.getProtectNetwork(), configEntity.getId());
            } else {
                ProtectNetworkConfigEntity oldEntity = protectNetworkConfigMapper.selectByPrimaryKey(vo.getId());
                // 判断防火墙是否已存在
                List<ProtectNetworkConfigEntity> allList = protectNetworkConfigMapper.findAll();
                for (ProtectNetworkConfigEntity networkConfigEntity : allList ){
                    if(StringUtils.equalsAnyIgnoreCase(networkConfigEntity.getDeviceUuid(), vo.getDeviceUuid()) &&
                            !StringUtils.equalsAnyIgnoreCase(vo.getDeviceUuid(), oldEntity.getDeviceUuid())) {
                        return new ReturnT(ReturnT.FAIL_CODE, "防护防火墙已存在");
                    }
                }

                // 如果防护网段有调整，需同步调整配置明细中的数据,先删除，再新增
                if(!StringUtils.equalsAnyIgnoreCase(vo.getProtectNetwork(), oldEntity.getProtectNetwork())){
                    protectNetworkConfigDetailMapper.deleteByConfigId(oldEntity.getId());
                    this.converntIP2StartAndEnd(vo.getProtectNetwork(), oldEntity.getId());
                }
                // 如果防护网段Nat映射有调整，需同步调整Nat映射中的数据,先删除，再新增
                protectNetworkNatMappingMapper.deleteByConfigId(oldEntity.getId());
                for (AutoRecommendNatMappingDTO natMappingDTO : vo.getNatMappingDTOList()) {
                    ProtectNetworkNatMappingEntity natMappingEntity = new ProtectNetworkNatMappingEntity();
                    BeanUtils.copyProperties(natMappingDTO, natMappingEntity);
                    natMappingEntity.setConfigId(oldEntity.getId());
                    protectNetworkNatMappingMapper.insert(natMappingEntity);
                }

                ProtectNetworkConfigEntity record = new ProtectNetworkConfigEntity();
                BeanUtils.copyProperties(vo, record);
                protectNetworkConfigMapper.updateByPrimaryKey(record);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            logger.error("新增或编辑防护网段配置异常，异常原因：", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT delete(ProtectNetworkConfigVO vo) throws Exception {
        try{
            // 删除之前先删除明细表中的数据
            for (Long id : vo.getIdList()) {
                protectNetworkConfigDetailMapper.deleteByConfigId(id);
                protectNetworkConfigMapper.deleteByPrimaryKey(id);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            logger.error("删除防护网段配置异常，异常原因：", e);
            throw e;
        }
    }

    @Override
    public PageInfo<ProtectNetworkConfigEntity> findList(ProtectNetworkConfigSearchVO vo, int pageNum, int pageSize) {
        List<ProtectNetworkConfigEntity> list = this.getList(vo, false);

        int psize = pageSize;
        int page = pageNum;

        //手动分页的分割起始下标
        Integer fromIndex = 0;
        //手动分页的分割结尾下标
        Integer toIndex = 0;
        Integer total = list.size();
        if(total/psize == 0 && total%psize > 0){
            fromIndex = 0;
            toIndex = total;
        }else{
            if(total/psize >= 1 && total % psize >= 0){
                fromIndex = psize * (page-1);
                if(psize * page >= total){
                    toIndex = total;
                }else{
                    toIndex = psize * page;
                }
            }
        }

        //开启分页
        PageHelper.startPage(page, psize);
        List<ProtectNetworkConfigEntity> list1 = list.subList(fromIndex, toIndex);
        PageInfo<ProtectNetworkConfigEntity> pageInfo = new PageInfo<>(list1);
        pageInfo.setTotal(total);
        //手动清理ThreadLocal存储的分页参,防止分页失效,参数乱套的情况
        PageHelper.clearPage();

        return pageInfo;
    }

    @Override
    public List<ProtectNetworkConfigEntity> findExcelList(ProtectNetworkConfigSearchVO vo) {
        List<ProtectNetworkConfigEntity> list = this.getList(vo, false);
        return list;
    }

    /**
     * 根据IP查询防护网段数据
     * 相交算法
     * @param vo
     * @param dealIp 是否处理IP
     * @return
     */
    private List<ProtectNetworkConfigEntity> getList(ProtectNetworkConfigSearchVO vo, boolean dealIp){
        ProtectNetworkConfigEntity detailEntity = new ProtectNetworkConfigEntity();
        Long ipv4Start = 0L;
        Long ipv4End = 0L;
        String ipv6Start ="";
        String ipv6End ="";

        if (StringUtils.isNotEmpty(vo.getIp())) {
            if (IpUtils.isIP(vo.getIp())) {
                detailEntity.setIpType(IpTypeEnum.IPV4.getCode());
                ipv4Start = IpAddress.ipToLong(vo.getIp());
                ipv4End = IpAddress.ipToLong(vo.getIp());
                detailEntity.setIpv4Start(ipv4Start);
                detailEntity.setIpv4End(ipv4End);
            } else if (IpUtils.isIPRange(vo.getIp()) || IpUtils.isIPSegment(vo.getIp())) {
                detailEntity.setIpType(IpTypeEnum.IPV4.getCode());
                long[] ipStartEnd = IpAddress.getIpStartEnd(vo.getIp());
                ipv4Start = ipStartEnd[0];
                ipv4End = ipStartEnd[1];
                detailEntity.setIpv4Start(ipv4Start);
                detailEntity.setIpv4End(ipv4End);
            } else {
                Pair<String, String> ip6StartEnd = IpAddress.getIp6StartEndString(vo.getIp());
                detailEntity.setIpType(IpTypeEnum.IPV6.getCode());
                ipv6Start = ip6StartEnd.getLeft();
                ipv6End = ip6StartEnd.getRight();
                detailEntity.setIpv6Start(ipv6Start);
                detailEntity.setIpv6End(ipv6End);
            }
        }

        if(StringUtils.isNotBlank(vo.getDeviceIp())){
            detailEntity.setDeviceIp(vo.getDeviceIp());
        }
        if(StringUtils.isNotBlank(vo.getDeviceName())){
            detailEntity.setDeviceName(vo.getDeviceName());
        }
        if(StringUtils.isNotBlank(vo.getNatType()) && (vo.getNatType().equals(PushNatTypeEnum.NAT_TYPE_D.getCode()) ||
                vo.getNatType().equals(PushNatTypeEnum.NAT_TYPE_S.getCode()))){
            detailEntity.setNatType(vo.getNatType());
        }

        if(StringUtils.isNotBlank(vo.getPreIp())){
            detailEntity.setOutsideIp(vo.getPreIp());
        }

        if(StringUtils.isNotBlank(vo.getPostIp())){
            detailEntity.setInsideIp(vo.getPostIp());
        }

        List<ProtectNetworkConfigEntity> list = protectNetworkConfigMapper.findList(detailEntity);
        if(CollectionUtils.isNotEmpty(list)){
            for (ProtectNetworkConfigEntity networkConfigEntity : list){
                List<String> ipSegmentList = new ArrayList<>();
                List<String> convertRangeIpList = new ArrayList<>();
                ProtectNetworkNatMappingEntity record = new ProtectNetworkNatMappingEntity();
                BeanUtils.copyProperties(detailEntity, record);
                record.setConfigId(networkConfigEntity.getId());
                List<ProtectNetworkNatMappingEntity> natMappingEntityList = protectNetworkNatMappingMapper.selectConfigList(record);
                //List<ProtectNetworkNatMappingEntity> natMappingEntityList = protectNetworkNatMappingMapper.selectByConfigId(networkConfigEntity.getId());
                networkConfigEntity.setNatMappingDTOList(natMappingEntityList);

                // 这里是处理新建自动开通工单时，查询交集数据的操作
                if(dealIp){
                    List<ProtectNetworkConfigDetailEntity> configDetailList = protectNetworkConfigDetailMapper.findByConfigId(networkConfigEntity.getId());
                    if(CollectionUtils.isNotEmpty(configDetailList)){
                        for (ProtectNetworkConfigDetailEntity configDetailEntity : configDetailList ) {
                            // 取交集并转换为范围IP
                            String ip = this.compareAndConvert2IP(ipv4Start, ipv4End, ipv6Start, ipv6End, configDetailEntity);
                            if(StringUtils.isBlank(ip)){
                                continue;
                            }
                            // 设置交集范围IP，用于查Nat映射关系和域以及接口信息
                            if(StringUtils.isNotBlank(ip)){
                                String[] ipStartEnd = ip.split("-");
                                if(StringUtils.equals(ipStartEnd[0], ipStartEnd[1])){
                                    if(IpAddress.isSameIp(ipStartEnd[0], vo.getIp(), IpTypeEnum.IPV4.getCode())){
                                        convertRangeIpList.add(vo.getIp());
                                    } else {
                                        convertRangeIpList.add(ipStartEnd[0]);
                                    }
                                } else {
                                    if(IpAddress.isSameIp(ip, vo.getIp(), IpTypeEnum.IPV4.getCode())){
                                        convertRangeIpList.add(vo.getIp());
                                    } else {
                                        convertRangeIpList.add(ip);
                                    }
                                }

                                // 将IP范围转换为子网
                                List<String> currentList = IPUtil.convertRangeToSubnet(ip);
                                if(CollectionUtils.isNotEmpty(currentList)){
                                    ipSegmentList.addAll(currentList);
                                }
                            }

                        }
                    }
                    // 将所有子网以英文逗号分隔，当成当前的IP
                    if(CollectionUtils.isNotEmpty(ipSegmentList)){
                        networkConfigEntity.setConvertIp(String.join(",", ipSegmentList));
                    } else {
                        networkConfigEntity.setConvertIp(vo.getIp());
                    }
                    if(CollectionUtils.isNotEmpty(convertRangeIpList)){
                        networkConfigEntity.setConvertRangeIp(String.join(",", convertRangeIpList));
                    } else {
                        networkConfigEntity.setConvertRangeIp(vo.getIp());
                    }
                }
            }
            if (StringUtils.isNotBlank(vo.getNatType()) && !vo.getNatType().equals(PushNatTypeEnum.NAT_TYPE_D.getCode()) &&
                    !vo.getNatType().equals(PushNatTypeEnum.NAT_TYPE_S.getCode())){
                List<ProtectNetworkConfigEntity> lists = new ArrayList<>();
                list.forEach(p -> {
                    if (CollectionUtils.isNotEmpty(p.getNatMappingDTOList())){
                        List<ProtectNetworkNatMappingEntity> natMappingDTOList = p.getNatMappingDTOList();
                        List<String> strings = natMappingDTOList.stream().map(p1 -> p1.getNatType()).distinct().collect(Collectors.toList());
                        if (strings.contains(PushNatTypeEnum.NAT_TYPE_D.getCode()) && strings.contains(PushNatTypeEnum.NAT_TYPE_S.getCode())){
                            lists.add(p);
                        }
                    }
                });
                list.clear();
                list = lists;
            }
        }
        return list;
    }



    @Override
    public List<ProtectNetworkConfigEntity> findByIp(String ip, AutoRecommendTaskVO taskVo) {
        ProtectNetworkConfigSearchVO vo = new ProtectNetworkConfigSearchVO();
        vo.setIp(ip);
        return this.getList(vo, true);
    }


    /**
     * 将IP拆分为开始数据和结束数据存库，方便对比
     * @param protectNetwork
     * @param configId
     * @return
     */
    private void converntIP2StartAndEnd(String protectNetwork, Long configId){
        if(StringUtils.isBlank(protectNetwork) || ObjectUtils.isEmpty(configId)){
            return;
        }
        String[] ips = protectNetwork.split(",");
        for (String ip :ips ) {
            ProtectNetworkConfigDetailEntity detailEntity = new ProtectNetworkConfigDetailEntity();
            detailEntity.setConfigId(configId);
            detailEntity.setCreateTime(new Date());
            // 如果为IPv4单Ip/子网/范围
            if (IpUtils.isIP(ip)) {
                detailEntity.setIpType(IpTypeEnum.IPV4.getCode());
                detailEntity.setIpv4Start(IpAddress.ipToLong(ip));
                detailEntity.setIpv4End(IpAddress.ipToLong(ip));
            } else if (IpUtils.isIPRange(ip) || IpUtils.isIPSegment(ip)) {
                detailEntity.setIpType(IpTypeEnum.IPV4.getCode());
                long[] ipStartEnd = IpAddress.getIpStartEnd(ip);
                detailEntity.setIpv4Start(ipStartEnd[0]);
                detailEntity.setIpv4End(ipStartEnd[1]);
            } else {
                Pair<String, String> ip6StartEnd = IpAddress.getIp6StartEndString(ip);
                detailEntity.setIpType(IpTypeEnum.IPV6.getCode());
                detailEntity.setIpv6Start(String.valueOf(ip6StartEnd.getLeft()));
                detailEntity.setIpv6End(String.valueOf(ip6StartEnd.getRight()));
            }
            protectNetworkConfigDetailMapper.insert(detailEntity);
        }

    }

    /**
     * 取交集，并转换为具体IP
     * @param ipv4Start
     * @param ipv4End
     * @param ipv6Start
     * @param ipv6End
     * @param configDetailEntity
     */
    private String compareAndConvert2IP(Long ipv4Start, Long ipv4End, String ipv6Start,String ipv6End, ProtectNetworkConfigDetailEntity configDetailEntity){
        Long[] startAndEnd = new Long[2];
        if (ipv4Start != null && ipv4End != null ){
            // 比较IPv4
            // 1. start < 原start && end > 原start  && end > 原end
            if(ipv4Start <= configDetailEntity.getIpv4Start() && ipv4End >= configDetailEntity.getIpv4Start() && ipv4End <= configDetailEntity.getIpv4End()){
                startAndEnd = new Long[]{configDetailEntity.getIpv4Start(),ipv4End};
                return IpAddress.longToIP(startAndEnd[0]) + "-" + IpAddress.longToIP(startAndEnd[1]);
            }
            // 2. start < 原end && end > 原end && start > 原start
            if (ipv4Start <= configDetailEntity.getIpv4End() && ipv4End >= configDetailEntity.getIpv4End() && ipv4Start >= configDetailEntity.getIpv4Start()) {
                startAndEnd = new Long[]{ipv4Start, configDetailEntity.getIpv4End()};
                return IpAddress.longToIP(startAndEnd[0]) + "-" + IpAddress.longToIP(startAndEnd[1]);
            }
            // 3.start > 原start && end < 原end
            if (ipv4Start >= configDetailEntity.getIpv4Start() && ipv4End <= configDetailEntity.getIpv4End()) {
                startAndEnd = new Long[]{ipv4Start, ipv4End};
                return IpAddress.longToIP(startAndEnd[0]) + "-" + IpAddress.longToIP(startAndEnd[1]);
            }
            // 4.start < 原start && end > 原end
            if (ipv4Start <= configDetailEntity.getIpv4Start() && ipv4End >= configDetailEntity.getIpv4End()) {
                startAndEnd = new Long[]{configDetailEntity.getIpv4Start(), configDetailEntity.getIpv4End()};
                return IpAddress.longToIP(startAndEnd[0]) + "-" + IpAddress.longToIP(startAndEnd[1]);
            }

            if (ipv6Start != null && ipv6End != null ){
                // TODO ipv6的比较算法
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(IPUtil.getStartIpFromIpAddress("1.1.1.1/32"));
        System.out.println(IPUtil.getStartIpFromIpAddress("1.1.1.2/31"));
        System.out.println(IPUtil.getEndIpFromIpAddress("1.1.1.2/31"));
        System.out.println(IPUtil.getStartIpFromIpAddress("1.1.1.4/30"));
        System.out.println(IPUtil.getEndIpFromIpAddress("1.1.1.4/30"));
        System.out.println(IPUtil.getStartIpFromIpAddress("1.1.1.8/31"));
        System.out.println(IPUtil.getEndIpFromIpAddress("1.1.1.8/31"));
        System.out.println(IPUtil.getStartIpFromIpAddress("1.1.1.10/32"));
//        System.out.println(IPUtil.convertRangeToSubnet("1.1.1.1-1.1.1.10"));
    }
}
