package com.abtnetworks.totems.retrieval.service;

import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.retrieval.dto.RetrievalParamDto;

import java.util.Map;

public interface RetrievalService {

    ReturnResult getCommandline2(RetrievalParamDto retrievalParamDto);

    Map<String,String> getCommandline(RetrievalParamDto retrievalParamDto);

}
