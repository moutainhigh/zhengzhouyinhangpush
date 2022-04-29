package com.abtnetworks.totems.push.dto.policy;

import com.abtnetworks.totems.common.utils.PortUtils;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/7/13
 */
@Data
public class PortValueDTO {

    int start;
    int end;

    @Override
    public String toString() {
        if(start != end) {
            return String.format("%d-%d", start, end);
        } else {
            return String.format("%d", end);
        }
    }

    public PortValueDTO(String port) {
        if(PortUtils.isPortRange(port)) {
            String startString = PortUtils.getStartPort(port);
            String endString = PortUtils.getEndPort(port);
            start = Integer.valueOf(startString);
            end = Integer.valueOf(endString);
        } else {
            start = Integer.valueOf(port);
            end = Integer.valueOf(port);
        }
    }
}
