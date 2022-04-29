# -*- coding: utf-8 -*-

import WhaCommon
import WhaCommonPush

from WhaCommonPush import DevicePushResult

MORE = '<--- More --->'
CLEANUP = r'<--- More --->\r\s+\r'
ERR_PROMPT=[ r'error', r'unrecognized', r'fail']
#PROMPT_STR = ['>', ']']

try:
    request = WhaCommonPush.get_device_push_request()

    commType = WhaCommonPush.get_value(request.deviceInfo["commType"], 'SSH')
    if commType == 'SSH':
        mycli = WhaCommon.open_ssh(request.deviceInfo["loginName"],
                                   request.deviceInfo["loginPassword"],
                                   request.deviceInfo["deviceIp"],
                                   port=WhaCommonPush.get_value(request.deviceInfo["port"], '22'))
    else:
        mycli = WhaCommon.open_telnet(request.deviceInfo["loginName"],
                                   request.deviceInfo["loginPassword"],
                                   request.deviceInfo["deviceIp"],
                                   port=WhaCommonPush.get_value(request.deviceInfo["port"], '23'))

    mycli.sendline('enable')
    j = WhaCommon.expect_exact(mycli, ['Password:', '#'], timeout=10)
    if j == 0:
        # need enable password
        mycli.sendline(request.deviceInfo["enablePassword"])
        k = WhaCommon.expect_exact(mycli, ['Password:', '#'], timeout=10)
        if k == 0:
            # wrong enable password
            print 'Wrong enable password'
            raise WhaCommon.WrongPasswordError
        else:
            print 'Entering enable mode...'

    output = WhaCommon.run_cmd(mycli, 'configure terminal',
                               retry_timeout=1,
                               min_result_linecount=1)

    output = WhaCommon.run_cmd(mycli, 'terminal width 200',
                               retry_timeout=1,
                               min_result_linecount=1)

    output = WhaCommon.run_cmd(mycli, 'disable',
                               retry_timeout=1,
                               min_result_linecount=1)

    result = DevicePushResult()

    WhaCommonPush.run_script(mycli, request.commandLine, result, err_prompt=ERR_PROMPT,
                             more_str=[MORE],
                             cleanup_pattern=CLEANUP)

    WhaCommonPush.print_result(result)

finally:
    # try to close the cli process and ignore any errors
    try:
        mycli.close()
    except:
        pass

