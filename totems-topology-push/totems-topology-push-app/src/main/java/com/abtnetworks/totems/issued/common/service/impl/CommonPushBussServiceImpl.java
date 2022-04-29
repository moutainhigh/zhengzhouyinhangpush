package com.abtnetworks.totems.issued.common.service.impl;

import com.abtnetworks.totems.disposal.service.impl.DisposalOrderServiceImpl;
import com.abtnetworks.totems.disposal.service.impl.DisposalRollbackServiceImpl;
import com.abtnetworks.totems.issued.common.service.CommonPushBussService;
import com.abtnetworks.totems.push.service.impl.PushForbidCommandLineServiceImpl;
import com.abtnetworks.totems.push.service.impl.PushServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @Title:
 * @Description: 为了避免<p> 这些类冗余代码方法{@link PushServiceImpl } {@link DisposalOrderServiceImpl}
 * {@link DisposalRollbackServiceImpl} {@link PushForbidCommandLineServiceImpl} ..... 随着业务的增加，每次添加一个功能加个参数，需要多处修改，
 * 特地提出公共方法的类，便于一次修改，多处使用，避免遗漏带入问题
 * </p>push
 * @date 2020/10/19
 */
@Service
public class CommonPushBussServiceImpl implements CommonPushBussService {
}
