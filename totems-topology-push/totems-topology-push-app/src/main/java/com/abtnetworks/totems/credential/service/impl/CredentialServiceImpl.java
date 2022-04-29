package com.abtnetworks.totems.credential.service.impl;

import com.abtnetworks.totems.branch.dto.Branch;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.CredentialConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.credential.dto.SearchCredentialByPageDTO;
import com.abtnetworks.totems.credential.dto.UpdateCredentialDTO;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.credential.service.CredentialService;
import com.abtnetworks.totems.credential.vo.CredentialVO;
import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleCredentialClient;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/8 12:04
 */
@Slf4j
@Service
public class CredentialServiceImpl implements CredentialService {

    @Autowired
    CredentialMapper credentialMapper;
    @Autowired
    NodeMapper nodeMapper;

    @Autowired
    WhaleCredentialClient client;

    @Resource
    RemoteBranchService remoteBranchService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int create(String name, String description, String loginName, String loginPassword, String enableUserName, String enablePassword, String userName, Boolean encrypt) {
        String transfLoginPassword = loginPassword;
        String transfEnablePassword = enablePassword;
        if (!encrypt) {
            //如果是导入加密密码，则需要解密后存入Mongo中
            transfLoginPassword = Encodes.decodeBase64Key(loginPassword);
            transfEnablePassword = Encodes.decodeBase64Key(enablePassword);
        }
        //mongo中存的是解密后的
        CredentialResultRO result = client.create(name, description, loginName, transfLoginPassword, enableUserName, transfEnablePassword);

        Integer resultCode = checkSysCredential(result);
        if (resultCode != ReturnCode.POLICY_MSG_OK) {
            return resultCode;
        }
        CredentialResultDataRO data = result.getData().get(0);
        CredentialEntity entity = new CredentialEntity();
        entity.setName(name);
        entity.setId(data.getId());
        entity.setUuid(data.getUuid());
        entity.setLoginName(loginName);
        //mysql中存的是加密的
        if (encrypt) {
            entity.setLoginPassword(Encodes.encodeBase64Key(loginPassword));
            entity.setEnablePassword(Encodes.encodeBase64Key(enablePassword));
        } else {
            entity.setLoginPassword(loginPassword);
            entity.setEnablePassword(enablePassword);
        }
        entity.setEnableUserName(enableUserName);
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if (userInfoDTO != null) {
            entity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            entity.setBranchLevel("00");
        }
        try {
            credentialMapper.create(entity);
        } catch (Exception e) {
            //如果异常，需要对前面的whale中操作删除，保证数据同步一致，todo 高并发挡不住
            log.error("异常，需要对前面的whale中操作删除，保证数据同步一致", e);
            client.delete(data.getUuid());
            throw e;
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 在调用的whale，有可能不同步数据情况捕获确认。最终回滚，tcc
     *
     * @param result
     * @return
     */
    private Integer checkSysCredential(CredentialResultRO result) {

        if (!result.getSuccess() && result.getMessage().indexOf("15001:VALIDATION_DUPLICATE_NAME") != -1) {
            return ReturnCode.CREDENTIAL_VALIDATION_DUPLICATE_NAME;
        }

        if (CredentialConstants.RESULT_SUCCESS_VALUE_FALSE.equals(result.getSuccess())) {
            if (CollectionUtils.isNotEmpty(result.getData())) {
                CredentialResultDataRO credentialResultDataRO = result.getData().get(0);
                if (credentialResultDataRO != null && StringUtils.isNotEmpty(credentialResultDataRO.getUuid())) {
                    log.warn("此处日志说明whale返回失败，单数数据已经新建，不确定是否成功保存");
                    client.delete(credentialResultDataRO.getUuid());
                }
            }
            return ReturnCode.FAILED_TO_CREATE_CREDENTIAL;
        }
        if (result.getData() == null) {
//            client.delete(result.get) whale接口有问题，有时 返回null的数据,第一层拦截
            return ReturnCode.FAILED_TO_CREATE_CREDENTIAL;
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int modify(String id, String uuid, String name, String description, String loginName, String loginPassword, String enableUserName, String enablePassword, String version, String userName, Boolean encrypt) {
        CredentialResultRO credentialResultRO = client.get(uuid);
        String transfLoginPassword = loginPassword;
        String transfEnablePassword = enablePassword;
        if (!encrypt) {
            //如果是导入加密密码，则需要解密后存入Mongo中
            transfLoginPassword = Encodes.decodeBase64Key(loginPassword);
            transfEnablePassword = Encodes.decodeBase64Key(enablePassword);
        }

        if (credentialResultRO == null || CollectionUtils.isEmpty(credentialResultRO.getData())) {
            log.warn("如果whalee没有采集凭据的记录就进行修改，但需要重新建，将mysql中数据同步更新下");
            CredentialResultRO result = client.create(name, description, loginName, transfLoginPassword, enableUserName, transfEnablePassword);
            Integer resultCode = checkSysCredential(result);
            if (resultCode != ReturnCode.POLICY_MSG_OK) {
                log.warn("创建失败了，就不在进行修改");
                throw new IllegalArgumentException("此凭据采集时无效，建议删除重新生成");
            } else {
                CredentialEntity entity = new CredentialEntity();
                entity.setId(id);
                entity.setUuid(uuid);
                entity.setName(name);
                entity.setLoginName(loginName);
                //mysql中存的是加密的
                if (encrypt) {
                    entity.setLoginPassword(Encodes.encodeBase64Key(loginPassword));
                    entity.setEnablePassword(Encodes.encodeBase64Key(enablePassword));
                } else {
                    entity.setLoginPassword(loginPassword);
                    entity.setEnablePassword(enablePassword);
                }
                entity.setEnableUserName(enableUserName);
                credentialMapper.modify(entity);
            }
        }
        CredentialResultRO result = client.modify(id, uuid, name, description, loginName, transfLoginPassword, enableUserName, transfEnablePassword, version);

        if (result != null && CredentialConstants.RESULT_SUCCESS_VALUE_FALSE.equals(result.getSuccess())) {
            return ReturnCode.FAILED_TO_MODIFY_CREDENTIAL;
        }

        CredentialEntity entity = new CredentialEntity();
        entity.setName(name);
        entity.setUuid(uuid);
        entity.setLoginName(loginName);
        //mysql中存的是加密的
        if (encrypt) {
            entity.setLoginPassword(Encodes.encodeBase64Key(loginPassword));
            entity.setEnablePassword(Encodes.encodeBase64Key(enablePassword));
        } else {
            entity.setLoginPassword(loginPassword);
            entity.setEnablePassword(enablePassword);
        }
        entity.setEnableUserName(enableUserName);
        //TODO 如果前面whalee修改失败或者异常，这里需保证数据一致，加事务不能保证whalee同步回滚
        credentialMapper.modify(entity);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int delete(String uuid) {
        if (!AliStringUtils.isEmpty(uuid)) {
            List<String> nodeUuids = nodeMapper.getNodeUuidsByCredentialUuid(uuid);
            if (nodeUuids != null && nodeUuids.size() > 0) {
                return ReturnCode.FAILED_TO_DELETE_CREDENTIAL_NODE_EXIST;
            }
            CredentialResultRO result = client.delete(uuid);
            if (result == null || result.getSuccess() == null) {
                log.warn("whale删除返回空，可能没有数据，mysql中存在数据既可以删除");
                credentialMapper.delete(uuid);
                return ReturnCode.POLICY_MSG_OK;
            } else if (!result.getSuccess()) {
                return ReturnCode.FAILED_TO_DELETE_CREDENTIAL;
            }
            credentialMapper.delete(uuid);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public CredentialResultRO get(String uuid) {
        CredentialResultRO credentialResultRO = client.get(uuid);
        if (credentialResultRO == null || CollectionUtils.isEmpty(credentialResultRO.getData())) {
            log.warn("查询whale为空，说明数据没同步过去");
            credentialResultRO = new CredentialResultRO();
            CredentialEntity credentialEntity = getFromMysql(uuid);
            if (credentialEntity != null) {
                List<CredentialResultDataRO> data = new ArrayList<>();
                CredentialResultDataRO credentialResultDataRO = new CredentialResultDataRO();
                BeanUtils.copyProperties(credentialEntity, credentialResultDataRO);
                data.add(credentialResultDataRO);
                credentialResultRO.setData(data);
            }
        }
        return credentialResultRO;
    }

    @Override
    public CredentialEntity getFromMysql(String uuid) {
        return credentialMapper.getByUuid(uuid);
    }

    @Override
    public PageInfo<CredentialVO> getAll(SearchCredentialByPageDTO searchCredentialByPageDTO, String userName) {

        if (StringUtils.isEmpty(searchCredentialByPageDTO.getBranchLevel())) {
            String branchLevel = remoteBranchService.likeBranch(userName);
            searchCredentialByPageDTO.setBranchLevel(branchLevel);
        } else {
            String branchLevel = searchCredentialByPageDTO.getBranchLevel();
            searchCredentialByPageDTO.setBranchLevel(branchLevel + "%");
        }
        PageHelper.startPage(searchCredentialByPageDTO.getPageIndex(), searchCredentialByPageDTO.getPageSize());
        List<CredentialEntity> credentialEntities = credentialMapper.listByParam(searchCredentialByPageDTO);
        PageInfo pageInfo = new PageInfo<>(credentialEntities);
        List<CredentialVO> credentialVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(credentialEntities)) {
            credentialEntities.forEach(credentialEntity -> {
                CredentialVO credentialVO = new CredentialVO();
                BeanUtils.copyProperties(credentialEntity, credentialVO);
                setNodeListAndBranch(credentialVO, credentialEntity, searchCredentialByPageDTO.getSelectBox());
                credentialVOS.add(credentialVO);
            });
        }
        pageInfo.setList(credentialVOS);
        return pageInfo;
    }

    /**
     * 如果是选项框使用的，就不需要多余参数赋值，提高选项框赋值
     * @param credentialVO
     * @param credentialEntity
     * @param selectBox
     */
    private void setNodeListAndBranch(CredentialVO credentialVO, CredentialEntity credentialEntity, Boolean selectBox) {
        if (selectBox != null && selectBox == true) {
            log.debug("选项框可以不做多余的参数查询，提高性能");
        } else {
            List<NodeEntity> nodeEntities = nodeMapper.getNodeByCredentialUuid(credentialEntity.getUuid());
            if (CollectionUtils.isNotEmpty(nodeEntities)) {
                List<NodeEntity> cleanVsyNodes = nodeEntities.stream().filter(n -> !n.getIp().contains("(")).collect(Collectors.toList());
                credentialVO.setNodeList(cleanVsyNodes);
            }
            List<Branch> branches = remoteBranchService.getBranchListByLevel(credentialEntity.getBranchLevel());
            if (CollectionUtils.isNotEmpty(branches)) {
                credentialVO.setBranchName(branches.get(0).getBranchName());
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchModify(UpdateCredentialDTO updateCredentialDTO, Authentication authentication) throws IllegalAccessException {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(authentication.getName());
        int count = 0;
        if (userInfoDTO != null && "00".equalsIgnoreCase(userInfoDTO.getBranchLevel())) {
            List<UpdateCredentialDTO> updateCredentialDTOS = new ArrayList<>();
            String[] uuidS = updateCredentialDTO.getUuid().split(",");
            for (String uuid : uuidS) {
                UpdateCredentialDTO updateCredentialDTO1 = new UpdateCredentialDTO();
                updateCredentialDTO1.setBranchLevel(updateCredentialDTO.getBranchLevel());
                updateCredentialDTO1.setUuid(uuid);
                updateCredentialDTOS.add(updateCredentialDTO1);
            }
            count = credentialMapper.modifyBatchBranch(updateCredentialDTOS);
        } else {
            throw new IllegalAccessException("当前用户非总部用户，不允许对凭据进行分配组");
        }
        return count;
    }

    @Override
    public List<CredentialEntity> getCredentialList(String userName) {
        SearchCredentialByPageDTO searchCredentialByPageDTO = new SearchCredentialByPageDTO();
        String branchLevel = remoteBranchService.likeBranch(userName);
        searchCredentialByPageDTO.setBranchLevel(branchLevel);
        return credentialMapper.listByParam(searchCredentialByPageDTO);
    }

    public static void main(String[] args) {
        List<NodeEntity> nodeEntities = new ArrayList<>();
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setIp("1.2.3.2(1)");
        nodeEntities.add(nodeEntity);
        NodeEntity nodeEntity1 = new NodeEntity();
        nodeEntity1.setIp("1.2.3.2");
        nodeEntities.add(nodeEntity1);
        List<NodeEntity> cleanVsyNodes = nodeEntities.stream().filter(n -> !n.getIp().contains("(")).collect(Collectors.toList());
        cleanVsyNodes.forEach(e->{
            System.out.println(e.getIp());
        });
    }
}
