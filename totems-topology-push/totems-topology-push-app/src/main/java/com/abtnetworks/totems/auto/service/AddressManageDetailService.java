package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.auto.vo.AddressManagePushVO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @Description 对象管理详情
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
public interface AddressManageDetailService {

    /**
     * 新增
     * @param addressManageDetailEntity 地址详情实体对象
     */
    ReturnT<String> insert(AddressManageDetailEntity addressManageDetailEntity);

    /**
     * 删除
     * @param ids 地址详情id,多个用,隔开
     */
    ReturnT<String> deleteByIds(String ids) throws Exception;

    /**
     * 删除单条记录
     * @param vo 地址详情id,多个用,隔开
     */
    ReturnT<String> deleteSelectOne(AddressManageDetailVO vo) throws Exception;

    /**
     * 更新
     * @param addressManageDetailEntity 地址详情实体对象
     */
    ReturnT<String> update(AddressManageDetailEntity addressManageDetailEntity);

    /**
     * 查询 get By Id
     * @param id 主键
     */
    AddressManageDetailEntity getById(Integer id);

    /**
     * 分页查询
     * @param addressManageDetailEntity 地址详情实体对象
     * @param pageNum 页数
     * @param pageSize 分页大小
     */
    PageInfo<AddressManageDetailEntity> findList(AddressManageDetailEntity addressManageDetailEntity, int pageNum, int pageSize);

    /**
     * 通过id查询数据
     * @param id 主键
     * @return
     */
    AddressManageDetailVO getDetailById(Integer id);

    /**
     * 保存详情信息
     * @param vo 地址详情vo对象
     * @return
     */
    ReturnT<String> saveDetail(AddressManageDetailVO vo) throws Exception;

    /**
     * 地址校验
     * @param vo 地址详情vo对象
     * @return
     */
    ReturnT<String> checkAddress(AddressManageDetailVO vo) throws Exception;

    /**
     * 获取下发命令行数据
     * @param vo 地址详情vo对象
     * @return
     */
    List<AddressManagePushVO> getPushCmdByScenes(AddressManageDetailVO vo);

    /**
     * 命令行下发
     * @param vo 地址详情下发vo对象
     */
    ReturnT<String> pushCmd(AddressManagePushVO vo);

    /**
     * 通过场景查找设备信息
     * @param vo 地址详情vo对象
     * @return
     */
    ReturnT<Map<String, Map<String, Map<String, Object>>>> getDeviceByScenes(AddressManageDetailVO vo);


    /**
     * 根据地址对象名称模糊查询地址对象
     * @param name 地址对象名称
     * @return
     */
    List<AddressManageDetailDTO> findAddressByName(String name);

    /**
     * 根据地对象名称查询对象条目
     * @param name 地址对象名称
     * @return
     */
    ReturnT<List<AddressDetailEntryEntity>> findAddressEntityByName(String name);
}
