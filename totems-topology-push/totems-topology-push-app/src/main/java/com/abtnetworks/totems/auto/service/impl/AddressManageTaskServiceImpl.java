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
 * @Description ??????????????????
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

    //?????? Service
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    private AddressManageDetailService addressManageDetailService;

    //?????????????????????????????????????????????????????????????????????
    private static NetWorkGroupObjectShowVO PARENT_SHOW = new NetWorkGroupObjectShowVO();

    @Override
    public ReturnT<String> addTask(AddressManageTaskVO addressManageTaskVO) {
        // ????????????
        if (null == addressManageTaskVO) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "???????????????????????????");
        }
        log.info("------------??????????????????????????????{}------------", JSONObject.toJSONString(addressManageTaskVO));

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
            //?????????????????????
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
                //????????????????????????????????????map
                Map<String, List<String>> allAddrMap = addressManageTaskManager.getAddressMap(searchDTO);

                //?????????????????????????????????????????????
                ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(addressTaskDTO.getDeviceUuid(), addr);
                List<NetWorkGroupObjectShowVO> objectShowVOList = res.getData();
                if (ObjectUtils.isNotEmpty(objectShowVOList)) {
                    //??????????????????????????????--???name???????????????include???
                    Boolean isTopLevel = isTopLevel(objectShowVOList, addr);

                    if (isTopLevel) {
                        //?????????????????????????????????????????????
                        createAddressDetail(addressTaskDTO, objectShowVOList.get(0), 0,null, allAddrMap);
                    } else {
                        //??????????????????????????????????????????????????????????????????include???,???????????????????????????
                        NetWorkGroupObjectShowVO topLevelAddr = getTopLevelAddr(objectShowVOList, addr, addressManageTaskVO.getDeviceUuid());
                        //?????????????????????????????????????????????
                        createAddressDetail(addressTaskDTO, topLevelAddr, 0,null, allAddrMap);
                    }
                }
            }

            //???????????????????????????
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
                return new ReturnT<>(ReturnT.FAIL_CODE, String.format("?????????%s?????????%s?????????????????????", str, taskEntity.getAddressName()));
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
        // ????????????
        int i= addressManageTaskMapper.update(taskEntity);
        // ????????????
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
        // ????????????
        if (null == vo) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "???????????????????????????");
        }
        // ???????????????????????????
        AddressManageDetailEntity entity = new AddressManageDetailEntity();
        entity.setAddressName(vo.getAddressName());
        List<AddressManageDetailEntity> list = addressManageDetailMapper.findList(entity);
        if (CollectionUtils.isNotEmpty(list) && ObjectUtils.isEmpty( vo.getId() )){
            // ??????
            return  new ReturnT<>(ReturnT.FAIL_CODE, "?????????????????????");
        }
        if ( ObjectUtils.isNotEmpty( vo.getId() ) ){
            // ??????
            for (AddressManageDetailEntity detailEntity : list) {
                if ( !detailEntity.getId().equals( vo.getId() ) ){
                    return  new ReturnT<>(ReturnT.FAIL_CODE, "?????????????????????");
                }
            }
        }
        if(ObjectUtils.isEmpty(vo.getId())){
            log.info("------------????????????????????????{}------------", JSONObject.toJSONString(vo));
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getParentId());
            String addressName = vo.getAddressName();
            String addressCategory = vo.getAddressCategory();

            //??????????????????
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
            log.info("------------????????????????????????{}------------", JSONObject.toJSONString(vo));
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getId());
            String addressName = vo.getAddressName();
            String addressCategory = vo.getAddressCategory();

            //??????????????????
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
            //??????????????????
            addressManageTaskManager.deleteByTaskId(id);
            //??????????????????
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

    /**??????????????????????????????**/
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
            //??????????????????
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
        //????????????
        addressManageTaskMapper.updateScenes(requestDTO);
        return ReturnT.SUCCESS;
    }

    @Override
    public ResultRO<List<NetWorkGroupObjectShowVO>> getDeviceAddressList(DeviceObjectSearchDTO searchDTO, DeviceObjectTypeEnum objectTypeEnum) {
        return addressManageTaskManager.getAddressList(searchDTO, objectTypeEnum);
    }


    /**
     * ?????????????????????????????????
     * @param objectShowVOList ??????????????????????????????list
     * @return
     */
    private Boolean isTopLevel(List<NetWorkGroupObjectShowVO> objectShowVOList, String addr) {
        //??????????????????????????????????????????????????????????????????
        if (objectShowVOList.size() == 1) {
            return true;
        }

        Boolean isTopLevel = false;
        Boolean isName = false;
        Boolean isInclude = false;
        for (NetWorkGroupObjectShowVO showVo :  objectShowVOList) {
            //??????????????????????????????
            if (addr.equals(showVo.getName())) {
                isName = true;
            }
            //?????????????????????????????????????????????
            List<String> strings = getAddrListForAddrStr(showVo.getIncludeAddress());
            if (ObjectUtils.isNotEmpty(strings) && strings.contains(addr)) {
                isInclude = true;
            }
        }

        //????????????????????????????????????????????????????????????????????????????????????
        if (isName && !isInclude) {
            isTopLevel = true;
        }

        return isTopLevel;
    }

    /**
     * ?????????????????????????????????
     * @param objectShowVOList ??????????????????????????????list
     * @param addr ??????????????????
     * @return
     */
    private NetWorkGroupObjectShowVO getTopLevelAddr(List<NetWorkGroupObjectShowVO> objectShowVOList, String addr, String deviceUuid) {
        String parentName = "";
        for (NetWorkGroupObjectShowVO showVO : objectShowVOList) {
            List<String> addrList = getAddrListForAddrStr(showVO.getIncludeAddress());
            if (ObjectUtils.isNotEmpty(addrList) && addrList.contains(addr) && !addr.equals(showVO.getName())) {
                //include?????????name????????????????????????
                parentName = showVO.getName();
                PARENT_SHOW = showVO;
                break;
            }
        }

        if (StringUtils.isNotEmpty(parentName)) {
            //?????????????????????????????????
            ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(deviceUuid, parentName);
            List<NetWorkGroupObjectShowVO> parentData = res.getData();

            if (ObjectUtils.isNotEmpty(parentData)) {
                //??????????????????????????????--???name???????????????include???
                Boolean isTopLevel = isTopLevel(parentData, parentName);

                if (isTopLevel) {
                    //???????????????????????????????????????,?????????????????????????????????????????????????????????????????????
                    for (NetWorkGroupObjectShowVO initShowVo : parentData) {
                        if (parentName.equals(initShowVo.getName())) {
                            PARENT_SHOW = initShowVo;
                            break;
                        }
                    }
                } else {
                    //??????????????????????????????????????????????????????????????????include???,???????????????????????????
                    //??????????????????????????????????????????,?????????????????????????????????????????????????????????????????????
                    getTopLevelAddr(parentData, parentName, deviceUuid);
                }

            }
        }

        return PARENT_SHOW;
    }

    /**
     * ?????????????????????????????????????????????
     * @param addressTaskDTO ????????????DTO??????
     * @param showVO ??????????????????????????????
     * @param level ????????????
     * @param parentId ????????????????????????id
     * @param allAddrMap ??????????????????????????????
     */
    private void createAddressDetail(AddressManageTaskDTO addressTaskDTO, NetWorkGroupObjectShowVO showVO, Integer level, Integer parentId, Map<String, List<String>> allAddrMap) {
        //??????????????????
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
                    //???????????????????????????
                    for (String includeAddr : strs) {
                        if (allAddrMap.containsKey(includeAddr)) {
                            //????????????????????????????????????????????????
                            ResultRO<List<NetWorkGroupObjectShowVO>> res = addressManageTaskManager.getDeviceAddressByName(addressTaskDTO.getDeviceUuid(), includeAddr);
                            List<NetWorkGroupObjectShowVO> objectShowVOList = res.getData();
                            if (ObjectUtils.isNotEmpty(objectShowVOList)) {
                                for (NetWorkGroupObjectShowVO showVO1 : objectShowVOList) {
                                    if (includeAddr.equals(showVO1.getName())) {
                                        //??????????????????include?????????????????????
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
     * ?????????????????????List,str???,??????
     * @param str ?????????str
     * @return List<String> ?????????list
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
