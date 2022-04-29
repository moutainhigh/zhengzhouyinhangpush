package com.abtnetworks.totems.common.executor;

import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author liuben
 * @Title:
 * @Description: 策略生成线程启动类,用于生成excel
 * @date 2021/1/14
 */
@Slf4j
@Service
public class PolicyGenerateThread extends Thread {

    @Autowired
    RecommendTaskManager policyRecommendTaskService;


    private String filePath;
    private File doingFile;
    private List<PolicyTaskDetailVO> taskList[];

    public PolicyGenerateThread(String filePath, File doingFile, List<PolicyTaskDetailVO> taskList[]) {
        super();
        this.filePath = filePath;
        this.doingFile = doingFile;
        this.taskList = taskList;
    }

    public PolicyGenerateThread() {
        super();
    }

    @Override
    public void run() {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            List<List<String>> data = new ArrayList<List<String>>() , data1 = new ArrayList<List<String>>() , data2 = new ArrayList<List<String>>()
                    , data3 = new ArrayList<List<String>>() , data4 = new ArrayList<List<String>>() ,data5 = new ArrayList<List<String>>();
            int index = 1;
            for (PolicyTaskDetailVO task : taskList[0]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcDomain());
                rowData.add(task.getSrcIp());
                rowData.add(task.getSrcIpSystem());
                rowData.add(task.getDstDomain());
                rowData.add(task.getDstIp());
                rowData.add(task.getDstIpSystem());
                rowData.add(getServiceString(task.getService()));
                rowData.add(getValidTimeString(task.getTime()));
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(getActionString(task.getAction()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data.add(rowData);
                index++;
            }
            index = 1;

            for (PolicyTaskDetailVO task : taskList[1]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcDomain());
                rowData.add(task.getDstDomain());
                rowData.add(task.getPreSrcIp());
                rowData.add(task.getSrcIpSystem());
                rowData.add(task.getDstIp());
                rowData.add(task.getDstIpSystem());
                rowData.add(getServiceString(task.getService()));
                rowData.add(task.getPostSrcIp());
                rowData.add(task.getPostSrcIpSystem());
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data1.add(rowData);
                index++;
            }
            index = 1;

            for (PolicyTaskDetailVO task : taskList[2]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcDomain());
                rowData.add(task.getDstDomain());
                rowData.add(task.getSrcIp());
                rowData.add(task.getSrcIpSystem());
                rowData.add(task.getPreDstIp());
                rowData.add(task.getDstIpSystem());
                rowData.add(getServiceString(task.getService()));
                rowData.add(task.getPostDstIp());
                rowData.add(task.getPostDstIpSystem());
                rowData.add(getPostServiceString(task.getPostService()));
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data2.add(rowData);
                index++;
            }
            index = 1;

            for (PolicyTaskDetailVO task : taskList[3]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcDomain());
                rowData.add(task.getDstDomain());
                rowData.add(task.getPublicAddress());
                rowData.add(task.getPrivateAddress());
                rowData.add(task.getProtocol());
                rowData.add(task.getPublicPort());
                rowData.add(task.getPrivatePort());
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data3.add(rowData);
                index++;
            }
            index = 1;

            for (PolicyTaskDetailVO task : taskList[4]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcDomain());
                rowData.add(task.getDstDomain());
                rowData.add(task.getPreSrcIp());
                rowData.add(task.getPostSrcIp());
                rowData.add(task.getPreDstIp());
                rowData.add(task.getPostDstIp());
                rowData.add(getServiceString(task.getService()));
                rowData.add(getServiceString(task.getPostService()));
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data4.add(rowData);
                index++;
            }
            index = 1;

            for (PolicyTaskDetailVO task : taskList[5]) {
                List<String> rowData = new ArrayList<>();
                rowData.add(String.valueOf(index));
                rowData.add(task.getPolicyName());
                rowData.add(task.getDeviceIp());
                rowData.add(task.getSrcVirtualRouter());
                rowData.add(task.getDstIp() + "/" + task.getSubnetMask());
                rowData.add(task.getDstVirtualRouter());
                rowData.add(task.getOutInterface());
                rowData.add(task.getNextHop());
                rowData.add(task.getPriority());
                rowData.add(task.getManagementDistance());
                rowData.add(getStatusString(task.getPushStatus()));
                rowData.add(task.getUserName());
                rowData.add(getTimeString(task.getCreateTime()));
                rowData.add(task.getDescription());
                rowData.add(task.getRemarks());

                data5.add(rowData);
                index++;
            }


            String[] headers = { "序号", "主题（工单号）", "设备/场景",  "源域/接口", "源地址", "源地址对象名", "目的域/接口", "目的地址", "目的地址对象名", "服务","生效时间","任务状态","动作","创建人","创建时间","策略描述","工单备注"};
            String[] headers1 = { "序号", "主题（工单号）", "设备",  "源域/接口", "目的域/接口", "源地址(转换前)", "源地址对象名", "目的地址", "目的地址对象名", "服务","源地址(转换后)","转换后源地址对象名","任务状态","创建人","创建时间","策略描述","工单备注"};
            String[] headers2 = { "序号", "主题（工单号）", "设备",  "源域/接口", "目的域/接口", "源地址", "源地址对象名", "目的地址(转换前)", "目的地址对象名", "服务","目的地址(转换后)","转换后目的地址对象名","转换后服务","任务状态","创建人","创建时间","策略描述","工单备注"};
            String[] headers3 = { "序号", "主题（工单号）", "设备",  "源域/接口", "目的域/接口", "外网地址", "内网地址", "协议", "外网端口", "内网端口","任务状态","创建人","创建时间","策略描述","工单备注"};
            String[] headers4 = { "序号", "主题（工单号）", "设备",  "源域/接口", "目的域/接口", "源地址(转换前)", "源地址(转换后)", "目的地址(转换前)", "目的地址(转换后)", "服务","转换后服务","任务状态","创建人","创建时间","策略描述","工单备注"};
            String[] headers5 = { "序号", "主题（工单号）", "设备",  "所属虚拟路由器", "目的地址/掩码", "目的虚拟路由器", "出接口", "下一跳", "优先级", "管理距离","任务状态","创建人","创建时间","策略描述","工单备注"};

            ExportExcelUtils eeu = new ExportExcelUtils();
            XSSFWorkbook workbook = new XSSFWorkbook();
            eeu.exportPolicyGenerateAddData(workbook, 0, "安全策略", headers, data, out);
            eeu.exportPolicyGenerateAddData(workbook, 1, "源NAT", headers1, data1, out);
            eeu.exportPolicyGenerateAddData(workbook, 2, "目的NAT", headers2, data2, out);
            eeu.exportPolicyGenerateAddData(workbook, 3, "静态NAT", headers3, data3, out);
            eeu.exportPolicyGenerateAddData(workbook, 4, "BothNAT", headers4, data4, out);
            eeu.exportPolicyGenerateAddData(workbook, 5, "静态路由", headers5, data5, out);
            // 原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
        } catch (Exception e) {
            File file = new File(filePath);
            if(file.exists()) {
                file.delete();
            }
            log.error("策略生成导出excel异常", e);
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
     * 导出excel状态字段调整
     * @param action
     * @return
     */
    protected String getActionString(String action){
        if("PERMIT".equals(action)){
            return "允许";
        }else {
            return "禁止";
        }
    }

    /**
     * 导出excel状态字段调整
     * @param pushStatus
     * @return
     */
    protected String getStatusString(int pushStatus){
        if(pushStatus == 11){
            return "加入下发队列";
        }else if(pushStatus == 13){
            return "下发部分完成";
        }else if(pushStatus == 19){
            return "下发失败";
        }else if(pushStatus == 20){
            return "下发成功";
        } else {
            return "下发未开始";
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
     * 导出-获取转换后服务
     * @param service
     * @return
     */
    protected String getPostServiceString(String service){
        StringBuilder serviceSb = new StringBuilder();
        if(org.apache.commons.lang3.StringUtils.isBlank(service)){
            return serviceSb.toString();
        }else {
            JSONArray jsonArray = JSONArray.parseArray(service);
            for(int i= 0 ; i< jsonArray.size(); i++ ){
                JSONObject json = jsonArray.getJSONObject(i);
                String protocol = json.getString("protocol");
                String dstPorts = json.getString("dstPorts");
                if(org.apache.commons.lang3.StringUtils.equalsIgnoreCase("any", protocol)){
                    serviceSb.append("");
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



    protected String getTimeString(Date time){
        if(time == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = sdf.format(time);
        return timeString;
    }

    protected String getValidTimeString(String time){
        if(time == null){
            return "永久";
        }
        return time;
    }
}
