package com.abtnetworks.totems.common.dto.generate;

import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 生成命令行的数据
 * @date 2021/3/31
 */
@Data
public class GenerateCommandDTO {

    private String commandline;

    private String rollbackCommandline;

    private  Integer resultCode;

}
