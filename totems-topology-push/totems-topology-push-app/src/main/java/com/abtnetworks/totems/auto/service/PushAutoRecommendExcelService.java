package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zhoumuhua
 * @desc 自动开通工单excel接口实现类
 * @date 2021-09-16
 */
public interface PushAutoRecommendExcelService {

    /**
     * 防护网段excel批量导入
     * @param file
     * @param userName
     * @param taskList
     * @return
     */
    String parseProtectNetworkExcel(MultipartFile file, String userName, List<ProtectNetworkConfigVO> taskList);

    /**
     * 下载文件
     * @param response
     * @param fileExcitPath
     */
    void downLoadProtect(HttpServletResponse response, String fileExcitPath);

}
