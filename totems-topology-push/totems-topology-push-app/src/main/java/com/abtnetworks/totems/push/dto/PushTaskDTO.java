package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.push.entity.PushTaskEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 11:20
 */
@Data
public class PushTaskDTO {

    private int taskId;

    private String orderNo;

    List<PushTaskEntity> list;
}
