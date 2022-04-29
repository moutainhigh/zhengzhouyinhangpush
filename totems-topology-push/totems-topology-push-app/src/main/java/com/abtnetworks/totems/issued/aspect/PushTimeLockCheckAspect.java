package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.dto.AbsoluteTimeDTO;
import com.abtnetworks.totems.advanced.dto.CycleTimeDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.issued.annotation.PushTimeLockCheck;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Aspect
@Component
@Slf4j
public class PushTimeLockCheckAspect {

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    protected LogClientSimple logClientSimple;

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.PushTimeLockCheck)")
    public void operationInCommandPoint() {log.debug("下发时间锁判断点");}

    @Around(value = "operationInCommandPoint() && @annotation(pushTimeLockCheck)")
    public Object advice(ProceedingJoinPoint pointcut,PushTimeLockCheck pushTimeLockCheck){
        Object proceed = null;
        boolean isCheck = pushTimeLockCheck.value();
        if(isCheck){
            Boolean doPush = true;
            log.info("根据高级设置 检查当前时间是否允许下发");
            //根据时间锁判断是否下发
            try {
                doPush = judgeTimeLock(doPush);
            } catch (Exception e) {
                log.error("下发时间锁处理异常",e);
                doPush=false;
            }
            //设置判断结果
            pointcut.getArgs()[1] = doPush;
            try {
                proceed = pointcut.proceed(pointcut.getArgs());
            } catch (Throwable throwable) {
                log.error("",throwable);
            }
        }else {
            log.info("不走 高级设置-下发时间锁 流程");
            try {
                proceed = pointcut.proceed();
            } catch (Throwable throwable) {
                log.error("",throwable);
            }
        }
        return proceed;
    }

    private Boolean judgeTimeLock(boolean doPush) throws Exception{
        if(!doPush){
            return false;
        }
        //获取高级设置的下发时间锁
        String timeLockParam = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK);
        if(StringUtils.isEmpty(timeLockParam)){
            return true;
        }
        JSONObject pushTimeLockJsonObj = (JSONObject) JSONObject.parse(timeLockParam);
        //获取时间锁 绝对时间
        JSONArray absoluteTimeDTOJsonArray  = new JSONArray();
        if(ObjectUtils.isNotEmpty(pushTimeLockJsonObj.get("absoluteTimeDTOList"))){
            absoluteTimeDTOJsonArray = (JSONArray) pushTimeLockJsonObj.get("absoluteTimeDTOList");
        }
        List<AbsoluteTimeDTO> absoluteTimeDTOList = absoluteTimeDTOJsonArray.toJavaList(AbsoluteTimeDTO.class);
        //获取时间锁 周期时间
        JSONArray cycleTimeDTOListJsonObj = new JSONArray();
        if(ObjectUtils.isNotEmpty(pushTimeLockJsonObj.get("cycleTimeDTOList"))){
            cycleTimeDTOListJsonObj = (JSONArray) pushTimeLockJsonObj.get("cycleTimeDTOList");
        }
        List<CycleTimeDTO> cycleTimeDTOList = cycleTimeDTOListJsonObj.toJavaList(CycleTimeDTO.class);
        //获取当前时间
        long currentTimeMillis = System.currentTimeMillis();
        Date nowDate = new Date(currentTimeMillis);
        //判断当前时间是否在绝对时间锁范围内
        doPush = judgeAbsoluteTimeLock(doPush, absoluteTimeDTOList, currentTimeMillis);
        //判断当前时间是否在周期时间锁范围内
        doPush = judgeCycleTimeLock(doPush, cycleTimeDTOList, nowDate);
        return doPush;
    }

    private Boolean judgeCycleTimeLock(boolean doPush, List<CycleTimeDTO> cycleTimeDTOList, Date nowDate) {
        if(!doPush){
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);
        for (CycleTimeDTO cycleTimeDTO : cycleTimeDTOList) {
            if(!doPush){
                break;
            }
            //判断星期是否匹配
            boolean inWeeks = false;
            if(cycleTimeDTO.getCycleType() == 0 && StringUtils.isNotEmpty(cycleTimeDTO.getWeek())){
                String nowWeek = String.valueOf(cal.get(Calendar.DAY_OF_WEEK) - 1);
                String[] weeks = cycleTimeDTO.getWeek().split(",");
                for (String week : weeks) {
                    if (nowWeek.equals(week)) {
                        inWeeks = true;
                        break;
                    }
                }
            }else if(cycleTimeDTO.getCycleType()==1){
                inWeeks = true;
            }
            //判断小时是否匹配
            boolean inHours = false;
            boolean matchStartHour = false;
            boolean matchEndHour = false;
            if(inWeeks){
                if((cycleTimeDTO.getStartHour()!=null && cycleTimeDTO.getEndHour()!=null)){
                    int nowHour = cal.get(Calendar.HOUR_OF_DAY);
                    if(cycleTimeDTO.getStartHour()<=nowHour && nowHour<=cycleTimeDTO.getEndHour()){
                        inHours = true;
                    }
                    if(cycleTimeDTO.getStartHour()==nowHour){
                        matchStartHour = true;
                    }
                    if(cycleTimeDTO.getEndHour()==nowHour){
                        matchEndHour = true;
                    }
                }else{
                    doPush = false;
                    //添加操作日志
                    addCycleTimeBusinessLog(nowDate,cycleTimeDTO);
                }
            }
            //判断分钟是否匹配
            if(inHours){
                int nowMinute = cal.get(Calendar.MINUTE);
                doPush = (matchStartHour && nowMinute < cycleTimeDTO.getStartMinute()) || (matchEndHour && nowMinute > cycleTimeDTO.getEndMinute());
                if(!doPush){
                    //添加操作日志
                    addCycleTimeBusinessLog(nowDate,cycleTimeDTO);
                }
            }
        }
        return doPush;
    }

    private void addCycleTimeBusinessLog(Date nowDate, CycleTimeDTO cycleTimeDTO) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder cycleTimeSb = new StringBuilder();
        if(StringUtils.isNotEmpty(cycleTimeDTO.getWeek())){
            cycleTimeSb.append("每周").append(cycleTimeDTO.getWeek());
        }else {
            cycleTimeSb.append("每日");
        }
        cycleTimeSb.append("的");
        if(cycleTimeDTO.getStartHour()!=null){
            cycleTimeSb.append(cycleTimeDTO.getStartHour()).append("点");
            if(cycleTimeDTO.getStartMinute()!=null){
                cycleTimeSb.append(cycleTimeDTO.getStartMinute()).append("分");
            }
            cycleTimeSb.append("-");
        }
        if(cycleTimeDTO.getEndHour()!=null){
            cycleTimeSb.append(cycleTimeDTO.getEndHour()).append("点");
            if(cycleTimeDTO.getEndMinute()!=null){
                cycleTimeSb.append(cycleTimeDTO.getEndMinute()).append("分");
            }
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH_PUSH.getId(),
                String.format("下发匹配到下发时间锁！下发时间：%s;匹配到周期时间锁：%s", simpleDateFormat.format(nowDate),cycleTimeSb.toString()));
    }

    private Boolean judgeAbsoluteTimeLock(boolean doPush, List<AbsoluteTimeDTO> absoluteTimeDTOList, long currentTimeMillis) {
        if(!doPush){
            return false;
        }
        if(CollectionUtils.isNotEmpty(absoluteTimeDTOList)){
            for (AbsoluteTimeDTO absoluteTimeDTO : absoluteTimeDTOList) {
                Long startTime = absoluteTimeDTO.getStartTime();
                Long endTime = absoluteTimeDTO.getEndTime();
                if (startTime !=null && endTime !=null && startTime < currentTimeMillis && currentTimeMillis < endTime) {
                    doPush = false;
                    //添加操作日志
                    addAbsoluteTimeBusinessLog(currentTimeMillis, startTime, endTime);
                    break;
                }
            }
        }
        return doPush;
    }

    private void addAbsoluteTimeBusinessLog(long currentTimeMillis, Long startTime, Long endTime) {
        String dateString = timeMillisToDateString(currentTimeMillis);
        String startString = timeMillisToDateString(startTime);
        String endString = timeMillisToDateString(endTime);
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH_PUSH.getId(), String.format("下发匹配到下发时间锁！下发时间：%s;匹配到绝对时间锁：%s",dateString,startString+"-"+endString));
    }

    private String timeMillisToDateString(long currentTimeMillis) {
        Date nowDate = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(nowDate);
    }
}
