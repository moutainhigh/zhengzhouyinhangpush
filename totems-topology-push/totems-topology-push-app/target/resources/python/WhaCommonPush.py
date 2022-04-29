# -*- coding: utf-8 -*-

import json
import WhaCommon
import re

class DevicePushRequest(object):
    deviceInfo = None
    relationDeviceInfo = None
    policy = None
    interactiveObject = None
    isRevert = None
    commandLine = None

    def __init__(self, json_str):
        self.__dict__ = json.loads(json_str)


class DevicePushResult(object):
    data = {}

    def __init__(self):
        pass


def get_value( value, default_value):
    if value is None:
        return default_value
    return value


def get_device_push_request():

    request_str = ''
    while True:
        reply = raw_input()
        # an empty line means the end of input
        if reply == '':
            break
        request_str = request_str + reply

    result = DevicePushRequest(request_str)
    return result


def run_script(cli, script, result, err_prompt=['error', 'unrecognized', 'unknown', r"can't", r'refused', r'unexpected',
                                                r'fail'],
            more_str=['--More--'], prompt_str=['#','>','%','$',':'], timeout=-1,
            cleanup_pattern=r'--More-- \r\s+\r',
            min_result_linecount=1,
            nextpage_str=' ',
            nextpage_delay_second=0.2,  # ENG-198
            retry_timeout=1):
    """
    Run the script on this cli console. Parameter err_prompt is an String array of RegularExpress patterns.
    Parameter result is an DevicePushResult object to store result.
    Other parameters are the same as WhaCommon.run_cmd()

    :param cli: cli console. An object created by pexpect.spawn().
    :param script: script to run
    :param result: DevicePushResult object to store result
    :param err_prompt: error string to match result to detect error situation. a list of Regular Expression
    :return: none. All result is stored in 'result' object passed in.
    """

    output = ''
    lines = script.splitlines()

    try:
        for line in lines:
            myout = WhaCommon.run_cmd(cli, line, more_str=more_str, prompt_str=prompt_str,
                                      timeout=timeout, cleanup_pattern=cleanup_pattern,
                                      min_result_linecount=min_result_linecount,
                                      nextpage_str=nextpage_str, nextpage_delay_second=nextpage_delay_second,
                                      retry_timeout=retry_timeout)
            output = output + myout
            for myerr in err_prompt:
                mym = re.search(myerr, myout, re.IGNORECASE | re.MULTILINE)
                if mym is not None:
                    # found error
                    result.data["commandlineEcho"] = output
                    result.data["code"] = -1
                    return

        result.data["commandlineEcho"] = output
        result.data["code"] = 0
    except Exception as e:
        print 'Error:' + repr(e)
        result.data["msg"] = output + cli.before
        result.data["code"] = -1

    WhaCommon.print_cli_output(result.data["commandlineEcho"])

    return


def print_result(result):
    """
    Print DevicePushResult in json to stdout

    :param result: DevicePushResult object
    :return: None
    """
    json_str = result.data["commandlineEcho"]
    print '\n##ECHO-START##\n'+json_str + '\n##ECHO-END##\n'
    return



