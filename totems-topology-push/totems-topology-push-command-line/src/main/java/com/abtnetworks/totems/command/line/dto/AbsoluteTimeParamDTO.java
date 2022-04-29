package com.abtnetworks.totems.command.line.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: WangCan
 * @Description 绝对时间对象
 * @Date: 2021/5/21
 */
@Data
public class AbsoluteTimeParamDTO {
    /**
     * 开始日期 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 开始时间 HH:mm:ss
     */
    private String startTime;

    /**
     * 结束日志 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 结束时间 HH:mm:ss
     */
    private String endTime;

    public AbsoluteTimeParamDTO() {
    }

    public AbsoluteTimeParamDTO(String startDate, String startTime, String endDate, String endTime) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
    }

    public AbsoluteTimeParamDTO(String start, String end) {
        if(StringUtils.isNotBlank(start)){
            String[] startArr = start.split(" ");
            this.startDate = startArr[0];
            this.startTime = startArr[1];
        }
        if(StringUtils.isNotBlank(end)){
            String[] endArr = end.split(" ");
            this.endDate = endArr[0];
            this.endTime = endArr[1];
        }
    }
}
