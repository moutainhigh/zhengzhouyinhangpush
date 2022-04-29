package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.auto.dao.mysql.PushZoneLimitConfigMapper;
import com.abtnetworks.totems.auto.entity.PushZoneLimitConfigEntity;
import com.abtnetworks.totems.auto.service.PushZoneLimitConfigService;
import com.abtnetworks.totems.auto.vo.PushZoneLimitConfigVO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuchanghao
 * @desc 高级设置—生成策略域限制配置接口实现
 * @date 2021-11-16 14:05
 */
@Service
public class PushZoneLimitConfigServiceImpl implements PushZoneLimitConfigService {

    private static Logger logger = LoggerFactory.getLogger(PushZoneLimitConfigServiceImpl.class);

    @Autowired
    private PushZoneLimitConfigMapper pushZoneLimitConfigMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Override
    public ReturnT addOrUpdate(PushZoneLimitConfigVO vo) throws Exception {
        try{
            if(ObjectUtils.isEmpty(vo.getId())){
                // 新增
                PushZoneLimitConfigEntity record = new PushZoneLimitConfigEntity();
                BeanUtils.copyProperties(vo, record);
                record.setUuid(IdGen.uuid());
                List<DeviceDTO> deviceDTOAddList = new ArrayList<>();
                for (String deviceInfoUpdate : record.getDeviceInfo().split(",")){
                    NodeEntity addEntity = nodeMapper.getTheNodeByUuid(deviceInfoUpdate);
                    if(null == addEntity){
                        logger.info("设备:{}已被删除,从列表移除", deviceInfoUpdate);
                        continue;
                    }
                    DeviceDTO deviceDTO= new DeviceDTO();
                    deviceDTO.setVendorName(addEntity.getVendorName());
                    deviceDTO.setDeviceIp(addEntity.getIp());
                    deviceDTO.setDeviceName(addEntity.getDeviceName());
                    deviceDTO.setDeviceUuid(addEntity.getUuid());
                    deviceDTOAddList.add(deviceDTO);
                }

                JSONObject deviceObject = new JSONObject();
                String valueString = JSONObject.toJSONString(deviceDTOAddList);
                JSONArray value = JSONArray.parseArray(valueString);
                deviceObject.put("devices", value);
                record.setDeviceInfo(deviceObject.toString());

                pushZoneLimitConfigMapper.insert(record);
            } else {
                PushZoneLimitConfigEntity pushZoneLimitConfigEntity = pushZoneLimitConfigMapper.selectByPrimaryKey(vo.getId());
                String deviceInfo = pushZoneLimitConfigEntity.getDeviceInfo();

                List<String> deviceDTOList = new ArrayList<>();
                JSONObject jsonObject = JSONObject.parseObject(deviceInfo);
                JSONArray jsonArray = jsonObject.getJSONArray("devices");
                for(int index =0; index < jsonArray.size(); index ++) {
                    JSONObject deviceObject = jsonArray.getJSONObject(index);
                    DeviceDTO device = JSONObject.toJavaObject(deviceObject, DeviceDTO.class);
                    deviceDTOList.add(device.getDeviceUuid());
                }

                PushZoneLimitConfigEntity record = new PushZoneLimitConfigEntity();
                BeanUtils.copyProperties(vo, record);

                List<DeviceDTO> deviceDTOUpdateList = new ArrayList<>();
                String deviceInfoUpdates = record.getDeviceInfo();
                for (String deviceInfoUpdate : deviceInfoUpdates.split(",")){
                    if(deviceDTOList.contains(deviceInfoUpdate)) {
                        logger.info(String.format("设备(%s)已在列表中", deviceInfoUpdate));
                        continue;
                    }
                    NodeEntity addEntity = nodeMapper.getTheNodeByUuid(deviceInfoUpdate);
                    if(null == addEntity){
                        logger.info(String.format("设备(%s)已被删除,从列表移除", deviceInfoUpdate));
                        continue;
                    }
                    DeviceDTO deviceDTO= new DeviceDTO();
                    deviceDTO.setDeviceUuid(addEntity.getUuid());
                    deviceDTO.setVendorName(addEntity.getVendorName());
                    deviceDTO.setDeviceName(addEntity.getDeviceName());
                    deviceDTO.setDeviceIp(addEntity.getIp());
                    deviceDTOUpdateList.add(deviceDTO);
                }

                JSONObject deviceObject = new JSONObject();
                String valueString = JSONObject.toJSONString(deviceDTOUpdateList);
                JSONArray value = JSONArray.parseArray(valueString);
                deviceObject.put("devices", value);
                record.setDeviceInfo(deviceObject.toString());
                pushZoneLimitConfigMapper.updateByPrimaryKey(record);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            logger.error("新增或修改域限制配置异常，异常原因：", e);
            throw e;
        }
    }

    @Override
    public ReturnT delete(PushZoneLimitConfigVO vo) throws Exception {
        try{
            pushZoneLimitConfigMapper.deleteByPrimaryKey(vo.getId());
            return ReturnT.SUCCESS;
        } catch (Exception e ) {
            logger.error("删除域限制配置异常，异常原因：", e);
            throw e;
        }
    }

    @Override
    public PageInfo<PushZoneLimitConfigEntity> findList(PushZoneLimitConfigVO vo, int pageNum, int pageSize) {
        try{
            PageHelper.startPage(pageNum, pageSize);
            List<PushZoneLimitConfigEntity> list = pushZoneLimitConfigMapper.findList();
            PageInfo<PushZoneLimitConfigEntity> pageInfo = new PageInfo<>(list);
            return pageInfo;
        } catch (Exception e ) {
            logger.error("查询域限制配置异常，异常原因：", e);
            throw e;
        }
    }
}
