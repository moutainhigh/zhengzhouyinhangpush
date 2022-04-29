package com.abtnetworks.totems.common.tools.excel;

import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.credential.entity.ExcelCredentialEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExcelParser {
    String parse(MultipartFile file, UserInfoDTO userInfoDTO, List<RecommendTaskEntity> taskList, List<RecommendTaskEntity>natTaskList,List<PushRecommendStaticRoutingDTO> tmpRouteList);
    List<RecommendTaskEntity> getRecommendTaskEntity(List<ExcelTaskNatEntity> list, String theme, String userName, UserInfoDTO userInfoDTO);

    void createNatCommandTask(List<RecommendTaskEntity> natTaskList, Authentication auth);

    /**
     * 解析凭据导入excel
     * @param file
     * @param userName
     * @param taskList
     * @return
     */
    String parseCredentialExcel(MultipartFile file, String userName, List<ExcelCredentialEntity> taskList, Boolean encrypt);

    /**
     *
     * @param routeExcelList
     * @param userName
     * @param userInfoDTO
     * @return
     */
    List<PushRecommendStaticRoutingDTO> getRouteTaskEntity(List<ExcelTaskStaticRouteEntity> routeExcelList, String userName, UserInfoDTO userInfoDTO);

    /**
     * 获取特殊关联场景DTO
     * @param specialNatEntityList
     * @param userName
     * @param userInfoDTO
     * @return
     */
    List<RecommendRelevanceSceneDTO> getSpecialNatDTO(List<ExcelTaskSpecialNatEntity> specialNatEntityList, String userName, UserInfoDTO userInfoDTO);


    /**
     * 自动开通excel批量导入
     * @param file
     * @param userName
     * @param taskList
     * @return
     */
    String parseAutoRecommendExcel(MultipartFile file, String userName, List<AutoRecommendTaskVO> taskList);
}
