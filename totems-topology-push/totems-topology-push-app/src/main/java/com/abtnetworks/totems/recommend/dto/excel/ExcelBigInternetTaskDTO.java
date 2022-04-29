package com.abtnetworks.totems.recommend.dto.excel;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 大网段开通的对应excel实体属性
 * @date 2021/1/19
 */
public class ExcelBigInternetTaskDTO {


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

    @ApiModelProperty("标签模式 0 是or，1是and")
    private String labelModel;

    @ApiModelProperty("起点标签")
    private String startLabel;


    /** ip 类型 **/
    private String ipType;

    @ApiModelProperty("合并检查")
    private String mergeCheck;
    @ApiModelProperty("移动冲突前")
    private String beforeConflict;

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


    @ExcelField(title="IP类型",type = 2,align=2, sort=20)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
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


    @ExcelField(title="标签模式",type = 2,align=2, sort=35)
    public String getLabelModel() {
        return labelModel;
    }

    public void setLabelModel(String labelModel) {
        this.labelModel = labelModel;
    }

    @ExcelField(title="起点标签",type = 2,align=2, sort=30)
    public String getStartLabel() {
        return startLabel;
    }

    public void setStartLabel(String startLabel) {
        this.startLabel = startLabel;
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






}
