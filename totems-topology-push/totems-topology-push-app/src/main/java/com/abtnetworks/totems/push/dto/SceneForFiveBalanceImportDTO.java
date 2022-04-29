package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.utils.excel.annotation.TotemsExcelField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;


/**
* 策略生成F5负载场景实体类实体
*
* @author lifei
* @since 2021年07月30日
*/
@ApiModel("策略生成F5负载场景DTO")
public class SceneForFiveBalanceImportDTO {

    @ApiModelProperty("序号")
    private Integer id;

    @ApiModelProperty("场景名称")
    private String applySystemName;

    @ApiModelProperty("应用发布类型")
    private String applyType;

    @ApiModelProperty("节点负载模式")
    private String loadBlanaceMode;

    @ApiModelProperty("节点回话保持")
    private String persist;

    @ApiModelProperty("健康检查")
    private String monitor;


    @TotemsExcelField(title = "序号", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 0)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @TotemsExcelField(title = "场景名称", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 10)
    public String getApplySystemName() {
        return applySystemName;
    }

    public void setApplySystemName(String applySystemName) {
        this.applySystemName = applySystemName;
    }


    @TotemsExcelField(title = "应用发布类型", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 20)
    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    @TotemsExcelField(title = "节点负载模式", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 30)
    public String getLoadBlanaceMode() {
        return loadBlanaceMode;
    }

    public void setLoadBlanaceMode(String loadBlanaceMode) {
        this.loadBlanaceMode = loadBlanaceMode;
    }

    @TotemsExcelField(title = "节点会话保持", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 40)
    public String getPersist() {
        return persist;
    }

    public void setPersist(String persist) {
        this.persist = persist;
    }

    @TotemsExcelField(title = "健康检查", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 50)
    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }


    @Override
    public String toString() {
        return "SceneForFiveBalanceImportDTO{" +
                "id=" + id +
                ", applySystemName='" + applySystemName + '\'' +
                ", applyType='" + applyType + '\'' +
                ", loadBlanaceMode='" + loadBlanaceMode + '\'' +
                ", persist='" + persist + '\'' +
                ", monitor='" + monitor + '\'' +
                '}';
    }

    public void dealData() {

        if (StringUtils.isNotBlank(applySystemName)) {
            applySystemName = applySystemName.trim();
        }
        if (StringUtils.isNotBlank(applyType)) {
            applyType = applyType.trim();
        }
        if (StringUtils.isNotBlank(loadBlanaceMode)) {
            loadBlanaceMode = loadBlanaceMode.trim();
        }
        if (StringUtils.isNotBlank(persist)) {
            persist = persist.trim();
        }
        if (StringUtils.isNotBlank(monitor)) {
            monitor = monitor.trim();
        }
    }
}