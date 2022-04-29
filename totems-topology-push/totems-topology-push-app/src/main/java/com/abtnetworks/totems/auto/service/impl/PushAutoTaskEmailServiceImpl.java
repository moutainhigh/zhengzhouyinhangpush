package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.auto.constants.DeviceModelNumberConstants;
import com.abtnetworks.totems.auto.dto.AutoRecommendAllResultDTO;
import com.abtnetworks.totems.auto.dto.AutoRecommendErrorDetailDTO;
import com.abtnetworks.totems.auto.dto.AutoRecommendOrderStatusDTO;
import com.abtnetworks.totems.auto.dto.WillExpirePolicyEmailDTO;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.enums.AutoRecommendStatusEnum;
import com.abtnetworks.totems.auto.enums.AutoTaskOrderStatusEnum;
import com.abtnetworks.totems.auto.enums.PushAccessTypeEnum;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.service.PushAutoTaskEmailService;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.PushStatusConstans;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.ZipUtil;
import com.abtnetworks.totems.push.dao.mysql.SystemParamMapper;
import com.abtnetworks.totems.push.dto.MailServerConfDTO;
import com.abtnetworks.totems.push.utils.CustomMailTool;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @desc 自动开通下发邮件告警相关接口实现类
 * @author zhoumuhua
 * @date 2021-07-06
 */
@Service
@Slf4j
public class PushAutoTaskEmailServiceImpl implements PushAutoTaskEmailService {

    @Value("${push.download-file}")
    String dirPath;

    @Autowired
    SystemParamMapper systemParamMapper;

    @Autowired
    NodeMapper nodeMapper;

    @Autowired
    RecommendTaskMapper recommendTaskMapper;

    @Autowired
    CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Override
    public void startAutoRecommendPushEmail(List<CommandTaskEditableEntity> taskEditableEntityList, AutoRecommendTaskEntity autoTaskEntity) {
        if (ObjectUtils.isEmpty(taskEditableEntityList) || ObjectUtils.isEmpty(autoTaskEntity)) {
            return;
        }
        //发送下发结果邮件至提单人
        /*sendAutoRecommendPushEmailCustomer(taskEditableEntityList, autoTaskEntity);
        //发送下发结果至管理员
        sendAutoRecommendPushEmailManager(taskEditableEntityList, autoTaskEntity);*/
    }

    @Override
    public void startWillExpirePolicyEmail(AutoRecommendTaskEntity autoTaskEntity) {
        if (ObjectUtils.isEmpty(autoTaskEntity)) {
            return;
        }

        //邮件发送主题
        String subject = "即将过期策略邮件告警";
        //邮件发送内容
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append("您好，以下为即将过期策略邮件告警").append("<br><br>");
        sb.append("&nbsp;&nbsp;").append("工单号：").append(getBoldStr(autoTaskEntity.getTheme())).append("<br>");
        sb.append("&nbsp;&nbsp;").append("申请人：").append(getBoldStr(autoTaskEntity.getApplicant())).append("<br>");
        sb.append("&nbsp;&nbsp;").append("访问类型：").append(getBoldStr(PushAccessTypeEnum.getDescByCode(autoTaskEntity.getTaskType()))).append("<br>");
        sb.append("&nbsp;&nbsp;").append("关联设备信息：").append(getBoldStr(getDeviceNameByAutoTaskEntity(autoTaskEntity))).append("<br>");
        sb.append("&nbsp;&nbsp;").append("过期时间：").append(getBoldStr(sdf.format(autoTaskEntity.getEndTime()))).append("<br>");
        double expireDate = DateUtils.getDistanceOfTwoDate(new Date(), autoTaskEntity.getEndTime());
        int expireNum = (int) expireDate;
        sb.append("&nbsp;&nbsp;").append("距离当前还有").append(getBoldStr(String.valueOf(expireNum))).append("天过期").append("<br>");

        sb.append("&nbsp;&nbsp;").append("附件为即将过期策略邮件告警报表").append("<br>");
        sb.append("<br><br>").append("<font size=\"2\" color=\"gray\">本邮件属于系统邮件,来源于策略可视化管理平台，请勿回复。</font>").append("<br>");
        String content = sb.toString();
        String userReceiveEmail = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL);
        String email = "";
        if (AdvancedSettingsConstants.IS_RECEIVE_EMAIL.equals(userReceiveEmail)) {
            email = autoTaskEntity.getApplicantEmail();
        }

        String manageEmail = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL);
        if (StringUtils.isNotBlank(manageEmail)) {
            log.info("管理员发送人信息为：{}", manageEmail);
            String taskEmail = StringUtils.isNotEmpty(email) ? PolicyConstants.ADDRESS_SEPERATOR +  email : "";
            email = manageEmail + taskEmail;
        }

        String relevancyNat = autoTaskEntity.getRelevancyNat();
        JSONArray jsonArray = JSONArray.parseArray(relevancyNat);

        List<WillExpirePolicyEmailDTO> emailDTOList = new ArrayList<>();
        for (int index = 0; index < jsonArray.size(); index++) {
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            Integer natId = jsonObject.getInteger("id");

            RecommendTaskEntity taskEntity = recommendTaskMapper.getById(natId);

            List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
            WillExpirePolicyEmailDTO emailDTO = new WillExpirePolicyEmailDTO();
            BeanUtils.copyProperties(taskEntity, emailDTO);
            emailDTO.setStartTime(autoTaskEntity.getStartTime());
            emailDTO.setEndTime(autoTaskEntity.getEndTime());
            emailDTO.setApplicant(autoTaskEntity.getApplicant());
            emailDTO.setApplicantEmail(autoTaskEntity.getApplicantEmail());
            emailDTO.setTaskEditableEntityList(commandTaskEditableList);
            emailDTOList.add(emailDTO);
        }

        try {
            //邮件发送
            if (StringUtils.isNotBlank(email)) {
                log.info("开始发送邮件，subject:{},toAddress:{}", subject, email);

                MailServerConfDTO emialDTO = systemParamMapper.findEmailParam(PushConstants.EMAIL_PARAM_GROUP_NAME);
                if (ObjectUtils.isNotEmpty(emialDTO)) {

                    //邮件发送附件
                    String emailFile = getWillExpirePolicyFile(emailDTOList);
                    CustomMailTool.sendEmail(emialDTO, email, subject, content, emailFile);
                } else {
                    log.error("查询系统邮箱配置，返回空");
                }
            }
        } catch (Exception e) {
            log.error("自动开通下发时，工单号{}，发送邮件告警失败", autoTaskEntity.getTheme(), e);
        }
    }

    @Override
    public void startAutoRecommendPushBatchTaskEmail(Map<String, List<CommandTaskEditableEntity>> taskMap, List<AutoRecommendTaskEntity> entityList) {
        if (ObjectUtils.isEmpty(taskMap) || CollectionUtils.isEmpty(entityList)) {
            return;
        }
        List<String> themeList = entityList.stream().map(s -> s.getTheme()).collect(Collectors.toList());
        Set<String> perfixSet = new HashSet<>();
        Map<String, List<String>> perfixToThemeMap = new HashMap<>();
        themeList.forEach(t -> {
            if (t.contains(PolicyConstants.VALUE_RANGE_SEPERATOR)) {
                String[] split = t.split(PolicyConstants.VALUE_RANGE_SEPERATOR);
                perfixSet.add(split[0]);
                if (perfixToThemeMap.containsKey(split[0])) {
                    List<String> themes = perfixToThemeMap.get(split[0]);
                    themes.add(t);
                    perfixToThemeMap.put(split[0], themes);
                } else {
                    List<String> themes = new ArrayList<>();
                    themes.add(t);
                    perfixToThemeMap.put(split[0], themes);
                }
            } else {
                perfixSet.add(t);
                List<String> themes = new ArrayList<>();
                themes.add(t);
                perfixToThemeMap.put(t, themes);
            }
        });

        for (String perfix : perfixSet) {
            List<String> themes = perfixToThemeMap.get(perfix);
            List<AutoRecommendTaskEntity> autoEmailList = new ArrayList<>();
            themes.forEach(theme -> {
                List<AutoRecommendTaskEntity> taskList = entityList.stream().filter(s -> theme.equals(s.getTheme())).collect(Collectors.toList());
                autoEmailList.addAll(taskList);
            });
            //发送下发结果邮件至提单人
            sendAutoRecommendPushEmailCustomer(autoEmailList, perfix);
            //发送下发结果至管理员
            sendAutoRecommendPushEmailManager(taskMap, autoEmailList, perfix);
        }

    }

    /**
     * 发送自动开通下发邮件给提单人
     * @param autoTaskEntityList
     * @param themePerfix
     */
    private void sendAutoRecommendPushEmailCustomer(List<AutoRecommendTaskEntity> autoTaskEntityList, String themePerfix) {
        String userReceiveEmail = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL);
        String email = autoTaskEntityList.get(0).getApplicantEmail();
        log.info("提单人发送人信息为：{}", email);
        if (!AdvancedSettingsConstants.IS_RECEIVE_EMAIL.equals(userReceiveEmail) || StringUtils.isBlank(email)) {
            return;
        }

        String emailStatus = getEmailStatus(autoTaskEntityList);
        //邮件发送主题
        String subject = "【"+themePerfix+"】自动开通下发邮件告警-" + emailStatus;
        //邮件发送内容
        StringBuilder sb = new StringBuilder();

        sb.append("您好，工单已下发，工单下发详情信息如下").append("<br><br>");

        for (AutoRecommendTaskEntity autoTaskEntity : autoTaskEntityList) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("&nbsp;&nbsp;").append("工单号：").append(getBoldStr(autoTaskEntity.getTheme())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("申请人：").append(getBoldStr(autoTaskEntity.getApplicant())).append("<br>");
            String color = getTaskStatusColor(autoTaskEntity.getStatus());

            sb.append("&nbsp;&nbsp;").append("状态：").append(getColorStr(AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()), color)).append("<br>");
            sb.append("<br>");
            sb.append("&nbsp;&nbsp;").append("访问类型：").append(getBoldStr(PushAccessTypeEnum.getDescByCode(autoTaskEntity.getTaskType()))).append("<br>");
            sb.append("&nbsp;&nbsp;").append("源IP：").append(getBoldStr(autoTaskEntity.getSrcIp())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("目的IP：").append(getBoldStr(autoTaskEntity.getDstIp())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("服务：").append(getServiceViewStr(autoTaskEntity.getServiceList())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("生效时间：").append(getEffectiveTimeStr(autoTaskEntity.getStartTime(), autoTaskEntity.getEndTime())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("下发时间：").append(getBoldStr(sdf.format(new Date()))).append("<br>");
            sb.append("<br>");
        }
        sb.append("<br><br>").append("<font size=\"2\" color=\"gray\">本邮件属于系统邮件,来源于策略可视化管理平台，请勿回复。</font>").append("<br>");
        String content = sb.toString();

        try {
            //邮件发送
            if (StringUtils.isNotBlank(email)) {
                log.info("开始发送邮件，subject:{},toAddress:{}", subject, email);

                MailServerConfDTO emialDTO = systemParamMapper.findEmailParam(PushConstants.EMAIL_PARAM_GROUP_NAME);
                if (ObjectUtils.isNotEmpty(emialDTO)) {
                    //邮件发送附件
                    CustomMailTool.sendEmail(emialDTO, email, subject, content, null);
                } else {
                    log.error("查询系统邮箱配置，返回空");
                }
            }
        } catch (Exception e) {
            log.error("自动开通下发时，发送邮件告警失败", e);
        }
    }

    /**
     * 发送自动开通下发邮件给管理员
     * @param taskMap
     * @param autoTaskEntityList
     * @param themePerfix
     */
    private void sendAutoRecommendPushEmailManager(Map<String, List<CommandTaskEditableEntity>> taskMap, List<AutoRecommendTaskEntity> autoTaskEntityList, String themePerfix) {
        String email = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL);
        String userReceiveEmail = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL);
        log.info("管理员发送人信息为：{}", email);
        if (StringUtils.isBlank(email) || !AdvancedSettingsConstants.IS_RECEIVE_EMAIL.equals(userReceiveEmail)) {
            return;
        }
        String emailStatus = getEmailStatus(autoTaskEntityList);
        //邮件发送主题
        String subject = "【"+themePerfix+"】自动开通下发邮件告警-" + emailStatus;
        //邮件发送内容
        StringBuilder sb = new StringBuilder();
        sb.append("您好，工单已下发，工单下发详情信息如下").append("<br><br>");
        for (AutoRecommendTaskEntity autoTaskEntity : autoTaskEntityList) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("&nbsp;&nbsp;").append("工单号：").append(getBoldStr(autoTaskEntity.getTheme())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("申请人：").append(getBoldStr(autoTaskEntity.getApplicant())).append("<br>");
            sb.append("&nbsp;&nbsp;").append("访问类型：").append(getBoldStr(PushAccessTypeEnum.getDescByCode(autoTaskEntity.getTaskType()))).append("<br>");
            String color = getTaskStatusColor(autoTaskEntity.getStatus());
            sb.append("&nbsp;&nbsp;").append("状态：").append(getColorStr(AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()), color)).append("<br>");
            sb.append("&nbsp;&nbsp;").append("下发时间：").append(getBoldStr(sdf.format(new Date()))).append("<br>");
            StringBuilder deviceName = new StringBuilder();
            List<CommandTaskEditableEntity> taskEditableEntityList = taskMap.get(autoTaskEntity.getTheme());
            List<String> deviceUuidList = taskEditableEntityList.stream().map(s -> s.getDeviceUuid()).distinct().collect(Collectors.toList());
            deviceUuidList.forEach(t -> {
                NodeEntity node = nodeMapper.getTheNodeByUuid(t);
                deviceName.append(node.getDeviceName()).append(PolicyConstants.ADDRESS_SEPERATOR);
            });
            deviceName.deleteCharAt(deviceName.length()-1);
            sb.append("&nbsp;&nbsp;").append("关联设备信息：").append(getBoldStr(deviceName.toString())).append("<br><br>");


            AutoRecommendTaskVO autoRecommendTaskVO = new AutoRecommendTaskVO();
            autoRecommendTaskVO.setTheme(autoTaskEntity.getTheme());
            ReturnT result = pushAutoRecommendService.getResult(autoRecommendTaskVO);
            AutoRecommendAllResultDTO resultData = (AutoRecommendAllResultDTO) result.getData();
            sb.append("&nbsp;&nbsp;").append(getBoldStr("工单状态")).append("<br>");
            sb.append("&nbsp;&nbsp;").append(getTaskStatusTable(resultData)).append("<br><br>");
            sb.append("&nbsp;&nbsp;").append(getBoldStr("未开通策略")).append("<br>");
            sb.append("&nbsp;&nbsp;").append(getPolicyTable(resultData)).append("<br>");
            sb.append("<br>");
        }

        sb.append("&nbsp;&nbsp;").append("附件为下发策略详情及详细命令行").append("<br>");
        sb.append("<br><br>").append("<font size=\"2\" color=\"gray\">本邮件属于系统邮件,来源于策略可视化管理平台，请勿回复。</font>").append("<br>");
        String content = sb.toString();

        try {
            //邮件发送
            if (StringUtils.isNotBlank(email)) {
                log.info("开始发送邮件，subject:{},toAddress:{}", subject, email);

                MailServerConfDTO emialDTO = systemParamMapper.findEmailParam(PushConstants.EMAIL_PARAM_GROUP_NAME);
                if (ObjectUtils.isNotEmpty(emialDTO)) {
                    //邮件发送附件
                    String emailFile = getAutoRecommendEmailFile(taskMap, autoTaskEntityList);
                    CustomMailTool.sendEmail(emialDTO, email, subject, content, emailFile);
                } else {
                    log.error("查询系统邮箱配置，返回空");
                }
            }
        } catch (Exception e) {
            log.error("自动开通下发时，发送邮件告警失败", e);
        }
    }

    /**
     * 生成自动开通下发邮件附件
     * @param taskMap
     * @param autoEntityList
     * @return
     */
    private String getAutoRecommendEmailFile(Map<String, List<CommandTaskEditableEntity>> taskMap, List<AutoRecommendTaskEntity> autoEntityList) {
        List<String> files = new ArrayList<>();

        String destDirName = dirPath + "/pushAutoEmailExcel";
        String zipFilename = autoEntityList.get(0).getTheme()+ "-Policy proposal" ;
        for (AutoRecommendTaskEntity autoEntity : autoEntityList) {
            List<CommandTaskEditableEntity> list = taskMap.get(autoEntity.getTheme());
            String preFilename = autoEntity.getTheme()+ "-Policy proposal";
            String filePath = destDirName + "/" + preFilename + ".xlsx";

            try {
                // 生成自动开通下发邮件附件文件夹
                if (!new File(destDirName).exists()) {
                    FileUtils.createDir(destDirName);
                }

                OutputStream out = new FileOutputStream(filePath);
                try {
                    List<List<String>> data = new ArrayList<List<String>>();
                    int index = 1;
                    for (CommandTaskEditableEntity task : list) {
                        List<String> rowData = new ArrayList<>();
                        rowData.add(String.valueOf(index));
                        rowData.add(task.getTheme());
                        NodeEntity node = nodeMapper.getTheNodeByUuid(task.getDeviceUuid());

                        rowData.add(node.getIp());
                        rowData.add(node.getDeviceName());
                        rowData.add(getStatusString(task.getPushStatus()));
                        rowData.add(getValidContent(task.getCommandline()));
                        rowData.add(getValidContent(task.getCommandlineEcho()));

                        RecommendTaskEntity taskEntity = recommendTaskMapper.getById(task.getTaskId());
                        rowData.add(getValidContent(taskEntity.getSrcIp()));
                        rowData.add(getValidContent(taskEntity.getDstIp()));
                        rowData.add(getServiceEmailView(taskEntity.getServiceList()));
                        rowData.add(getValidContent(autoEntity.getApplicant()));
                        rowData.add(getValidContent(autoEntity.getApplicantEmail()));
                        data.add(rowData);
                        index++;
                    }

                    String[] headers = { "序号", "工单号", "管理ip", "设备名称", "下发状态", "命令行", "下发结果", "源IP", "目的IP", "服务", "申请人", "邮箱" };
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    ExportExcelUtils.exportPushAutoEmailAddData(workbook, 0, "邮件告警设备信息", headers, data, out, "自动开通下发报表");
                    // 原理就是将所有的数据一起写入，然后再关闭输入流。
                    workbook.write(out);
                } catch (Exception e) {
                    File file = new File(filePath);
                    if(file.exists()) {
                        file.delete();
                    }
                    log.error("下发失败邮件告警excel异常", e);
                } finally {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        log.error("关闭流异常");
                    }
                }

            } catch (Exception e) {
                log.error("自动开通邮件告警Excel表格生成失败:", e);
            }

            files.add(filePath);
        }

        try {
            ZipUtil.writeZipByDir(files, destDirName + "/" + zipFilename);
        } catch (Exception e) {
            log.error("自动开通邮件告警压缩包生成失败:", e);
        }

        String zipFilePath = destDirName + "/" + zipFilename + ".zip";

        return zipFilePath;
    }


    /**
     * 生成即将到期策略邮件附件
     * @param list
     * @return
     */
    private String getWillExpirePolicyFile(List<WillExpirePolicyEmailDTO> list) {
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = list.get(0).getTheme();
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("生成即将过期策略邮件附件文件名称异常", e1);
        }

        String destDirName = dirPath + "/willExpirePolicyExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";

        try {
            // 生成即将过期策略邮件文件夹
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            OutputStream out = new FileOutputStream(filePath);
            try {
                List<List<String>> data = new ArrayList<List<String>>();
                int index = 1;
                for (WillExpirePolicyEmailDTO task : list) {
                    List<CommandTaskEditableEntity> entityList = task.getTaskEditableEntityList();
                    for (CommandTaskEditableEntity entity : entityList) {
                        List<String> rowData = new ArrayList<>();
                        rowData.add(String.valueOf(index));
                        rowData.add(task.getTheme());

                        NodeEntity node = nodeMapper.getTheNodeByUuid(entity.getDeviceUuid());
                        rowData.add(node.getIp());
                        rowData.add(node.getDeviceName());
                        //策略id
                        rowData.add(getDevicePolicyId(entity, node.getModelNumber()));
                        //策略名称
                        //rowData.add(getValidPolicyContent(getPolicyName(entity.getCommandline(), node.getModelNumber())));
                        rowData.add(getIpValidContent(task.getSrcIp()));
                        rowData.add(getIpValidContent(task.getDstIp()));
                        rowData.add(getServiceEmailView(task.getServiceList()));
                        rowData.add(null == task.getEndTime() ? "" : sdf.format(task.getEndTime()));
                        rowData.add(getValidContent(task.getDescription()));
                        rowData.add(getValidContent(task.getApplicant()));
                        rowData.add(getValidContent(task.getApplicantEmail()));
                        data.add(rowData);
                        index++;
                    }
                }

                String[] headers = { "序号", "工单号", "设备ip", "设备名称", "策略ID", "源ip", "目的ip", "服务", "到期时间", "描述", "申请人", "邮箱"};
                XSSFWorkbook workbook = new XSSFWorkbook();
                ExportExcelUtils.exportPushAutoEmailAddData(workbook, 0, "即将过期策略告警信息", headers, data, out, "即将过期策略告警");
                // 原理就是将所有的数据一起写入，然后再关闭输入流。
                workbook.write(out);
            } catch (Exception e) {
                File file = new File(filePath);
                if(file.exists()) {
                    file.delete();
                }
                log.error("即将过期策略邮件告警excel异常", e);
            } finally {
                try {
                    out.close();
                } catch (IOException e1) {
                    log.error("关闭流异常");
                }
            }

        } catch (Exception e) {
            log.error("即将过期策略邮件告警Excel表格生成失败:", e);
        }
        List<String> files = new ArrayList<>();
        files.add(filePath);
        try {
            ZipUtil.writeZipByDir(files, destDirName + "/" + preFilename);
        } catch (Exception e) {
            log.error("即将过期策略邮件告警压缩包生成失败:", e);
        }

        String zipFilePath = destDirName + "/" + preFilename + ".zip";

        return zipFilePath;

    }

    /**
     * 导出excel状态字段调整
     * @param pushStatus
     * @return
     */
    private String getStatusString(int pushStatus){
        if(pushStatus == PushStatusConstans.PUSH_STATUS_PUSHING || pushStatus == PushStatusConstans.PUSH_INT_PUSH_QUEUED ){
            return "下发中";
        }else if(pushStatus == PushStatusConstans.PUSH_STATUS_PART_FINISHED){
            return "下发部分完成";
        }else if(pushStatus == PushStatusConstans.PUSH_STATUS_FAILED){
            return "下发失败";
        }else if(pushStatus == PushStatusConstans.PUSH_STATUS_FINISHED){
            return "下发成功";
        } else {
            return "下发未开始";
        }
    }

    /**
     * 返回有效文本防止报错
     * @param text
     * @return
     */
    private String getValidContent(String text) {
        return null == text ? "" : text;
    }

    /**
     * 返回ip有效文本
     * @param text
     * @return
     */
    private String getIpValidContent(String text) {
        return StringUtils.isEmpty(text) ? PolicyConstants.POLICY_STR_VALUE_ANY : text;
    }

    /**
     * 返回有效文本防止报错
     * @param policyTexts
     * @return
     */
    private String getValidPolicyContent(Set<String> policyTexts) {
        if (ObjectUtils.isEmpty(policyTexts)) {
            return "";
        }
        return String.join(PolicyConstants.ADDRESS_SEPERATOR, policyTexts);
    }

    @Override
    public Set<String> getPolicyName(String commandlime, String modelNumber) {
        if(StringUtils.isEmpty(commandlime)|| StringUtils.isEmpty(modelNumber)){
            return null;
        }
        Set<String> policyNameSet = new HashSet<>();
        String[] commandLines = commandlime.split("\n");
        switch (modelNumber) {
            case DeviceModelNumberConstants.CISCO_MODELNUMBER :
                getRuleName(commandLines, DeviceModelNumberConstants.CISCO_POLICY_NAME_STR,policyNameSet,true);
                break;
            case DeviceModelNumberConstants.DPTECHR004_MODELNUMBER :
                getRuleName(commandLines,DeviceModelNumberConstants.DPTECHR004_POLICY_NAME_STR,policyNameSet,true);
                break;
            case DeviceModelNumberConstants.SRX_MODELNUMBER :
                getRuleName(commandLines,DeviceModelNumberConstants.SRX_POLICY_NAME_STR,policyNameSet,true);
                break;
            case DeviceModelNumberConstants.SSG_MODELNUMBER :
                getRuleName(commandLines,DeviceModelNumberConstants.SSG_POLICY_NAME_STR,policyNameSet,true);
                break;
            case DeviceModelNumberConstants.USG6000_MODELNUMBER :
            case DeviceModelNumberConstants.H3CV7_MODELNUMBER :
                getRuleName(commandLines,DeviceModelNumberConstants.USG6000_POLICY_NAME_STR,policyNameSet,false);
                break;
            case DeviceModelNumberConstants.FORTINET_MODELNUMBER :
            case DeviceModelNumberConstants.FORTINET_MODELNUMBER_V5_2 :
                getRuleName(commandLines,DeviceModelNumberConstants.FORTINET_POLICY_NAME_STR,policyNameSet,false);
                break;
            case DeviceModelNumberConstants.FORTINET_MODELNUMBER_ID :
                getRuleName(commandLines,DeviceModelNumberConstants.FORTINET_POLICY_ID_STR,policyNameSet,false);
                break;
            case DeviceModelNumberConstants.HILLSTONER5_MODELNUMBER :
                getRuleName(commandLines,DeviceModelNumberConstants.HILLSTONER5_POLICY_NAME_STR,policyNameSet,false);
                break;
            case DeviceModelNumberConstants.HILLSTONER5_SECURITY_ID :
                Set<String> policyNameSetZH = new HashSet<>();
                getRuleName(commandLines,DeviceModelNumberConstants.HILLSTONER5_POLICY_SECURITY_ID_STR_ZH,policyNameSetZH,false);
                Set<String> policyNameSetEN = new HashSet<>();
                getRuleName(commandLines,DeviceModelNumberConstants.HILLSTONER5_POLICY_SECURITY_ID_STR,policyNameSetEN,false);
                policyNameSet.addAll(policyNameSetZH);
                policyNameSet.addAll(policyNameSetEN);
                break;
            case DeviceModelNumberConstants.HILLSTONER5_MODELNUMBER_ID :
                getRuleName(commandLines,DeviceModelNumberConstants.HILLSTONER5_POLICY_ID_STR,policyNameSet,false);
                break;
            default:
                return null;

        }
        return policyNameSet;
    }

    /**
     * 根据正则匹配策略名称
     * @param commandLines
     * @param policyNameStr
     * @param policyNameSet
     */
    private void getRuleName(String[] commandLines, String policyNameStr, Set<String> policyNameSet, boolean isSubString ) {
        for (String comStr : commandLines) {
            String ruleName = "";
            Pattern pattern = Pattern.compile( policyNameStr );
            Matcher matcher = pattern.matcher(comStr);
            if (matcher.find()) {
                String ruleLine = StringUtils.substring(comStr,matcher.end()).trim();
                if (isSubString) {
                    ruleName = ruleLine.substring(0,ruleLine.indexOf(" "));
                } else {
                    ruleName = ruleLine;
                }
                if (StringUtils.isNotEmpty(ruleName)) {
                    // 如果是迪普的，要以from_开头
                    if(StringUtils.equals(policyNameStr, DeviceModelNumberConstants.SRX_POLICY_NAME_STR)) {
                        if (StringUtils.isNotEmpty(ruleName) && ruleName.startsWith("from_")) {
                            policyNameSet.add(ruleName);
                        }
                    } else {
                        policyNameSet.add(ruleName);
                    }
                }
            }
        }
    }

    /**
     * 通过自动开通工单信息获取设备信息
     * @param autoTaskEntity
     * @return
     */
    private String getDeviceNameByAutoTaskEntity(AutoRecommendTaskEntity autoTaskEntity) {
        String relevancyNat = autoTaskEntity.getRelevancyNat();
        JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
        List<CommandTaskEditableEntity> taskEditableEntityList = new ArrayList<>();

        try {
            for (int index = 0; index < jsonArray.size(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);

                Integer natId = jsonObject.getInteger("id");

                List<CommandTaskEditableEntity> partList = commandTaskEdiableMapper.selectByTaskId(natId);
                taskEditableEntityList.addAll(partList);
            }
        } catch (Exception e) {
            log.error("自动开通工单任务下发异常，异常原因：", e);
            throw e;
        }

        StringBuilder deviceName = new StringBuilder();
        List<String> deviceUuidList = taskEditableEntityList.stream().map(CommandTaskEditableEntity::getDeviceUuid).distinct().collect(Collectors.toList());
        deviceUuidList.forEach(t -> {
            NodeEntity node = nodeMapper.getTheNodeByUuid(t);
            deviceName.append(node.getDeviceName()).append(PolicyConstants.ADDRESS_SEPERATOR);
        });
        deviceName.deleteCharAt(deviceName.length()-1);
        return deviceName.toString();
    }

    /**
     * 获取加粗字体
     * @param str
     * @return
     */
    private String getBoldStr(String str) {
        return "<font style=\"font-weight: bold;\">" + (StringUtils.isNotEmpty(str) ? str :"") + "</font>";
    }

    /**
     * 获取带有颜色的加粗字体
     * @param str
     * @param color info(#909399),success(#67c23a),warning(#e6a23c),danger(#f56c6c)
     * @return
     */
    private String getBoldStr(String str, String color) {
        return "<font style=\"font-weight: bold;\" "+ "color=\"" + color + "\">"  + (StringUtils.isNotEmpty(str) ? str :"") + "</font>";
    }

    /**
     * 获取带有颜色的字体
     * @param str
     * @param color info(#909399),success(#67c23a),warning(#e6a23c),danger(#f56c6c)
     * @return
     */
    private String getColorStr(String str, String color) {
        return "<font color=\"" + color + "\">"  + (StringUtils.isNotEmpty(str) ? str :"") + "</font>";
    }

    /**
     * 获取服务显示内容
     * @param str
     * @return
     */
    private String getServiceViewStr(String str) {
        if (StringUtils.isEmpty(str)) {
            return getBoldStr(PolicyConstants.POLICY_STR_VALUE_ANY);
        }
        StringBuilder sb = new StringBuilder();
        JSONArray jsonArray = JSONArray.parseArray(str);
        for (int index = 0; index < jsonArray.size(); index++) {
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String protocol = jsonObject.getString("protocol");
            String dstPorts = jsonObject.getString("dstPorts");
            if (PolicyConstants.POLICY_NUM_VALUE_ANY.equals(protocol)) {
                sb.append(PolicyConstants.POLICY_STR_VALUE_ANY);
            }  else if (PolicyConstants.POLICY_NUM_VALUE_TCP.equals(protocol)) {
                sb.append(PolicyConstants.POLICY_STR_VALUE_TCP).append(":").append(dstPorts);
            } else if (PolicyConstants.POLICY_NUM_VALUE_UDP.equals(protocol)) {
                sb.append(PolicyConstants.POLICY_STR_VALUE_UDP).append(":").append(dstPorts);
            } else {
                sb.append(PolicyConstants.POLICY_STR_VALUE_ICMP);
            }
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
        }
        if (sb.length() == 0) {
            return getBoldStr(PolicyConstants.POLICY_STR_VALUE_ANY);
        }
        return getBoldStr(sb.deleteCharAt(sb.length()-1).toString());
    }

    /**
     * 获取生效时间显示
     * @param startTime
     * @param endTime
     * @return
     */
    private String getEffectiveTimeStr(Date startTime, Date endTime) {
        if (startTime == null || endTime == null ) {
            return getBoldStr("永久");
        }
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = sdf.format(startTime);
        String end = sdf.format(endTime);
        sb.append("开始：").append(start).append("\n").append("结束：").append(end);
        return getBoldStr(sb.toString());
    }


    /**
     * 获取工单状态字体颜色
     * @param status
     * @return info(#909399),success(#67c23a),warning(#e6a23c),danger(#f56c6c)
     */
    private String getTaskStatusColor(Integer status) {
        if (status == 0 || status == 3 || status == 4 || status == 5) {
            return "#909399";
        }
        if (status == 1 || status == 6) {
            return "#67c23a";
        }
        if (status == 2 || status == 7) {
            return "#f56c6c";
        }
        if (status == 8 || status == 9 || status == 10) {
            return "#e6a23c";
        }

        return "#409eff";
    }

    /**
     * 获取邮件工单下发状态
     * @param autoTaskEntityList
     * @return
     */
    private String getEmailStatus(List<AutoRecommendTaskEntity> autoTaskEntityList) {
        List<Integer> statusList = autoTaskEntityList.stream().map(AutoRecommendTaskEntity::getStatus).distinct().collect(Collectors.toList());

        if (statusList.size() == 1) {
            //如果只有一种状态则下发状态为该状态
            return AutoRecommendStatusEnum.getDescByCode(statusList.get(0));
        }

        return AutoRecommendStatusEnum.PUSH_SUCCESS_PARTS.getDesc();
    }

    /**
     * 获取工单状态表格
     * @param resultData
     * @return
     */
    private String getTaskStatusTable(AutoRecommendAllResultDTO resultData) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table id=\"tfhover\" class=\"tftable\" style=\"table-layout:fixed;\" border= \"1\" rules=rows>");
        sb.append("<tr><th width = \"30%\" align = \"center\">").append(getBoldStr("源地址")).append("</th>");
        sb.append("<th width = \"30%\" align = \"center\">").append(getBoldStr("目的地址")).append("</th>");
        sb.append("<th width = \"10%\" align = \"center\">").append(getBoldStr("状态")).append("</th>");
        sb.append("<th width = \"30%\" align = \"center\">").append(getBoldStr("设备信息")).append("</th></tr>");

        List<AutoRecommendOrderStatusDTO> orderStatusDTOList = resultData.getOrderStatusDTOList();

        if (CollectionUtils.isNotEmpty(orderStatusDTOList)) {
            for (AutoRecommendOrderStatusDTO orderStatusDTO : orderStatusDTOList) {
                if (CollectionUtils.isEmpty(orderStatusDTO.getSrcIpList()) && CollectionUtils.isEmpty(orderStatusDTO.getDstIpList()) && CollectionUtils.isEmpty(orderStatusDTO.getEntityList())) {
                    continue;
                }
                String srcIp = "";
                if (CollectionUtils.isNotEmpty(orderStatusDTO.getSrcIpList())) {
                    srcIp = orderStatusDTO.getSrcIpList().stream().collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
                }
                sb.append("<tr><td width = \"30%\" align = \"center\" style=\"word-wrap:break-word\">").append(srcIp).append("</td>");
                String dstIp = "";
                if (CollectionUtils.isNotEmpty(orderStatusDTO.getDstIpList())) {
                    dstIp = orderStatusDTO.getDstIpList().stream().collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
                }
                sb.append("<td width = \"30%\" align = \"center\" style=\"word-wrap:break-word\">").append(dstIp).append("</td>");
                String status = AutoTaskOrderStatusEnum.getDescByCode(orderStatusDTO.getOrderStatus());

                String color = "#e6a23c";
                if (AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode().equals(orderStatusDTO.getOrderStatus())) {
                    color = "#67c23a";
                } else if (AutoTaskOrderStatusEnum.POLICY_GENERATE_ERROR.getCode().equals(orderStatusDTO.getOrderStatus())) {
                    color = "#f56c6c";
                }
                sb.append("<td width = \"10%\" align = \"center\" style=\"word-wrap:break-word\">").append(getColorStr(status, color)).append("</td>");
                String deviceName = "";
                if (CollectionUtils.isNotEmpty(orderStatusDTO.getEntityList())) {
                    deviceName = orderStatusDTO.getEntityList().stream().map(NodeEntity :: getDeviceName).collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
                }
                sb.append("<td width = \"30%\" align = \"center\" style=\"word-wrap:break-word\">").append(deviceName).append("</td></tr>");

            }
        }

        sb.append("</table>");
        log.info(sb.toString());
        return "<font size=\"2\">"+ sb.toString() +"</font>";
    }

    /**
     * 获取未开通策略表格
     * @param resultData
     * @return
     */
    private String getPolicyTable(AutoRecommendAllResultDTO resultData) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table id=\"tfhover\" class=\"tftable\" style=\"table-layout:fixed;\" border= \"1\" rules=rows>");
        sb.append("<tr><th width = \"10%\" align = \"center\" style = \"min-width:80px\">").append(getBoldStr("防火墙名称")).append("</th>");
        sb.append("<th width = \"7%\" align = \"center\" style = \"min-width:60px\">").append(getBoldStr("防火墙IP")).append("</th>");
        sb.append("<th width = \"5%\" align = \"center\" style = \"min-width:50px\">").append(getBoldStr("源域")).append("</th>");
        sb.append("<th width = \"7%\" align = \"center\" style = \"min-width:60px\">").append(getBoldStr("源IP")).append("</th>");
        sb.append("<th width = \"5%\" align = \"center\" style = \"min-width:50px\">").append(getBoldStr("目的域")).append("</th>");
        sb.append("<th width = \"7%\" align = \"center\" style = \"min-width:60px\">").append(getBoldStr("目的IP")).append("</th>");
        sb.append("<th width = \"7%\" align = \"center\" style = \"min-width:60px\">").append(getBoldStr("服务")).append("</th>");
        sb.append("<th width = \"5%\" align = \"center\" style = \"min-width:50px\">").append(getBoldStr("转换后IP")).append("</th>");
        sb.append("<th width = \"7%\" align = \"center\" style = \"min-width:70px\">").append(getBoldStr("生效时间")).append("</th>");
        sb.append("<th width = \"10%\" align = \"center\" style = \"min-width:80px\">").append(getBoldStr("状态")).append("</th>");
        sb.append("<th width = \"30%\" align = \"center\" style = \"min-width:200px\">").append(getBoldStr("错误原因")).append("</th></tr>");

        List<AutoRecommendErrorDetailDTO> errorDetailDTOList = resultData.getErrorDetailDTOList();

        if (CollectionUtils.isNotEmpty(errorDetailDTOList)) {
            for (AutoRecommendErrorDetailDTO errorDetailDTO : errorDetailDTOList) {
                String deviceName = StringUtils.isEmpty(errorDetailDTO.getDeviceName()) ? "" : errorDetailDTO.getDeviceName();
                sb.append("<tr><td width = \"10%\" align = \"center\" style=\"word-wrap:break-word\">").append(deviceName).append("</td>");
                String deviceIp = StringUtils.isEmpty(errorDetailDTO.getDeviceIp()) ? "" : errorDetailDTO.getDeviceIp();
                sb.append("<td width = \"7%\" align = \"center\" style=\"word-wrap:break-word\">").append(deviceIp).append("</td>");
                String srcZone = StringUtils.isEmpty(errorDetailDTO.getSrcZone()) ? "" : errorDetailDTO.getSrcZone();
                sb.append("<td width = \"5%\" align = \"center\" style=\"word-wrap:break-word\">").append(srcZone).append("</td>");
                String srcIp = StringUtils.isEmpty(errorDetailDTO.getSrcIp()) ? "" : errorDetailDTO.getSrcIp();
                sb.append("<td width = \"7%\" align = \"center\" style=\"word-wrap:break-word\">").append(srcIp).append("</td>");
                String dstZone = StringUtils.isEmpty(errorDetailDTO.getDstZone()) ? "" : errorDetailDTO.getDstZone();
                sb.append("<td width = \"5%\" align = \"center\" style=\"word-wrap:break-word\">").append(dstZone).append("</td>");
                String dstIp = StringUtils.isEmpty(errorDetailDTO.getDstIp()) ? "" : errorDetailDTO.getDstIp();
                sb.append("<td width = \"7%\" align = \"center\" style=\"word-wrap:break-word\">").append(dstIp).append("</td>");
                List<ServiceDTO> serviceList = errorDetailDTO.getServiceList();
                String service = "";
                if (CollectionUtils.isNotEmpty(serviceList)) {
                    service = getServiceEmailView(JSONArray.toJSONString(serviceList));
                }
                sb.append("<td width = \"7%\" align = \"center\" style=\"word-wrap:break-word\">").append(service).append("</td>");
                String postIpAddress = StringUtils.isEmpty(errorDetailDTO.getPostIpAddress()) ? "" : errorDetailDTO.getPostIpAddress();
                sb.append("<td width = \"5%\" align = \"center\" style=\"word-wrap:break-word\">").append(postIpAddress).append("</td>");
                Date startTime = errorDetailDTO.getStartTime();
                Date endTime = errorDetailDTO.getEndTime();
                sb.append("<td width = \"7%\" align = \"center\" style=\"word-wrap:break-word\">").append(getEffectiveTimeStr(startTime, endTime)).append("</td>");
                Integer status = errorDetailDTO.getStatus();
                String color = getTaskStatusColor(status);
                sb.append("<td width = \"10%\" align = \"center\" style=\"word-wrap:break-word\">").append(getColorStr(AutoRecommendStatusEnum.getDescByCode(status), color)).append("</td>");
                String errorMsg = StringUtils.isEmpty(errorDetailDTO.getErrorMsg()) ? "" : errorDetailDTO.getErrorMsg();
                sb.append("<td width = \"30%\" align = \"center\" style=\"word-wrap:break-word\">").append(getColorStr(errorMsg, "#f56c6c")).append("</td></tr>");
            }
        }

        sb.append("</table>");
        log.info(sb.toString());
        return "<font size=\"1\">"+ sb.toString() +"</font>";
    }

    /**
     * 获取策略id
     * @param entity
     * @param modelNumber
     * @return
     */
    private String getDevicePolicyId(CommandTaskEditableEntity entity, String modelNumber) {
        String commandline = entity.getCommandline();
        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        Integer taskType = entity.getTaskType();
        switch (deviceModelNumberEnum) {
            case H3CV7:
                return getValidPolicyContent(getPolicyName(commandline, DeviceModelNumberConstants.H3CV7_MODELNUMBER));
            case HILLSTONE:
            case HILLSTONE_R5:
                //山石设备安全设备
                commandline = entity.getCommandlineEcho();
                String matchNumber = DeviceModelNumberConstants.HILLSTONER5_SECURITY_ID;

                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType
                        || PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT ==taskType) {
                    matchNumber = DeviceModelNumberConstants.HILLSTONER5_MODELNUMBER_ID;
                }
                return getHillStonePolicyId(getPolicyName(commandline, matchNumber));
            case FORTINET:
                return getValidPolicyContent(getPolicyName(commandline, DeviceModelNumberConstants.FORTINET_MODELNUMBER_ID));
            case DPTECHR004:
                return getValidPolicyContent(getPolicyName(commandline, DeviceModelNumberConstants.DPTECHR004_MODELNUMBER));
            default:
                return null;
        }
    }


    /**
     * 获取飞塔的id
     * @param policyNameList
     * @return
     */
    private String getHillStonePolicyId(Set<String> policyNameList) {
        Set<String> validSet = new HashSet<>();
        if (ObjectUtils.isNotEmpty(policyNameList)) {
            if (ObjectUtils.isNotEmpty(policyNameList)) {
                for (String name : policyNameList) {
                    String policyId = getHillstoneSecurityPolicyId(name);
                    if (StringUtils.isNotEmpty(policyId)) {
                        validSet.add(policyId);
                    }
                }
            }
        }
        return getValidPolicyContent(validSet);
    }

    /**
     * 从山石安全策略回显中抓取策略id
     * @param name
     * @return
     */
    public String getHillstoneSecurityPolicyId(String name) {
        if (!name.contains(" ")) {
            return name;
        }

        String[] names = name.split(" ");
        String str = "";
        for (String id : names) {
            if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(id)) {
                str = id;
                break;
            }
        }
        return str;
    }


    /**
     * 获取邮件服务显示信息
     * @param serviceList
     * @return
     */
    private String getServiceEmailView(String serviceList) {
        if (StringUtils.isEmpty(serviceList)) {
            return PolicyConstants.POLICY_STR_VALUE_ANY;
        }

        JSONArray array = JSONArray.parseArray(serviceList);
        List<ServiceDTO> services = array.toJavaList(ServiceDTO.class);
        StringBuilder reStr = new StringBuilder();
        for (ServiceDTO service : services) {
            if (PolicyConstants.POLICY_NUM_VALUE_ANY.equals(service.getProtocol())) {

                reStr.append(PolicyConstants.POLICY_STR_VALUE_ANY).append(PolicyConstants.ADDRESS_SEPERATOR);
            } else if (PolicyConstants.POLICY_NUM_VALUE_ICMP.equals(service.getProtocol()) ) {

                reStr.append(PolicyConstants.POLICY_STR_VALUE_ICMP).append(PolicyConstants.ADDRESS_SEPERATOR);
            } else if (PolicyConstants.POLICY_NUM_VALUE_TCP.equals(service.getProtocol())) {

                reStr.append(PolicyConstants.POLICY_STR_VALUE_TCP).append(":").append(service.getDstPorts()).append(PolicyConstants.ADDRESS_SEPERATOR);
            } else if (PolicyConstants.POLICY_NUM_VALUE_UDP.equals(service.getProtocol())) {

                reStr.append(PolicyConstants.POLICY_STR_VALUE_UDP).append(":").append(service.getDstPorts()).append(PolicyConstants.ADDRESS_SEPERATOR);
            }
        }

        if (reStr.length() > 1) {
            reStr.deleteCharAt(reStr.length()-1);
        }
        return reStr.toString();
    }



    public static void main(String[] args) {
        /*String comStr= "";
        PushAutoTaskEmailServiceImpl taskEmail = new PushAutoTaskEmailServiceImpl();
        Set<String> policyName = taskEmail.getPolicyName(comStr, DeviceModelNumberConstants.HILLSTONER5_SECURITY_ID);

        System.out.println(policyName);
        System.out.println(taskEmail.getValidPolicyContent(policyName));*/
        PushAutoTaskEmailServiceImpl taskEmail1 = new PushAutoTaskEmailServiceImpl();
        String str = taskEmail1.getBoldStr("张三", "#67c23a");
        System.out.println(str);
        Set<String> policyNameSet = new HashSet<>();
        Set<String> policyNameSetZH = new HashSet<>();
        policyNameSetZH.add("aa");
        Set<String> policyNameSetEN = new HashSet<>();
        policyNameSet.addAll(policyNameSetZH);
        policyNameSet.addAll(policyNameSetEN);
        System.out.println(policyNameSet);
        PushAutoTaskEmailServiceImpl taskEmail = new PushAutoTaskEmailServiceImpl();
        String serviceList = "[{\"dstPorts\":\"any\",\"protocol\":\"0\",\"srcPorts\":\"any\"}]";
        String serviceEmailView = taskEmail.getServiceEmailView(serviceList);

        System.out.println(serviceEmailView);
    }

}
