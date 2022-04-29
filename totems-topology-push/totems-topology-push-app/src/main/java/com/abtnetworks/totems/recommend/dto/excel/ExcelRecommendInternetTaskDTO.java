package com.abtnetworks.totems.recommend.dto.excel;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 仿真开通互联网开通
 * @date 2021/1/19
 */
public class ExcelRecommendInternetTaskDTO{


    String id;

    String orderNO;

    String name;

   String srcIp;

   String srcIpDescription;

    String dstIp;

    String dstIpDescription;

    String service;

    /**读取excel的时间范围***/
    String timeRange;

    String user;

    String description;

    /**读取的service解析后的，转换的**/
    List<ServiceDTO> serviceList;

    /**开始时间**/
    private String startTime;

    /**结束时间**/
    private String endTime;

    /**长连接**/
    private String idleTimeout;


    /** 开通备注 **/
    private String remark;


    /** 开通类型 互联网开通，内网访问外网8，外网访问内网14**/
    private String taskType;
    /** 起点标签**/
    private String startLabel;

    @ApiModelProperty("标签模式 or，and")
    private String  labelModel;

    @ApiModelProperty("合并检查")
    private String mergeCheck;
    @ApiModelProperty("移动冲突前")
    private String beforeConflict;
    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;
	
	/** ip 类型 **/
    private String ipType;

  @ExcelField(title = "序号", type = 2, align = 2, sort = 0)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNO() {
        return orderNO;
    }

    public void setOrderNO(String orderNO) {
        this.orderNO = orderNO;
    }

    @ExcelField(title="主题/工单号",type = 2,align=2, sort=10)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title="开通类型",type = 2,align=2, sort=20)
    public String getTaskType() {
        return taskType;
    }

    @ExcelField(title="IP类型",type = 2,align=2, sort=25)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    @ExcelField(title="起点标签",type = 2,align=2, sort=30)
    public String getStartLabel() {
        return startLabel;
    }

    public void setStartLabel(String startLabel) {
        this.startLabel = startLabel;
    }
    @ExcelField(title="标签模式",type = 2,align=2, sort=35)
    public String getLabelModel() {
        return labelModel;
    }

    public void setLabelModel(String labelModel) {
        this.labelModel = labelModel;
    }

    @ExcelField(title="源IP地址",type = 2,align=2, sort=40)
    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    @ExcelField(title="源地址描述",type = 2,align=2, sort=45)
    public String getSrcIpDescription() {
        return srcIpDescription;
    }

    public void setSrcIpDescription(String srcIpDescription) {
        this.srcIpDescription = srcIpDescription;
    }

    @ExcelField(title="目的主机地址",type = 2,align=2, sort=50)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    @ExcelField(title="目的地址描述",type = 2,align=2, sort=55)
    public String getDstIpDescription() {
        return dstIpDescription;
    }

    public void setDstIpDescription(String dstIpDescription) {
        this.dstIpDescription = dstIpDescription;
    }

    @ExcelField(title = "服务", type = 2, align = 2, sort = 60)
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


    @ExcelField(title="生效时间",type = 2,align=2, sort=70)
    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    @ExcelField(title="长连接",type = 2,align=2, sort=75)
    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @ExcelField(title="申请人",type = 2,align=2, sort=80)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @ExcelField(title="备注",type = 2,align=2, sort=90)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ExcelField(title="开通备注",type = 2,align=2, sort=100)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @ExcelField(title="合并检查",type = 2,align=2, sort=104)
    public String getMergeCheck() {
        return mergeCheck;
    }

    public void setMergeCheck(String mergeCheck) {
        this.mergeCheck = mergeCheck;
    }
    @ExcelField(title="移动到冲突前",type = 2,align=2, sort=106)
    public String getBeforeConflict() {
        return beforeConflict;
    }

    public void setBeforeConflict(String beforeConflict) {
        this.beforeConflict = beforeConflict;
    }

    @ExcelField(title="转换后源地址",type = 2,align=2, sort=108)
    public String getPostSrcIp() {
        return postSrcIp;
    }

    public void setPostSrcIp(String postSrcIp) {
        this.postSrcIp = postSrcIp;
    }

    @ExcelField(title="转换后目的地址",type = 2,align=2, sort=110)
    public String getPostDstIp() {
        return postDstIp;
    }

    public void setPostDstIp(String postDstIp) {
        this.postDstIp = postDstIp;
    }

    public List<ServiceDTO> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<ServiceDTO> serviceList) {
        this.serviceList = serviceList;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(id) && AliStringUtils.isEmpty(srcIp)
                && AliStringUtils.isEmpty(dstIp) && AliStringUtils.isEmpty(service)) {
            return true;
        }
        return false;
    }





    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id = ").append(id);
        sb.append(", name = ").append(name);
        sb.append(", user = ").append(user);
        sb.append(", description = ").append(description);
        sb.append(", srcIp = ").append(srcIp);
        sb.append(", dstIp = ").append(dstIp);
        sb.append(", timeRange = ").append(timeRange);
        sb.append(", service = ").append(service);

        sb.append("]");

        return sb.toString();
    }




//    private String getProtocolValue(String protocol) {
//        if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
//            return TCP_VALUE_STRING;
//        }
//
//        if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
//            return UDP_VALUE_STRING;
//        }
//
//        if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
//            return ICMP_VALUE_STRING;
//        }
//
//        //any
//        return ANY_VALUE_STRING;
//    }
//
//    /**
//     * 检测是否为合法ICMP值（包括ICMPCode和Type值，0~255之间的数字）
//     * @param value 值
//     * @return 合法返回true，不合法返回false
//     */
//    private boolean isValidIcmpValue(String value) {
//        if(!StringUtils.isNumeric(value)){
//            return false;
//        }
//        if(Integer.parseInt(value) < 0 || Integer.parseInt(value) > 255){
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * 检测时间数据是否合法。
//     * @param startTime
//     * @param endTime
//     * @return
//     */
//    private boolean isValidDateFormat(String startTime, String endTime) {
//        boolean valid = true;
//        if(AliStringUtils.isEmpty(startTime) && AliStringUtils.isEmpty(endTime)) {
//
//        } else if(startTime != null && endTime != null) {
//            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
//            try {
//                Date start = sdf.parse(startTime);
//                Date end = sdf.parse(endTime);
//                if (!start.before(end)) {
//                    valid = false;
//                }
//            } catch (ParseException e) {
//                valid = false;
//            }
//        } else {
//            valid = false;
//        }
//        return valid;
//    }

    //判断地址是否有大于16的子网或者数量大于65535（255*255）的IP范围，有则为超大工单
//    public boolean isIgnore() {
//        String[] srcIps = srcIp.split(",");
//        for(String srcIpString: srcIps) {
//            if(isBigRange(srcIpString)) {
//                return true;
//            }
//        }
//
//        String[] dstIps = dstIp.split(",");
//        for(String dstIpString: dstIps) {
//            if(isBigRange(dstIpString)) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    public boolean isBigRange(String ipAddress) {
        if(IpUtils.isIPRange(ipAddress)) {
            String startIp = IpUtils.getStartIpFromIpAddress(ipAddress);
            String endIp = IpUtils.getEndIpFromIpAddress(ipAddress);
            Long startIpLong = IpUtils.IPv4StringToNum(startIp);
            Long endIpLong = IpUtils.IPv4StringToNum(endIp);
            if((endIpLong - startIpLong) >= 65535) {
                return true;
            }
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String maskbitString = IpUtils.getMaskBitFromIpSegment(ipAddress);
            int maskbit = Integer.valueOf(maskbitString);
            if(maskbit <= 16) {
                return true;
            }
        }

        return false;
    }
//
//    public static void main(String[] args) {
//        String segment = "192.168.20.10/15";
//        String range = "192.168.0.0-192.168.255.255";
//
//        ExcelRecommendInternetTaskDTO entity = new ExcelRecommendInternetTaskDTO();
//
//        System.out.println(entity.isBigRange(segment));
//        System.out.println(entity.isBigRange(range));
//    }
}
