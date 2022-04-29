package com.abtnetworks.totems.push.dao.mysql;

import com.abtnetworks.totems.push.dto.MailServerConfDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author luwei
 * @date 2019/5/20
 */
@Mapper
@Repository
public interface SystemParamMapper {

    MailServerConfDTO findEmailParam(@Param("groupName") String groupName);

}
