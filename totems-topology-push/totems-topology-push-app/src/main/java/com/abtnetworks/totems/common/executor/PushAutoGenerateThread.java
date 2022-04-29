package com.abtnetworks.totems.common.executor;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.enums.AutoRecommendStatusEnum;
import com.abtnetworks.totems.auto.enums.InputTypeEnum;
import com.abtnetworks.totems.auto.enums.PushAccessTypeEnum;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class PushAutoGenerateThread extends Thread {
    private String filePath;
    private File doingFile;
    private Authentication authentication;
    private PushAutoRecommendService pushAutoRecommendService;
    private List<AutoRecommendTaskEntity> taskList;

    public PushAutoGenerateThread(String filePath, File doingFile, Authentication authentication, PushAutoRecommendService pushAutoRecommendService, List<AutoRecommendTaskEntity> taskList) {
        this.filePath = filePath;
        this.doingFile = doingFile;
        this.authentication = authentication;
        this.pushAutoRecommendService = pushAutoRecommendService;
        this.taskList = taskList;
    }

    public PushAutoGenerateThread() {
    }

    @Override
    public void run() {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            List<List<String>> data = new ArrayList<List<String>>();
            int index = 1;
            // 传过来查出来的所有数据
            for (AutoRecommendTaskEntity task : taskList) {
                String relevancyNat = task.getRelevancyNat();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(relevancyNat)) {
                    List<String> rowData = setData(index, task);
                    // 邮箱
                    JSONArray natList = JSON.parseArray(relevancyNat);
                    StringBuilder securityPolicy = new StringBuilder();
                    StringBuilder srcNatPolicy = new StringBuilder();
                    StringBuilder dstNatPolicy = new StringBuilder();
                    for (int i = 0; i < natList.size(); i++) {
                        JSONObject json = natList.getJSONObject(i);
                        int taskType = json.getInteger("taskType");
                        boolean notLast = true;
                        if (i == natList.size()-1){
                            notLast = false;
                        }
                        if (taskType == 3) {
                            securityPolicy.append(json.getString("name"));
                            if (notLast){
                                securityPolicy.append(";");
                            }
                        } else if (taskType == 6) {
                            srcNatPolicy.append(json.getString("name"));
                            if (notLast){
                                srcNatPolicy.append(";");
                            }
                        } else if (taskType == 7) {
                            dstNatPolicy.append(json.getString("name"));
                            if (notLast){
                                dstNatPolicy.append(";");
                            }
                        }
                    }
                    rowData.add(securityPolicy.toString());
                    // 源策略
                    rowData.add(srcNatPolicy.toString());
                    // 目的NAT策略
                    rowData.add(dstNatPolicy.toString());

                    data.add(rowData);
                    index++;
                } else {
                    List<String> rowData = setData(index, task);
                    data.add(rowData);
                    index++;
                }
            }
            String[] headers = {"序号", "工单号", "访问类型", "源地址", "目的地址", "服务", "状态", "邮箱", "申请人",
                    "描述", "生效时间", "创建时间", "安全策略", "源NAT策略", "目的NAT策略"};
            ExportExcelUtils eeu = new ExportExcelUtils();
            XSSFWorkbook workbook = new XSSFWorkbook();
            eeu.exportAutoPushCommonData(workbook, 0, "自动开通列表", headers, data, out);
            workbook.write(out);
        } catch (Exception e) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            log.error("自动开通导出excel异常", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
                log.error("关闭流异常");
            }
        }
        doingFile.delete();
    }

    /**
     * 抽取Set字段值的公共方法
     * @param index     序号
     * @param task      数据实体
     * @return          表数据集合
     */
    private List<String> setData(int index, AutoRecommendTaskEntity task) {
        List<String> rowData = new ArrayList<>();
        rowData.add(index + "");
        rowData.add(task.getTheme());
        // 访问类型（0：内网互访；1：内网访问互联网；2：互联网访问内网）
        rowData.add(getTaskTypeString(task.getAccessType()));
        if(InputTypeEnum.SRC_INPUT_TYPE_IP.getCode() == task.getSrcInputType().intValue()){
            rowData.add(task.getSrcIp());
        }else {
            rowData.add(task.getSrcAddressObjectName());
        }
        if(InputTypeEnum.DST_INPUT_TYPE_IP.getCode() == task.getDstInputType().intValue()){
            rowData.add(task.getDstIp());
        }else {
            rowData.add(task.getDstAddressObjectName());
        }
        rowData.add(getServiceString(task.getServiceList()));
        // 状态
        rowData.add(getTaskStatus(task.getStatus()));
        // 邮箱
        rowData.add(task.getApplicantEmail());
        // 申请人
        rowData.add(task.getUserName());
        // 申请描述
        rowData.add(task.getDescription());
        if (task.getStartTime() == null) {
            rowData.add("");
        } else {
            rowData.add(getTimeString(task.getStartTime()) + "-" + getTimeString(task.getEndTime()));
        }
        rowData.add(getTimeString(task.getCreateTime()));
        return rowData;
    }

    /**
     * 导出-获取服务
     * @param service     导出服务格式化
     * @return            服务字符串（格式：tcp:10）
     */
    protected String getServiceString(String service){
        StringBuilder serviceSb = new StringBuilder();
        if(org.apache.commons.lang3.StringUtils.isBlank(service)){
            return serviceSb.toString();
        }else {
            JSONArray jsonArray = JSON.parseArray(service);
            for(int i= 0 ; i< jsonArray.size(); i++ ){
                JSONObject json = jsonArray.getJSONObject(i);
                String protocolStr = json.getString("protocol");
                String protocol = getProtocolType(Integer.parseInt(protocolStr));
                String dstPorts = json.getString("dstPorts");
                if(org.apache.commons.lang3.StringUtils.equalsIgnoreCase("any", protocol)){
                    serviceSb.append(protocol);
                    continue;
                }
                if(org.apache.commons.lang3.StringUtils.isBlank(protocol)){
                    serviceSb.append(dstPorts).append(" \n");
                }else if(org.apache.commons.lang3.StringUtils.isBlank(dstPorts)){
                    serviceSb.append(protocol).append(" \n");
                }else {
                    serviceSb.append(protocol).append(":").append(dstPorts).append(" \n");
                }
            }if (org.apache.commons.lang3.StringUtils.isBlank(serviceSb.toString())) {
                return "";
            }
            return serviceSb.toString();
        }
    }

    /**
     * 根据协议类型后去对应的字符串协议
     * @param protocol      协议类型
     * @return              any、icmp、tcp、udp、icmp6、未知类型
     */
    private String getProtocolType(Integer protocol) {
        switch (protocol) {
            case 0:
                return ProtocolTypeEnum.ANY.getType();
            case 1:
                return ProtocolTypeEnum.ICMP.getType();
            case 6:
                return ProtocolTypeEnum.TCP.getType();
            case 17:
                return ProtocolTypeEnum.UDP.getType();
            case 58:
                return ProtocolTypeEnum.ICMP6.getType();
            case 200:
                return ProtocolTypeEnum.PROTOCOL.getType();
            default:
                return "未知类型";
        }
    }

    /**
     * 格式化时间
     * @param time  数据库的时间字段
     * @return      2021-12-09 11:11:11
     */
    protected String getTimeString(Date time) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    /**
     * 导出excel状态字段调整
     *
     * @param taskType  访问类型
     * @return          访问类型字符串
     */
    protected String getTaskTypeString(Integer taskType) {
        switch (taskType) {
            case 0:
                return PushAccessTypeEnum.INSIDE_TO_INSIDE.getDesc();
            case 1:
                return PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getDesc();
            case 2:
                return PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getDesc();
            default:
                return "未知类型";
        }
    }


    /**
     * 获取对应状态
     * @param status    状态类型
     * @return          状态类型字符串
     */
    private String getTaskStatus(Integer status) {
        switch (status) {
            case 0:
                return AutoRecommendStatusEnum.GENERATE_COMMANDLINE_NOT_START.getDesc();
            case 1:
                return AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getDesc();
            case 2:
                return AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getDesc();
            case 3:
                return AutoRecommendStatusEnum.PUSH_NOT_START.getDesc();
            case 4:
                return AutoRecommendStatusEnum.PUSH_WAITING.getDesc();
            case 5:
                return AutoRecommendStatusEnum.PUSHING.getDesc();
            case 6:
                return AutoRecommendStatusEnum.PUSH_SUCCESS.getDesc();
            case 7:
                return AutoRecommendStatusEnum.PUSH_FAIL.getDesc();
            case 8:
                return AutoRecommendStatusEnum.PUSH_SUCCESS_PARTS.getDesc();
            case 9:
                return AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getDesc();
            case 10:
                return AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getDesc();
            case 11:
                return AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS_PARTS.getDesc();
            case 12:
                return AutoRecommendStatusEnum.GENERATING_COMMANDLINE.getDesc();
            default:
                return "未知状态";
        }
    }
}
