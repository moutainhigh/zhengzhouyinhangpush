package com.abtnetworks.totems.recommend.dto.push;

import com.abtnetworks.totems.push.dto.PushStatus;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/17
 */
@Data
public class TaskStatusBranchLevelsDTO {

    private List<PushStatus> pushStatuses;

    private String branchLevel;
}
