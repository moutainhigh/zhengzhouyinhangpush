package com.abtnetworks.totems.common.dto.manager;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/19
 */
@Data
public class IpRangeDTO {

    String start;

    String end;


    @Override
    public String toString() {
        return this.start.equals(this.end) ? start: start + "-" + end;
    }
}
