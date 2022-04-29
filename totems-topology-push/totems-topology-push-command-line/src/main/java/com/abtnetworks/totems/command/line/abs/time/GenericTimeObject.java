package com.abtnetworks.totems.command.line.abs.time;

import com.abtnetworks.totems.command.line.abs.service.GenericServiceBean;
import com.abtnetworks.totems.command.line.dto.AbsoluteTimeParamDTO;
import com.abtnetworks.totems.command.line.dto.PeriodicTimeParamDTO;
import com.abtnetworks.totems.command.line.inf.time.TimeObjectInterface;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:09'.
 */
public abstract class GenericTimeObject extends GenericServiceBean implements TimeObjectInterface {

    /**
     * 绝对计划时间对象名
     * @param absoluteTimeParamDTO
     * @param map
     * @return
     */
    public String createTimeObjectNameByAbsolute(AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String,Object> map,String[] args) {
        if(ObjectUtils.isEmpty(absoluteTimeParamDTO)){
            return StringUtils.EMPTY;
        }
        String endTime = null;
        if(StringUtils.isBlank(absoluteTimeParamDTO.getEndDate()) || StringUtils.isBlank(absoluteTimeParamDTO.getEndTime())){
            return StringUtils.EMPTY;
        } else {
            endTime = String.format("%s %s",absoluteTimeParamDTO.getEndDate(),absoluteTimeParamDTO.getEndTime());
        }
        String endTimeStr = TotemsTimeUtils.transformDateFormat(endTime, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.COMMON_TIME_DAY_FORMAT);
        return String.format("to%s",endTimeStr);
    }

    public String createTimeObjectNameByAbsoluteParamDTO(AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String,Object> map) {
        if(ObjectUtils.isEmpty(absoluteTimeParamDTO)){
            return StringUtils.EMPTY;
        }
        String endTime = null;
        int dtoHashCode = absoluteTimeParamDTO.hashCode();
        dtoHashCode = dtoHashCode<0?-dtoHashCode:dtoHashCode;
        if(StringUtils.isBlank(absoluteTimeParamDTO.getEndDate()) || StringUtils.isBlank(absoluteTimeParamDTO.getEndTime())){
            endTime = String.valueOf(dtoHashCode);
        } else {
            endTime = String.format("%s %s",absoluteTimeParamDTO.getEndDate(),absoluteTimeParamDTO.getEndTime());
        }
        String endTimeStr = TotemsTimeUtils.transformDateFormat(endTime, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.COMMON_TIME_DAY_FORMAT);
        return String.format("to%s",endTimeStr);
    }

    /**
     * 周期计划时间对象名
     * @param periodicTimeParamDTO
     * @param map
     * @return
     */
    public String createTimeObjectNameByPeriodic(PeriodicTimeParamDTO periodicTimeParamDTO, Map<String,Object> map,String[] args) {
        if(ObjectUtils.isEmpty(periodicTimeParamDTO)){
            return StringUtils.EMPTY;
        }

        String[] cycle = periodicTimeParamDTO.getCycle();
        String cycleStart = periodicTimeParamDTO.getCycleStart();
        String cycleEnd = periodicTimeParamDTO.getCycleEnd();
        int dtoHashCode = periodicTimeParamDTO.hashCode();
        dtoHashCode = dtoHashCode<0?-dtoHashCode:dtoHashCode;
        if(ArrayUtils.isEmpty(cycle) || StringUtils.isEmpty(cycleStart) || StringUtils.isEmpty(cycleEnd)){
            return String.format("p_%s",dtoHashCode);
        }
        return String.format("%s_%s_%s",cycle[0],cycleStart,cycleEnd);
    }

}
