package com.abtnetworks.totems.command.line.inf.time;

import com.abtnetworks.totems.command.line.dto.AbsoluteTimeParamDTO;
import com.abtnetworks.totems.command.line.dto.PeriodicTimeParamDTO;
import com.abtnetworks.totems.command.line.inf.BasicInterface;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:27'.
 */
public interface TimeObjectInterface extends BasicInterface {

    /**
     * 生成时间对象名称
     * @param name
     * @return
     */
    default String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return name;
    }

    /**
     * 生成绝对计划时间对象命令行
     * @param name 时间标记字符串
     * @param attachStr 附加字符串
     * @param absoluteTimeParamDTO 绝对计划
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO,
                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }
    /**
     * 生成周期计划时间对象命令行
     * @param name 时间标记字符串
     * @param attachStr 附加字符串
     * @param periodicTimeParamDTO 周期计划
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO,
                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 删除周期计划命令行
     * @param timeFlag
     * @param map
     * @param args
     * @return
     */
    default String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args){
        return "";
    }

    /**
     * 删除时间对象命令行
     * @param name 时间对象名称
     * @param map
     * @param args
     * @return
     */
    default String deleteAbsoluteTimeCommandLine(String name,Map<String, Object> map, String[] args){
        return "";
    }

}
