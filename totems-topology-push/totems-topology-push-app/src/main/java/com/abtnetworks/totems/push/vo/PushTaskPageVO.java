package com.abtnetworks.totems.push.vo;

import lombok.Data;

import java.util.List;

@Data
public class PushTaskPageVO {
    int total;

    List<PushTaskVO> list;
}
