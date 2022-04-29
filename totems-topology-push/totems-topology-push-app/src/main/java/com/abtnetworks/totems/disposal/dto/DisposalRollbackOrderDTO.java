package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseDto;

import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 17:56 2019/11/12
 */
public class DisposalRollbackOrderDTO extends DisposalOrderDTO {

    private static final long serialVersionUID = 3998046559238665990L;

    /**
     * 父类工单UUID
     */
    private String pCenterUuid;

    public DisposalRollbackOrderDTO() {
    }

    public String getpCenterUuid() {
        return pCenterUuid;
    }

    public void setpCenterUuid(String pCenterUuid) {
        this.pCenterUuid = pCenterUuid;
    }
}
