package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigMapper;
import com.abtnetworks.totems.auto.dto.AutoRecommendNatMappingDTO;
import com.abtnetworks.totems.auto.dto.PortectNetworkConfigExcelDTO;
import com.abtnetworks.totems.auto.dto.ProtectNetworkNatMappingExcelDTO;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.enums.PushNatFlagEnum;
import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.auto.service.PushAutoRecommendExcelService;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.tools.excel.ImportExcel;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.PushCopyBeanUtils;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhoumuhua
 * @desc 自动开通工单excel接口实现类
 * @date 2021-09-16
 */
@Service
@Slf4j
public class PushAutoRecommendExcelServiceImpl implements PushAutoRecommendExcelService {

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private ProtectNetworkConfigMapper protectNetworkConfigMapper;

    @Override
    public String parseProtectNetworkExcel(MultipartFile file, String userName, List<ProtectNetworkConfigVO> taskList) {
        String errMsg = null;
        if(taskList == null) {
            errMsg =  "解析失败，任务列表为空！<br>";
            return errMsg;
        }
        try {
            int failureNum = 0;
            //解析数据并校验
            StringBuilder failureMsg = new StringBuilder();
            ImportExcel ei = new ImportExcel(file, 0, 0);
            //防护网段nat映射在第二页
            ImportExcel natExcel = new ImportExcel(file, 0, 1);
            List<PortectNetworkConfigExcelDTO> list = ei.getDataList(PortectNetworkConfigExcelDTO.class);

            List<ProtectNetworkNatMappingExcelDTO> natList = natExcel.getDataList(ProtectNetworkNatMappingExcelDTO.class);

            boolean hasInvalid = false;
            List<String> deviceIpList = new ArrayList<>();
            Map<String, String> deviceNatMap = new HashMap<>();
            //校验防护网段数据
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("Excel value:" + list.get(i).toString());
                    try {
                        int rowNum = i + 1;
                        if (list.get(i) != null) {
                            PortectNetworkConfigExcelDTO entity = list.get(i);

                            if(entity.isEmpty()) {
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }

                            //数据验证
                            int rc = entity.validation();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("防护网段模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            ProtectNetworkConfigVO taskVO = new ProtectNetworkConfigVO();
                            BeanUtils.copyProperties(entity, taskVO);

                            if (deviceIpList.contains(entity.getDeviceIp())) {
                                log.error("设备IP：{} 重复！", entity.getDeviceIp());
                                String errorMsg = String.format("设备IP：%s重复", entity.getDeviceIp());
                                failureMsg.append("防护网段模版页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            //校验设备IP是否正确
                            NodeEntity node = nodeMapper.getTheNodeByIp(entity.getDeviceIp());
                            if (node == null || StringUtils.isEmpty(node.getUuid())) {
                                log.error("设备IP：{} 不存在或采集失败！", entity.getDeviceIp());
                                String errorMsg = String.format("设备IP：%s不存在或采集失败", entity.getDeviceIp());
                                failureMsg.append("防护网段模版页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }
                            taskVO.setDeviceName(node.getDeviceName());
                            taskVO.setDeviceUuid(node.getUuid());
                            ProtectNetworkConfigEntity existConfigEntity = protectNetworkConfigMapper.selectByDeviceUuid(node.getUuid());
                            if (null != existConfigEntity) {
                                taskVO.setId(existConfigEntity.getId());
                            }

                            if (PushNatFlagEnum.NAT_FLAG_Y.getCode().equals(taskVO.getNatFlag())) {
                                //如果开启了nat映射，则添加nat映射规则
                                List<ProtectNetworkNatMappingExcelDTO> natMappingList = natList.stream().filter(nat -> taskVO.getDeviceIp().equals(nat.getDeviceIp())).collect(Collectors.toList());

                                if (ObjectUtils.isEmpty(natMappingList)) {
                                    log.error("设备IP：{} 开启nat映射，未配置nat映射信息！", entity.getDeviceIp());
                                    String errorMsg = String.format("设备IP：%s开启nat映射，未配置nat映射信息", entity.getDeviceIp());
                                    failureMsg.append("防护网段模版页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                    hasInvalid = true;
                                    failureNum ++;
                                    continue;
                                }

                                natMappingList.forEach(nat -> {
                                    nat.setInsideProtocol(nat.getOutsideProtocol());
                                    nat.setNatType(PushNatTypeEnum.getCodeByDesc(nat.getNatType()));
                                });
                                List<AutoRecommendNatMappingDTO> natMappingDTOS = PushCopyBeanUtils.copyList(natMappingList, AutoRecommendNatMappingDTO.class);

                                taskVO.setNatMappingDTOList(natMappingDTOS);

                            } else {
                                taskVO.setNatMappingDTOList(new ArrayList<>());
                            }

                            taskList.add(taskVO);
                            deviceIpList.add(entity.getDeviceIp());

                            deviceNatMap.put(taskVO.getDeviceIp(), taskVO.getNatFlag());
                        }
                    } catch (Exception ex) {
                        hasInvalid = true;
                        log.error("批量导入防护网段异常", ex);
                        failureMsg.append(" 批量导入防护网段异常: " + list.get(i).getDeviceIp() + " 导入失败：<br>");
                        failureNum++;
                    }
                }
            } else {
                hasInvalid = true;
                failureMsg.append("导入文件内容为空！<br>");
            }

            //校验nat映射数据
            if (natList != null && natList.size() > 0) {
                for (int i = 0; i < natList.size(); i++) {
                    log.info("Excel value:" + natList.get(i).toString());
                    try {
                        int rowNum = i + 1;
                        if (natList.get(i) != null) {
                            ProtectNetworkNatMappingExcelDTO entity = natList.get(i);

                            if(entity.isEmpty()) {
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }

                            //数据验证
                            int rc = entity.validation();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("防护网段nat映射页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            ProtectNetworkConfigVO taskVO = new ProtectNetworkConfigVO();
                            BeanUtils.copyProperties(entity, taskVO);

                            if (!deviceIpList.contains(entity.getDeviceIp())) {
                                log.error("防护网段nat映射页设备IP：{} 在防护网段页不存在！", entity.getDeviceIp());
                                String errorMsg = String.format("防护网段nat映射页设备IP：%s在防护网段页不存在", entity.getDeviceIp());
                                failureMsg.append("防护网段nat映射页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            if (deviceNatMap.keySet().contains(entity.getDeviceIp()) && PushNatFlagEnum.NAT_FLAG_N.getCode().equals(deviceNatMap.get(entity.getDeviceIp()))) {
                                log.error("防护网段nat映射页设备IP：{} 未开启Nat映射！", entity.getDeviceIp());
                                String errorMsg = String.format("防护网段nat映射页设备IP：%s未开启Nat映射", entity.getDeviceIp());
                                failureMsg.append("防护网段nat映射页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }
                        }
                    } catch (Exception ex) {
                        hasInvalid = true;
                        log.error("批量导入防护网段异常", ex);
                        failureMsg.append(" 批量导入防护网段异常: " + list.get(i).getDeviceIp() + " 导入失败：<br>");
                        failureNum++;
                    }
                }
            }

            if (hasInvalid) {
                failureMsg.insert(0, " 失败 " + failureNum + "条任务信息 导入信息如下：<br>");
                errMsg = failureMsg.toString();
                return errMsg;
            }


        } catch (Exception e) {
            errMsg = "导入失败，请确保文件格式和内容正确！<br>";
            log.error("导入防护网段失败：", e);
            return errMsg;
        }
        return errMsg;
    }

    /**
     * 下载文件
     * @param response
     * @param fileExcitPath
     */
    @Override
    public void downLoadProtect(HttpServletResponse response, String fileExcitPath) {
        File src = new File(fileExcitPath);
        FileUtils.downloadOverView(src, response);
    }
}
