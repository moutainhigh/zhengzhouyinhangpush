package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.AddressDetailEntryMapper;
import com.abtnetworks.totems.auto.dao.mysql.AddressManageDetailMapper;
import com.abtnetworks.totems.auto.dao.mysql.AddressManageTaskMapper;
import com.abtnetworks.totems.auto.dto.AddressManageTaskDTO;
import com.abtnetworks.totems.auto.dto.AddressUpdateScenesDTO;
import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import com.abtnetworks.totems.auto.enums.AddressStatusEnum;
import com.abtnetworks.totems.auto.manager.AddressManageTaskManager;
import com.abtnetworks.totems.auto.service.AddressManageDetailService;
import com.abtnetworks.totems.auto.service.AddressManageTaskService;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.auto.vo.AddressManageTaskVO;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Service
@Slf4j
public class AddressManageTaskServiceImpl implements AddressManageTaskService {

    @Autowired
    private AddressManageTaskMapper addressManageTaskMapper;

    @Autowired
    private AddressManageDetailMapper addressManageDetailMapper;

    @Autowired
    private AddressDetailEntryMapper addressDetailEntryMapper;

    @Autowired
    private AddressManageTaskManager addressManageTaskManager;

    //场景 Service
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    private AddressManageDetailService addressManageDetailService;

    //定义全局静态父级地址对象，用于递归返回最终结果
    private static NetWorkGroupObjectShowVO PARENT_SHOW = new NetWorkGroupObjectShowVO();

    @Override
    public ReturnT<String> addTask(AddressManageTaskVO addressManageTaskVO) {
        // 校验数据
        if (null == addressManageTaskVO) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "地址组名称不能为空");
        }
        log.info("------------开始新建对象管理为：{}------------", JSONObject.toJSONString(addressManageTaskVO));

        String addressName = addressManageTaskVO.getAddressName();
        String[] addrNameList = addressName.split(PolicyConstants.ADDRESS_SEPERATOR);

        AddressManageTaskEntity addressManageTaskEntity = new AddressManageTaskEntity();
        BeanUtils.copyProperties(addressManageTaskVO, addressManageTaskEntity);
        addressManageTaskEntity.setAddressName(addressName);
        addressManageTaskEntity.setUuid(IdGen.uuid());
        addressManageTaskEntity.setCreateTime(new Date());
        addressManageTaskMapper.insert(addressManageTaskEntity);

        AddressManageTaskDTO addressTaskDTO = new AddressManageTaskDTO();
        BeanUtils.copyProperties(addressManageTaskEntity, addressTaskDTO);
        addressTaskDTO.setDeviceUuid(addressManageTaskVO.getDeviceUuid());
        for (String addr : addrNameList) {
            //初始化顶级对象
            PARENT_SHOW = new NetWorkGroupObjectShowVO();

            if ("new".equals(addressManageTaskVO.getInitCategory())) {
                AddressManageDetailEntity detail = new AddressManageDetailEntity();
                detail.setAddressName(addr);
                detail.setScenesUuid(addressTaskDTO.getScenesUuid());
                detail.setAddressLevel(0);
                detail.setUuid(IdGen.uuid());
                detail.setParentId(null);
                detail.setTaskId(addressTaskDTO.getId());
                detail.setCreateTime(new Date());
                detail.setStatus(0);
                detail.setAddressType(addressManageTaskVO.getAddressCategory());
                addressManageDetailMapper.insert(detail);

                if (DeviceObjectTypeEnum.NETWORK_OBJECT.getCode().equals(addressManageTaskVO.getAddressCategory())) {
                    String addressEntry = addressManageTaskVO.getAddressEntry();
                    String[] addrEntryList = addressEntry.split(PolicyConstants.ADDRESS_SEPERATOR);
                    for (String addrEntry : addrEntryList) {
                        AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
                        detailEntryEntity.setDetailId(detail.getId());
                        detailEntryEntity.setTaskId(addressTaskDTO.getId());
                        detailEntryEntity.setAddressName(addrEntry);
                        detailEntryEntity.setAddressType("ADDRESS");
                        detailEntryEntity.setUuid(IdGen.uuid());
                        detailEntryEntity.setCreateTime((new Date()));
                        addressDetailEntryMapper.insert(detailEntryEntity);
                    }
                }
            } else {
                DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
                searchDTO.setDeviceUuid(addressTaskDTO.getDeviceUuid());
                //获取地址和地址内容的映射map
                Map<String, List<String>> allAddrMap = addressManageTaskManager.getAddressMap(searchDTO);

                //开始初始化对象管理批量管理列表
                ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(addressTaskDTO.getDeviceUuid(), addr);
                List<NetWorkGroupObjectShowVO> objectShowVOList = res.getData();
                if (ObjectUtils.isNotEmpty(objectShowVOList)) {
                    //判断是否为最顶级对象--在name中，且不在include中
                    Boolean isTopLevel = isTopLevel(objectShowVOList, addr);

                    if (isTopLevel) {
                        //顶级对象入库，并初始化层级关系
                        createAddressDetail(addressTaskDTO, objectShowVOList.get(0), 0,null, allAddrMap);
                    } else {
                        //如果不是顶级对象，取到当前对象的上级对象，在include中,遍历取到最顶级对象
                        NetWorkGroupObjectShowVO topLevelAddr = getTopLevelAddr(objectShowVOList, addr, addressManageTaskVO.getDeviceUuid());
                        //顶级对象入库，并初始化层级关系
                        createAddressDetail(addressTaskDTO, topLevelAddr, 0,null, allAddrMap);
                    }
                }
            }

            //更新初始化对象状态
            List<AddressManageDetailEntity> detailList = addressManageDetailMapper.getByTaskId(addressTaskDTO.getId());
            List<AddressDetailEntryEntity> entryList = addressDetailEntryMapper.getByTaskId(addressTaskDTO.getId());
            for (AddressManageDetailEntity detailEntity : detailList) {
                List<AddressDetailEntryEntity> matchEntryList = entryList.stream().filter(e -> detailEntity.getId().equals(e.getDetailId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(matchEntryList)) {
                    detailEntity.setAddressType(DeviceObjectTypeEnum.NETWORK_OBJECT.getCode());
                } else {
                    detailEntity.setAddressType(DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT.getCode());
                }
                addressManageDetailMapper.update(detailEntity);
            }
        }

        return ReturnT.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<String> addAllTask(AddressManageTaskVO vo) {
        String addressName = vo.getAddressName();
        String[] split = addressName.split(",");
        for (String str : split) {
            AddressManageDetailEntity entity = new AddressManageDetailEntity();
            entity.setAddressName(str);
            List<AddressManageDetailEntity> list = addressManageDetailMapper.findList(entity);
            if (CollectionUtils.isNotEmpty(list)){
                AddressManageTaskEntity taskEntity = addressManageTaskMapper.getById(list.get(0).getTaskId());
                return new ReturnT<>(ReturnT.FAIL_CODE, String.format("对象【%s】在【%s】工单中已存在", str, taskEntity.getAddressName()));
            }
            vo.setAddressName(str);
            addTask(vo);
        }
        return ReturnT.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<String> update(AddressManageTaskVO vo) throws Exception{
        Integer id = vo.getId();
        AddressManageTaskEntity taskEntity = addressManageTaskMapper.getById(id);
        taskEntity.setScenesUuid( vo.getScenesUuid() );
        // 更新主表
        int i= addressManageTaskMapper.update(taskEntity);
        // 更新附表
        AddressManageDetailEntity entity = new AddressManageDetailEntity();
        entity.setScenesUuid( vo.getScenesUuid() );
        entity.setTaskId( id );
        addressManageDetailMapper.updateByTaskId(entity);
        List<Integer> idList = vo.getIdList();
        AddressManageDetailVO addressManageDetailVO = new AddressManageDetailVO();
        addressManageDetailVO.setIdList(idList);
        return addressManageDetailService.checkAddress(addressManageDetailVO);
    }

    @Override
    public ReturnT<String> addAddress(AddressManageDetailVO vo) {
        // 校验数据
        if (null == vo) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "地址组名称不能为空");
        }
        // 校验名称是否已存在
        AddressManageDetailEntity entity = new AddressManageDetailEntity();
        entity.setAddressName(vo.getAddressName());
        List<AddressManageDetailEntity> list = addressManageDetailMapper.findList(entity);
        if (CollectionUtils.isNotEmpty(list) && ObjectUtils.isEmpty( vo.getId() )){
            // 新增
            return  new ReturnT<>(ReturnT.FAIL_CODE, "对象名称已存在");
        }
        if ( ObjectUtils.isNotEmpty( vo.getId() ) ){
            // 编辑
            for (AddressManageDetailEntity detailEntity : list) {
                if ( !detailEntity.getId().equals( vo.getId() ) ){
                    return  new ReturnT<>(ReturnT.FAIL_CODE, "对象名称已存在");
                }
            }
        }
        if(ObjectUtils.isEmpty(vo.getId())){
            log.info("------------开始添加地址为：{}------------", JSONObject.toJSONString(vo));
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getParentId());
            String addressName = vo.getAddressName();
            String addressCategory = vo.getAddressCategory();

            //本次新增对象
            AddressManageDetailEntity detail = new AddressManageDetailEntity();
            detail.setAddressName(addressName);
            detail.setScenesUuid(detailEntity.getScenesUuid());
            detail.setAddressLevel(detailEntity.getAddressLevel()+1);
            detail.setUuid(IdGen.uuid());
            detail.setParentId(vo.getParentId());
            detail.setTaskId(detailEntity.getTaskId());
            detail.setCreateTime(new Date());
            detail.setStatus(0);
            detail.setAddressType(addressCategory);
            addressManageDetailMapper.insert(detail);

            if (DeviceObjectTypeEnum.NETWORK_OBJECT.getCode().equals(addressCategory)) {
                String addressEntry = vo.getAddressEntry();
                String[] addrEntryList = addressEntry.split(PolicyConstants.ADDRESS_SEPERATOR);
                for (String addrEntry : addrEntryList) {
                    AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
                    detailEntryEntity.setDetailId(detail.getId());
                    detailEntryEntity.setTaskId(detailEntity.getTaskId());
                    detailEntryEntity.setAddressName(addrEntry);
                    detailEntryEntity.setAddressType("ADDRESS");
                    detailEntryEntity.setUuid(IdGen.uuid());
                    detailEntryEntity.setCreateTime((new Date()));
                    addressDetailEntryMapper.insert(detailEntryEntity);
                }
            }
        } else {
            log.info("------------开始编辑地址为：{}------------", JSONObject.toJSONString(vo));
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getId());
            String addressName = vo.getAddressName();
            String addressCategory = vo.getAddressCategory();

            //本次编辑对象
            detailEntity.setAddressName(addressName);

            if (DeviceObjectTypeEnum.NETWORK_OBJECT.getCode().equals(addressCategory)) {
                detailEntity.setAddressType(DeviceObjectTypeEnum.NETWORK_OBJECT.getCode());
                addressManageDetailMapper.update(detailEntity);
                addressDetailEntryMapper.deleteByDetailId(detailEntity.getId());
                String addressEntry = vo.getAddressEntry();
                String[] addrEntryList = addressEntry.split(PolicyConstants.ADDRESS_SEPERATOR);
                for (String addrEntry : addrEntryList) {
                    AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
                    detailEntryEntity.setDetailId(detailEntity.getId());
                    detailEntryEntity.setTaskId(detailEntity.getTaskId());
                    detailEntryEntity.setAddressName(addrEntry);
                    detailEntryEntity.setAddressType("ADDRESS");
                    detailEntryEntity.setUuid(IdGen.uuid());
                    detailEntryEntity.setCreateTime((new Date()));
                    addressDetailEntryMapper.insert(detailEntryEntity);
                }

            } else if (DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT.getCode().equals(addressCategory)) {
                detailEntity.setAddressType(DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT.getCode());
                addressManageDetailMapper.update(detailEntity);
            }
        }


        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> delete(String ids) throws Exception {
        String[] idList = ids.split(PolicyConstants.ADDRESS_SEPERATOR);

        for (String idStr : idList) {
            Integer id = Integer.valueOf(idStr);
            //删除详情对象
            addressManageTaskManager.deleteByTaskId(id);
            //删除任务对象
            addressManageTaskMapper.delete(id);
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> update(AddressManageTaskEntity addressManageTaskEntity) {
        int ret = addressManageTaskMapper.update(addressManageTaskEntity);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public AddressManageTaskEntity getById(int id) {
        return addressManageTaskMapper.getById(id);
    }

    @Override
    public AddressManageTaskEntity getByUUID(String uuid) {
        return addressManageTaskMapper.getByUuid(uuid);
    }

    @Override
    public PageInfo<AddressManageTaskEntity> findList(AddressManageTaskEntity addressManageTaskEntity, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<AddressManageTaskEntity> list = addressManageTaskManager.findList(addressManageTaskEntity);

        for (AddressManageTaskEntity entity : list) {
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(entity.getScenesUuid());
            if (scenesEntity == null) {
                entity.setScenesUuid(null);
            } else {
                entity.setScenesName(scenesEntity.getName());
            }
        }
        PageInfo<AddressManageTaskEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }


    @Override
    public List<AddressManageDetailVO> findDetailList(AddressManageDetailEntity addressManageDetailEntity) {
        List<AddressManageDetailVO> list = addressManageTaskManager.getParentDetailListByTaskId(addressManageDetailEntity.getTaskId());
        for (AddressManageDetailVO entity : list) {
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(entity.getScenesUuid());
            if (scenesEntity == null) {
                entity.setScenesUuid(null);
                entity.setStatus(AddressStatusEnum.INIT_STATUS.getCode());
            } else {
                entity.setScenesName(scenesEntity.getName());
            }
        }
        return list;
    }

    /**获取地址、地址组列表**/
    @Override
    public PageInfo<NetWorkGroupObjectShowVO> getAddressListByDevice(AddressManageTaskVO vo, int pageNum, int pageSize) {
        List<NetWorkGroupObjectShowVO> totalList = new ArrayList<>();
        if (StringUtils.isEmpty(vo.getAddressName())) {
            return new PageInfo<>(totalList);
        }

        PageHelper.startPage(pageNum, pageSize);

        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();

        searchDTO.setDeviceUuid(vo.getDeviceUuid());
        searchDTO.setName(vo.getAddressName());

        totalList = addressManageTaskManager.getAllDetailAddressBySearchDTO(searchDTO, vo.getAddressCategory());

        PageInfo<NetWorkGroupObjectShowVO> pageInfo = new PageInfo<>(totalList);

        return pageInfo;
    }

    @Override
    public ReturnT<String> changeScenes(AddressUpdateScenesDTO requestDTO) throws Exception {
        for (Integer id : requestDTO.getIdList()) {
            //删除详情对象
            List<AddressManageDetailEntity> byTaskId = addressManageDetailMapper.getByTaskId(id);
            if (CollectionUtils.isNotEmpty(byTaskId)) {
                for (AddressManageDetailEntity detailEntity : byTaskId) {
                    addressManageTaskManager.deletePushTaskByDetail(detailEntity);
                }
                List<Integer> collect = byTaskId.stream().map(AddressManageDetailEntity::getId).collect(Collectors.toList());
                AddressUpdateScenesDTO addressUpdateScenesDTO = new AddressUpdateScenesDTO();
                addressUpdateScenesDTO.setIdList(collect);
                addressUpdateScenesDTO.setScenesUuid(requestDTO.getScenesUuid());
                addressManageDetailMapper.updateDetailScenes(addressUpdateScenesDTO);
            }
        }
        //更新场景
        addressManageTaskMapper.updateScenes(requestDTO);
        return ReturnT.SUCCESS;
    }

    @Override
    public ResultRO<List<NetWorkGroupObjectShowVO>> getDeviceAddressList(DeviceObjectSearchDTO searchDTO, DeviceObjectTypeEnum objectTypeEnum) {
        return addressManageTaskManager.getAddressList(searchDTO, objectTypeEnum);
    }


    /**
     * 当前对象是否是顶级对象
     * @param objectShowVOList 设备地址查询展示对象list
     * @return
     */
    private Boolean isTopLevel(List<NetWorkGroupObjectShowVO> objectShowVOList, String addr) {
        //如果只查到一个相关对象，那么自己就是顶级对象
        if (objectShowVOList.size() == 1) {
            return true;
        }

        Boolean isTopLevel = false;
        Boolean isName = false;
        Boolean isInclude = false;
        for (NetWorkGroupObjectShowVO showVo :  objectShowVOList) {
            //该对象是否为自己本身
            if (addr.equals(showVo.getName())) {
                isName = true;
            }
            //该对象是否为另一个对象的下一级
            List<String> strings = getAddrListForAddrStr(showVo.getIncludeAddress());
            if (ObjectUtils.isNotEmpty(strings) && strings.contains(addr)) {
                isInclude = true;
            }
        }

        //如果查到为自己本身，且不为别的对象的下一级，则为顶级对象
        if (isName && !isInclude) {
            isTopLevel = true;
        }

        return isTopLevel;
    }

    /**
     * 递归获取顶级地址组对象
     * @param objectShowVOList 设备地址查询展示对象list
     * @param addr 地址对象名称
     * @return
     */
    private NetWorkGroupObjectShowVO getTopLevelAddr(List<NetWorkGroupObjectShowVO> objectShowVOList, String addr, String deviceUuid) {
        String parentName = "";
        for (NetWorkGroupObjectShowVO showVO : objectShowVOList) {
            List<String> addrList = getAddrListForAddrStr(showVO.getIncludeAddress());
            if (ObjectUtils.isNotEmpty(addrList) && addrList.contains(addr) && !addr.equals(showVO.getName())) {
                //include包含且name不等于即为父对象
                parentName = showVO.getName();
                PARENT_SHOW = showVO;
                break;
            }
        }

        if (StringUtils.isNotEmpty(parentName)) {
            //查找父级对象相关地址组
            ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(deviceUuid, parentName);
            List<NetWorkGroupObjectShowVO> parentData = res.getData();

            if (ObjectUtils.isNotEmpty(parentData)) {
                //判断是否为最顶级对象--在name中，且不在include中
                Boolean isTopLevel = isTopLevel(parentData, parentName);

                if (isTopLevel) {
                    //获取顶级对象中的子级地址组,通过子级地址组找到下一级地址组，直到找不到为止
                    for (NetWorkGroupObjectShowVO initShowVo : parentData) {
                        if (parentName.equals(initShowVo.getName())) {
                            PARENT_SHOW = initShowVo;
                            break;
                        }
                    }
                } else {
                    //如果不是顶级对象，取到当前对象的上级对象，在include中,遍历取到最顶级对象
                    //并获取顶级对象中的子级地址组,通过子级地址组找到下一级地址组，直到找不到为止
                    getTopLevelAddr(parentData, parentName, deviceUuid);
                }

            }
        }

        return PARENT_SHOW;
    }

    /**
     * 传递顶级对象，并初始化地址详情
     * @param addressTaskDTO 地址任务DTO对象
     * @param showVO 设备地址查询展示对象
     * @param level 地址级别
     * @param parentId 地址详情对象父级id
     * @param allAddrMap 设备所有有效地址对象
     */
    private void createAddressDetail(AddressManageTaskDTO addressTaskDTO, NetWorkGroupObjectShowVO showVO, Integer level, Integer parentId, Map<String, List<String>> allAddrMap) {
        //地址详情入库
        if (allAddrMap.containsKey(showVO.getName())) {
            AddressManageDetailEntity detail = new AddressManageDetailEntity();
            detail.setAddressName(showVO.getName());
            detail.setScenesUuid(addressTaskDTO.getScenesUuid());
            detail.setAddressLevel(level);
            detail.setUuid(IdGen.uuid());
            detail.setParentId(parentId);
            detail.setTaskId(addressTaskDTO.getId());
            detail.setCreateTime(new Date());
            detail.setStatus(0);
            detail.setAddressType(showVO.getTypeDesc());
            addressManageDetailMapper.insert(detail);


            if (showVO.getName().equals(showVO.getIncludeAddress())) {
                AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
                detailEntryEntity.setDetailId(detail.getId());
                detailEntryEntity.setTaskId(addressTaskDTO.getId());
                detailEntryEntity.setAddressName(showVO.getIncludeAddress());
                detailEntryEntity.setAddressType("ADDRESS");
                detailEntryEntity.setUuid(IdGen.uuid());
                detailEntryEntity.setCreateTime((new Date()));
                addressDetailEntryMapper.insert(detailEntryEntity);
            } else {
                List<String> strs = getAddrListForAddrStr(showVO.getIncludeAddress());
                if (ObjectUtils.isNotEmpty(strs) && !strs.contains(showVO.getName())) {
                    //查找下一级地址对象
                    for (String includeAddr : strs) {
                        if (allAddrMap.containsKey(includeAddr)) {
                            //如果包含地址为对象，则查找下一级
                            ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(addressTaskDTO.getDeviceUuid(), includeAddr);
                            List<NetWorkGroupObjectShowVO> objectShowVOList = res.getData();
                            if (ObjectUtils.isNotEmpty(objectShowVOList)) {
                                for (NetWorkGroupObjectShowVO showVO1 : objectShowVOList) {
                                    if (includeAddr.equals(showVO1.getName())) {
                                        //父级对象中的include为当前地址的值
                                        createAddressDetail(addressTaskDTO, showVO1, level+1, detail.getId(), allAddrMap);
                                        break;
                                    }
                                }
                            }
                        } else {
                            AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
                            detailEntryEntity.setDetailId(detail.getId());
                            detailEntryEntity.setTaskId(addressTaskDTO.getId());
                            detailEntryEntity.setAddressName(includeAddr);
                            detailEntryEntity.setAddressType("ADDRESS");
                            detailEntryEntity.setUuid(IdGen.uuid());
                            detailEntryEntity.setCreateTime((new Date()));
                            addressDetailEntryMapper.insert(detailEntryEntity);

                        }
                    }
                }
            }

        } else {
            AddressDetailEntryEntity detailEntryEntity = new AddressDetailEntryEntity();
            detailEntryEntity.setDetailId(parentId);
            detailEntryEntity.setTaskId(addressTaskDTO.getId());
            detailEntryEntity.setAddressName(showVO.getName());
            detailEntryEntity.setAddressType("ADDRESS");
            detailEntryEntity.setUuid(IdGen.uuid());
            detailEntryEntity.setCreateTime((new Date()));
            addressDetailEntryMapper.insert(detailEntryEntity);
        }
    }


    /**
     * 将字符串转换成List,str以,分隔
     * @param str 转换前str
     * @return List<String> 转换后list
     */
    private List<String> getAddrListForAddrStr(String str) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<>();
        }

        String[] strs = str.split(";");

        List<String> strList = Arrays.asList(strs);

        return strList;
    }

}
