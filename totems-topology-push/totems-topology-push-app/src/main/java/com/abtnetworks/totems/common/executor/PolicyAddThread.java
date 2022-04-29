package com.abtnetworks.totems.common.executor;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.RecommendRelevanceSceneService;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 策略开通线程启动类,用于生成excel
 * @date 2021/1/14
 */
@Slf4j
@Service
public class PolicyAddThread extends Thread {

    private String filePath;
    private File doingFile;
    private List<RecommendTaskEntity> taskList;
    private Authentication authentication;
    private RecommendTaskManager policyRecommendTaskService;
    private RecommendRelevanceSceneService recommendRelevanceSceneService;

    public PolicyAddThread(String filePath, File doingFile, List<RecommendTaskEntity> taskList,Authentication authentication,RecommendTaskManager policyRecommendTaskService,RecommendRelevanceSceneService recommendRelevanceSceneService) {
        super();
        this.filePath = filePath;
        this.doingFile = doingFile;
        this.taskList = taskList;
        this.authentication = authentication;
        this.policyRecommendTaskService = policyRecommendTaskService;
        this.recommendRelevanceSceneService = recommendRelevanceSceneService;
    }

    public PolicyAddThread() {
        super();
    }

    @Override
    public void run() {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            List<List<String>> data = new ArrayList<List<String>>();
            int index = 1;
            for (RecommendTaskEntity task : taskList) {
                String relevancyNat = task.getRelevancyNat();
                if(org.apache.commons.lang3.StringUtils.isNotBlank(relevancyNat)){
                    JSONArray natList = JSONArray.parseArray(relevancyNat);
                    for(int i = 0; i< natList.size();i ++){
                        List<String> rowData = new ArrayList<>();
                        JSONObject json = natList.getJSONObject(i);

                        Integer id = json.getInteger("id");
                        Integer natType = json.getInteger("type");
                        if(null == id){
                            continue;
                        }

                        PolicyTaskDetailVO natDetail = null;
                        if (PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT != natType) {
                            PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getNatPolicyTaskList(null, null, 1, 20,null,id,null,null,null,authentication);
                            natDetail = pageInfo.getList().get(0);
                            if (null == natDetail) {
                                continue;
                            }
                        } else {
                            // 如果是特殊Nat 比如 飞塔NAT
                            natDetail = getNatDetailVO(id);
                            if (natDetail == null) {
                                continue;
                            }
                        }

                        rowData.add(index+"");
                        rowData.add(task.getOrderNumber());
                        rowData.add(task.getTheme());
                        rowData.add(getStatusString(task.getStatus()));
                        rowData.add(getPathAnalyzeStatusString(task.getPathAnalyzeStatus()));
                        rowData.add(task.getSrcIp());
                        rowData.add(task.getSrcIpSystem());
                        rowData.add(task.getDstIp());
                        rowData.add(task.getDstIpSystem());
                        rowData.add(getServiceString(task.getServiceList()));
                        rowData.add(task.getIdleTimeout() == null ? "":task.getIdleTimeout().toString());
                        if(task.getStartTime() == null ){
                            rowData.add("");
                        } else {
                            rowData.add(getTimeString(task.getStartTime())+"-"+getTimeString(task.getEndTime()));
                        }
                        rowData.add(task.getUserName());
                        rowData.add(getTimeString(task.getCreateTime()));
                        // 开通类型","起点标签","标签模式
                        rowData.add(getTaskTypeString(task.getTaskType()));
                        rowData.add(task.getStartLabel());
                        rowData.add(task.getLabelModel());
                        rowData.add(task.getDescription());
                        rowData.add(task.getRemarks());

                        // nat数据
                        rowData.add(natDetail.getPolicyName());
                        rowData.add(natDetail.getDeviceIp());
                        rowData.add(natDetail.getSrcDomain());
                        rowData.add(natDetail.getDstDomain());
                        rowData.add(natDetail.getPreSrcIp());
                        rowData.add(natDetail.getPostSrcIp());
                        rowData.add(natDetail.getDstIp());
                        rowData.add(natDetail.getPostDstIp());
                        rowData.add(getServiceString(natDetail.getService()));
                        rowData.add(getServiceString(natDetail.getPostService()));

                        if(natDetail.getCreateTime() == null ){
                            rowData.add("");
                        } else {
                            rowData.add(getTimeString(natDetail.getCreateTime()));
                        }
                        rowData.add(natDetail.getUserName());
                        data.add(rowData);
                        index++;
                    }
                } else {
                    List<String> rowData = new ArrayList<>();
                    rowData.add(String.valueOf(index));
                    rowData.add(task.getOrderNumber());
                    rowData.add(task.getTheme());
                    rowData.add(getStatusString(task.getStatus()));
                    rowData.add(getPathAnalyzeStatusString(task.getPathAnalyzeStatus()));
                    rowData.add(task.getSrcIp());
                    rowData.add(task.getSrcIpSystem());
                    rowData.add(task.getDstIp());
                    rowData.add(task.getDstIpSystem());
                    rowData.add(getServiceString(task.getServiceList()));
                    rowData.add(task.getIdleTimeout() == null ? "":task.getIdleTimeout().toString());
                    if(task.getStartTime() == null ){
                        rowData.add("");
                    } else {
                        rowData.add(getTimeString(task.getStartTime())+"-"+getTimeString(task.getEndTime()));
                    }
                    rowData.add(task.getUserName());

                    if(task.getCreateTime() == null ){
                        rowData.add("");
                    } else {
                        rowData.add(getTimeString(task.getCreateTime()));
                    }
                    // 开通类型","起点标签","标签模式
                    rowData.add(getTaskTypeString(task.getTaskType()));
                    rowData.add(task.getStartLabel());
                    rowData.add(task.getLabelModel());
                    rowData.add(task.getDescription());
                    rowData.add(task.getRemarks());
                    data.add(rowData);
                    index++;
                }
            }
            String[] headers = { "序号","流水号", "主题（工单号）", "任务状态","路径分析状态", "源地址", "源地址对象名", "目的地址", "目的地址对象名", "服务", "长连接（秒）", "生效时间","申请人","创建时间","开通类型","起点标签","标签模式","策略描述","工单备注","仿真NAT(主题工单号)",
                    "仿真NAT（设备）", "仿真NAT（源域/接口）", "仿真NAT（目的域/接口）","仿真NAT（源地址-转换前）", "仿真NAT（源地址-转换后）", "仿真NAT（目的地址）"," 仿真NAT（目的地址-转换后）", "仿真NAT（服务）","仿真NAT（服务-转换后）","仿真NAT（创建时间）", "仿真NAT（创建人）"};
            ExportExcelUtils eeu = new ExportExcelUtils();
            XSSFWorkbook workbook = new XSSFWorkbook();
            eeu.exportPolicyAddData(workbook, 0, "策略开通列表", headers, data, out);
            // 原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
        } catch (Exception e) {
            File file = new File(filePath);
            if(file.exists()) {
                file.delete();
            }
            log.error("策略开通导出excel异常", e);
        }finally {
            try{
                if(out != null) {
                    out.close();
                }
            }catch (IOException e1){
                log.error("关闭流异常");
            }
        }
        doingFile.delete();
    }

    /**
     * 获取特殊nat详情
     * @param id
     * @return
     */
    private PolicyTaskDetailVO getNatDetailVO(int id) {
        PolicyTaskDetailVO natDetail = new PolicyTaskDetailVO();
        RecommendRelevanceSceneDTO recommendRelevanceSceneDTO = recommendRelevanceSceneService.queryById(id);
        if(null == recommendRelevanceSceneDTO){
            return null;
        }
        natDetail.setPolicyName(recommendRelevanceSceneDTO.getName());
        natDetail.setDeviceIp(recommendRelevanceSceneDTO.getDeviceIp());
        natDetail.setSrcDomain(recommendRelevanceSceneDTO.getSrcDomain());
        natDetail.setDstDomain(recommendRelevanceSceneDTO.getDstDomain());
        natDetail.setPreSrcIp(recommendRelevanceSceneDTO.getPreSrcIp());
        natDetail.setPostSrcIp(recommendRelevanceSceneDTO.getPostSrcIp());
        natDetail.setDstIp(recommendRelevanceSceneDTO.getPreDstIp());
        natDetail.setService(recommendRelevanceSceneDTO.getServiceListJson());
        natDetail.setPostService(recommendRelevanceSceneDTO.getPostService());
        natDetail.setCreateTime(recommendRelevanceSceneDTO.getCreateTime());
        natDetail.setUserName(recommendRelevanceSceneDTO.getCreateUser());
        return natDetail;
    }

    /**
     * 导出excel状态字段调整
     * @param status
     * @return
     */
    protected String getStatusString(Integer status){
        switch (status.intValue()) {
            case 0 :
                return "仿真未开始";
            case 1 :
                return "仿真等待中";
            case 2 :
                return "仿真执行中";
            case 9 :
                return "仿真失败";
            case 10 :
                return "仿真完成";
            case 13 :
                return "下发部分完成";
            case 19 :
                return "下发失败";
            case 20:
                return "下发成功";
            case 21:
                return "验证中";
            case 22:
                return  "验证等待中";
            case 29:
                return  "验证失败";
            case 30:
                return "验证完成";
            default:
                return String.valueOf(status);
        }
    }

    /**
     * 导出excel路径状态字段调整
     *
     * @param status
     * @return
     */
    protected String getPathAnalyzeStatusString(String status) {
        if (StringUtils.isBlank(status)) {
            return "未执行分析";
        }
        StringBuffer sb = new StringBuffer();
        String[] pathAnalyzeStatus = status.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String itemStatus : pathAnalyzeStatus) {
            String[] item = itemStatus.split(":");
            switch (item[0]) {
                case "0":
                    sb.append(String.format("未开始(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "1":
                    sb.append(String.format("业务未开通(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "2":
                    sb.append(String.format("执行异常(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "3":
                    sb.append(String.format("路径不存在(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "4":
                    sb.append(String.format("已开通(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "5":
                    sb.append(String.format("存在多路径(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "6":
                    sb.append(String.format("通路已存在,策略具有时间对象(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "7":
                    sb.append(String.format("未匹配到源子网(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "8":
                    sb.append(String.format("源地址与目的地址子网不能相同(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "9":
                    sb.append(String.format("未找到可达路径(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "10":
                    sb.append(String.format("长连接未放通(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "11":
                    sb.append(String.format("无路径(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                case "12":
                    sb.append(String.format("源目的同网段(%s)", item.length > 1 ? item[1] : "1")).append(StringUtils.CR);
                    break;
                default:
                    sb.append(itemStatus);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 导出excel状态字段调整
     * @param taskType
     * @return
     */
    protected String getTaskTypeString(Integer taskType){
        switch (taskType.intValue()) {
            case 1 :
                return "业务开通";
            case 8 :
                return "互联网开通-由内到外";
            case 14 :
                return "互联网开通-由外到内";
            case 15 :
                return "大网段开通";
            default:
                return String.valueOf(taskType);
        }
    }

    /**
     * 导出-获取服务
     * @param service
     * @return
     */
    protected String getServiceString(String service){
        StringBuilder serviceSb = new StringBuilder();
        if(org.apache.commons.lang3.StringUtils.isBlank(service)){
            return serviceSb.toString();
        }else {
            JSONArray jsonArray = JSONArray.parseArray(service);
            for(int i= 0 ; i< jsonArray.size(); i++ ){
                JSONObject json = jsonArray.getJSONObject(i);
                String protocol = json.getString("protocol");
                String dstPorts = json.getString("dstPorts");
                if (org.apache.commons.lang3.StringUtils.isBlank(protocol)) {
                    continue;
                }
                if(org.apache.commons.lang3.StringUtils.equalsIgnoreCase("any", protocol)){
                    serviceSb.append(protocol);
                    continue;
                }
                if(org.apache.commons.lang3.StringUtils.isBlank(dstPorts)){
                    serviceSb.append(protocol).append(" \n");
                } else {
                    serviceSb.append(protocol).append(":").append(dstPorts).append(" \n");
                }
            }
            return serviceSb.toString();
        }
    }



    protected String getTimeString(Date time){
        if(time == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = sdf.format(time);
        return timeString;
    }
}
