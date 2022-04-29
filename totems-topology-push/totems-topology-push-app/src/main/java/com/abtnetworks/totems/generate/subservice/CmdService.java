package com.abtnetworks.totems.generate.subservice;

import com.abtnetworks.totems.common.dto.CmdDTO;

/**
 * @Description 该接口用于对命令行生成对象数据进行处理。
 *              通过实现该接口，并将其加入到命令行生成中的procedure中，可以增加命令行生成步骤的处理流程
 * @Author Wen Jiachang
 */
public interface CmdService {
    void modify(CmdDTO cmdDto) throws Exception;
}
