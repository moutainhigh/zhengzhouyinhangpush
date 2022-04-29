
package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

/**
 * @Author: WangCan
 * @Description 周期时间对象
 * @Date: 2021/5/21
 */
@Data
public class PeriodicTimeParamDTO {
    /**
     * 周期：如 daily weekdays weekend monday...sunday
     */
    private String[] cycle;

    /**
     * 周期类型
     */
    private String cycleType;

    private String cycleStart;

    private String cycleEnd;

    /**
     * 配置起止时间
     */
    private String absoluteStart;

    private String absoluteEnd;

    public PeriodicTimeParamDTO() {
    }

    public PeriodicTimeParamDTO(String[] cycle, String cycleStart, String cycleEnd) {
        this.cycle = cycle;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
    }
}