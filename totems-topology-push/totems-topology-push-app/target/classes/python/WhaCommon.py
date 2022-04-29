# -*- coding: utf-8 -*-

import pexpect
import re
import uuid
import shutil
import os
import time

os.environ["TERM"] = "xterm"


class WrongPasswordError(Exception): pass


class WrongResultError(Exception): pass


class DefaultError(Exception): pass


def get_arguments():

    result = {}
    while True:
        reply = raw_input()
        if reply == '':
            break
        myindex = reply.find('=')
        if myindex == -1:
            continue
        key = reply[:myindex]
        value = ''
        if myindex != len(reply) - 1:
            value = reply[myindex + 1:]
        result[key] = value
    return result


def expect_exact(cli, prompts, timeout=-1):

    searchlen = 0
    for myp in prompts:
        if isinstance(myp, basestring):
            if len(myp) > searchlen:
                searchlen = len(myp)

    #如果无论如何都无法匹配到提示符，可以考虑将此处的searchwindowsize改为None
    #i = cli.expect_exact(prompts, timeout=timeout, searchwindowsize=None)
    i = cli.expect_exact(prompts, timeout=timeout, searchwindowsize=searchlen + 3)
    return i


#打印cli输出
def print_cli_output(output):
    if output is None:
        return

    mylist = output.split('\n')
    for mys in mylist:
        print '    ' + mys

    return

def remove_known_host(host):
    """
    在known_hosts中删除host的key
    sed -i '/^192.168.215.33/d' ~/.ssh/known_hosts
    """
    os.system("sed -i '/^{0} /d' ~/.ssh/known_hosts".format(host))

def open_ssh(user, passwd, host, port=22, open_timeout=30, default_timeout=60,
             cont_prompt=['continue connecting (yes/no)?', 'continue connecting (yes/no/[fingerprint])?'],
             passwd_prompt=['password:', 'Password:'],
             cmd_prompt=['>','#','%','$'],
             other_prompts=['[Y/N]:','(yes/no):'],
             dimensions=(5100, 5100)):
    """
    打开ssh会话
    user：ssh用户名
    passwd：ssh密码
    host：ssh连接IP
    port：ssh连接端口
    open_timeout：ssh连接超时
    default_timeout：命令超时
    cont_prompt：ssh连接时的提示，例如continue connecting (yes/no)?
    passwd_prompt：ssh连接时，提示输入密码，例如password:
    cmd_prompt：设备输入命令后，显示的终端提示符
    other_prompts：一些特殊的提示消息：例如密码过期了：The password needs to be changed. Change now? [Y/N]:
    dimensions：连接窗口的大小，格式为(row, column)，某些设备会强制换行，需手动设置窗口的大小
    """

    # remove_known_host(host)
    
    if (port is None) or (port < 1):
        port = 22

    cmdline = 'ssh {0}@{1} -p {2}'.format(user, host, port)
    cmdline_v1 = 'ssh -1 {0}@{1} -p {2}'.format(user, host, port)

    print '> ' + cmdline

    cmd = pexpect.spawn(cmdline, timeout=default_timeout, dimensions=dimensions, maxread=5000)
    try:
        first_time = True
        while True:
            prompts = [pexpect.EOF] + passwd_prompt + cont_prompt
            time.sleep(3)
            i = expect_exact(cmd, prompts, timeout=open_timeout)
            if i == 0:
                # EOF
                if first_time:
                    first_time = False
                    # 尝试sshv1
                    print_cli_output(cmd.before)
                    cmd.close()
                    print 'failed.retry with SSHv1:'
                    print '>' + cmdline_v1
                    cmd = pexpect.spawn(cmdline_v1, timeout=default_timeout, dimensions=dimensions, maxread=5000)
                    continue
                else:
                    raise pexpect.EOF("Failed to connect with SSH v1.")
            elif i < len(passwd_prompt) + 1:
                cmd.sendline(passwd)
                break
            else:
                cmd.sendline('yes')
                expect_exact(cmd, passwd_prompt, timeout=open_timeout)
                cmd.sendline(passwd)
                break

        prompts = passwd_prompt + other_prompts + cmd_prompt
        loopcount = 0
        while loopcount < 4 :
            loopcount = loopcount +1
            i = expect_exact(cmd, prompts, timeout=open_timeout)
            if i < len(passwd_prompt):
                raise WrongPasswordError
            elif i < len(passwd_prompt) + len(other_prompts):
                if prompts[i] == '[Y/N]:':
                    cmd.sendline('N')
                if prompts[i] == '(yes/no):':
                    cmd.sendline('no')
                if prompts[i] == '[y]:':
                    cmd.sendline('y')
            else:
                print user + ' login...'
                return cmd

    except pexpect.EOF as e:
        print 'EOF'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        print_cli_output( cmd.before )
        cmd.close()
        raise e
    except WrongPasswordError as e:
        print 'Password Wrong'
        print_cli_output( cmd.before + cmd.after )
        cmd.close()
        raise e

    return


def open_telnet(user, passwd, host, port=23, open_timeout=30, default_timeout=60,
                user_prompt=['Username:', 'username:', 'login:', 'Login:'],
                passwd_prompt=['password:', 'Password:'],
                cmd_prompt=['>','#','%','$'],
                other_prompts=['[Y/N]:','(yes/no):'],
                dimensions=(200, 510)):
    """
    打开telnet会话
    user：telnet用户名
    passwd：telnet密码
    host：telnet连接IP
    port：telnet连接端口
    open_timeout：ssh连接超时
    default_timeout：命令超时
    cont_prompt：ssh连接时的提示，例如continue connecting (yes/no)?
    passwd_prompt：ssh连接时，提示输入密码，例如password:
    cmd_prompt：设备输入命令后，显示的终端提示符
    other_prompts：一些特殊的提示消息：例如密码过期了：The password needs to be changed. Change now? [Y/N]:
    dimensions：连接窗口的大小，格式为(row, column)，某些设备会强制换行，需手动设置窗口的大小
    """

    if (port is None) or (port < 1):
        port = 23

    cmdline = 'telnet -l {0} {1} {2}'.format(user, host, port)
    print '> ' + cmdline

    cmd = pexpect.spawn(cmdline, timeout=default_timeout, dimensions=dimensions, maxread=5000)
    try:
        prompts = user_prompt + passwd_prompt
        time.sleep(3)
        i = expect_exact(cmd, prompts, timeout=open_timeout)
        if i < len( user_prompt ):
            # 不同于ssh，telnet需要输入用户名
            cmd.sendline(user)
            expect_exact(cmd, passwd_prompt, timeout=open_timeout)
        cmd.sendline(passwd)

        prompts = user_prompt + passwd_prompt + other_prompts + cmd_prompt

        loopcount = 0
        while loopcount < 4 :   
            loopcount = loopcount +1
            i = expect_exact(cmd, prompts, timeout=open_timeout)
            if i < len(user_prompt) + len(passwd_prompt):
                raise WrongPasswordError
            elif i < len(user_prompt) + len(passwd_prompt) + len(other_prompts):
                if prompts[i] == '[Y/N]:':
                    cmd.sendline('N')
                if prompts[i] == '(yes/no):':
                    cmd.sendline('no')
            else:
                print user + ' login...'
                return cmd

    except pexpect.EOF as e:
        print 'EOF'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except WrongPasswordError as e:
        print 'Password Wrong'
        print_cli_output(cmd.before + cmd.after)
        cmd.close()
        raise e
    except Exception as e:
        print 'Exception: ' + repr(e)
        print_cli_output(cmd.before)
        cmd.close()
        raise e

    return


def run_cmd(cli, cmd_str, more_str=['--More--'], prompt_str=['#','>','%','$'], timeout=-1,
            cleanup_pattern=r'--More-- \r\s+\r',
            min_result_linecount=8,
            nextpage_str=' ',
            nextpage_delay_second=0.2, #ENG-198
            retry_timeout=5):  #ENG-569
    """
    执行cli命令
    more_str：匹配翻页提示，例如'--More--'；如果不需要翻页，保持为空[]
    prompt_str：匹配终端提示符
    timeout：命令超时时间
    cleanup_pattern：替换cli输出中的一些输出，例如'--More--'
    min_result_linecount：最少输出行数，如果小于设置值，则产生异常
    nextpage_str：发送到cli的字符串，用于翻页，一般是空格
    nextpage_delay_second：翻页延迟，发送翻页字符串后的等待时间
    retry_timeout：有时候某条命令输出反馈特别慢，需要额外等待一些时间
    """
    

    try:

        if more_str is None:
            more_str = []

        print '> ' + cmd_str

        cli.sendline(cmd_str)

        mylist = more_str + prompt_str

        result = ''
        while True:
            i = expect_exact(cli, mylist, timeout=timeout)
            result = result + cli.before + cli.after
            if i < len(more_str):
                time.sleep(nextpage_delay_second)
                cli.send(nextpage_str)
                continue
            else:
                try:
                    hasMore = False
                    while True:
                        i = expect_exact(cli, mylist, retry_timeout)
                        result = result + cli.before + cli.after
                        if i < len(more_str):
                            time.sleep(nextpage_delay_second)
                            cli.send(nextpage_str)
                            hasMore = True
                            break

                    if hasMore:
                        continue
                except pexpect.EOF as e:
                    result = result + cli.before
                except pexpect.TIMEOUT as e:
                    result = result + cli.before
                break

    except pexpect.EOF as e:
        print 'EOF'
        result = result + cli.before
        print_cli_output( result )
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        result = result + cli.before
        print_cli_output( result )
        raise e

    linecount = result.count( '\n')
    if linecount < min_result_linecount:
        print 'Wrong Result: Returned lines count should be over ' + str(min_result_linecount) +': '
        print_cli_output(result)
        raise WrongResultError

    result = re.sub(cleanup_pattern, '', result)
    result = result.replace('\r\r\n', '\r\n')
    return result


#返回输出配置文件的文件名
def get_config_filename(pluginId='', hostId='', opUuid='', charset=None, miscStr=None):
    if (charset is None) and (miscStr is None):
        ret = '{0}_{1}_{2}_.conf'.format(pluginId, hostId, opUuid)
    elif charset is None:
        ret = '{0}_{1}_{2}_.{3}.conf'.format(pluginId, hostId, opUuid, miscStr)
    elif miscStr is None:
        ret = '{0}_{1}_{2}_{3}_.conf'.format(pluginId, hostId, opUuid, charset)
    else:
        ret = '{0}_{1}_{2}_{3}_.{4}.conf'.format(pluginId, hostId, opUuid, charset, miscStr)

    return ret

#返回输出路由表的文件名
def get_routing_filename(pluginId='', hostId='', opUuid='', charset=None, miscStr=None):
    if (charset is None) and (miscStr is None):
        ret = '{0}_{1}_{2}_.rt.conf'.format(pluginId, hostId, opUuid)
    elif charset is None:
        ret = '{0}_{1}_{2}_.{3}.rt.conf'.format(pluginId, hostId, opUuid, miscStr)
    elif miscStr is None:
        ret = '{0}_{1}_{2}_{3}_.rt.conf'.format(pluginId, hostId, opUuid, charset)
    else:
        ret = '{0}_{1}_{2}_{3}_.{4}.rt.conf'.format(pluginId, hostId, opUuid, charset, miscStr)

    return ret


def save_and_deploy_config(config_txt, whalehome_dir, config_filename, routing_txt=None, routing_filename=None):
    staging_dir = whalehome_dir + '/temp/configFileStaging/'
    if not os.path.isdir(staging_dir):
        os.makedirs(staging_dir)

    tmp_uuid = str(uuid.uuid1()).replace('-', '')
    tmpfile_path = staging_dir + config_filename + '.' + tmp_uuid
    print 'save config file: ' + tmpfile_path
    tmpfile = open(tmpfile_path, "w")
    tmpfile.write(config_txt)
    tmpfile.close()

    if (routing_txt is not None) and (routing_filename is not None):
        tmp_rt_path = staging_dir + routing_filename + '.' + tmp_uuid
        print 'save route file: ' + tmp_rt_path
        tmpfile = open(tmp_rt_path, "w")
        tmpfile.write(routing_txt)
        tmpfile.close()

    if (routing_txt is not None) and (routing_filename is not None):
        inbox_rt_file = whalehome_dir + '/configFileInbox/' + routing_filename
        print 'move to ' + inbox_rt_file
        shutil.rmtree(inbox_rt_file, ignore_errors=True)
        shutil.move(tmp_rt_path, inbox_rt_file)

    inbox_file = whalehome_dir + '/configFileInbox/' + config_filename
    print 'move to ' + inbox_file

    shutil.rmtree(inbox_file, ignore_errors=True)
    shutil.move(tmpfile_path, inbox_file)
    return

def open_zssh(user, passwd, host, port=22, open_timeout=30, default_timeout=60,
             cont_prompt=['continue connecting (yes/no)?', 'continue connecting (yes/no/[fingerprint])?'],
             passwd_prompt=['password:', 'Password:'],
             cmd_prompt=['>','#','%','$'], dimensions=(200, 510)):
    """
    参数同open_ssh
    """

    if (port is None) or (port < 1):
        port = 22

    cmdline = '/bin/bash'
    print '> ' + cmdline

    cmd = pexpect.spawn(cmdline, timeout=default_timeout, dimensions=dimensions, maxread=5000)
    try:
        cmdline = 'stty raw'
        print '>', cmdline
        cmd.sendline(cmdline)

        time.sleep(1)
        cmdline = 'zssh -z ^x {0}@{1} -p {2}'.format(user, host, port)
        print '>', cmdline
        cmd.sendline(cmdline)

        prompts = passwd_prompt + cont_prompt
        time.sleep(3)
        i = expect_exact(cmd, prompts, timeout=open_timeout)
        if i < len(passwd_prompt):
            cmd.sendline(passwd)
        else:
            cmd.sendline('yes')
            expect_exact(cmd, passwd_prompt, timeout=open_timeout)
            cmd.sendline(passwd)

        prompts = passwd_prompt + cmd_prompt
        i = expect_exact(cmd, prompts, timeout=open_timeout)
        if i < len(passwd_prompt):
            raise WrongPasswordError
        else:
            print user + ' login...'
            return cmd

    except pexpect.EOF as e:
        print 'EOF'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        print_cli_output( cmd.before )
        cmd.close()
        raise e
    except WrongPasswordError as e:
        print 'Password Wrong'
        print_cli_output( cmd.before + cmd.after )
        cmd.close()
        raise e
    except Exception as e:
        print 'Exception: ' + repr(e)
        print_cli_output( cmd.before )
        cmd.close()
        raise e

    return


def get_file_content( path_file_name ):
    with open(path_file_name, 'r') as myfile:
        data = myfile.read()
        return data


def get_value( value, default_value):
    if value is None:
        return default_value
    return value
    

#从跳板机跳转ssh登录设备
def open_jump_ssh(cmd, user, passwd, host, port=22, open_timeout=30, default_timeout=60,
                user_prompt=['Username:', 'username:', 'login:', 'Login:'],
                passwd_prompt=['password:', 'Password:'],
                cmd_prompt=['>','#','%','$'],
                other_prompts=['[Y/N]:','(yes/no):','(yes/no)?','Do you want to save the server public key? [Y/N]:'],
                dimensions=(200, 510)):
    """
    打开telnet会话
    user：telnet用户名
    passwd：telnet密码
    host：telnet连接IP
    port：telnet连接端口
    open_timeout：ssh连接超时
    default_timeout：命令超时
    cont_prompt：ssh连接时的提示，例如continue connecting (yes/no)?
    passwd_prompt：ssh连接时，提示输入密码，例如password:
    cmd_prompt：设备输入命令后，显示的终端提示符
    other_prompts：一些特殊的提示消息：例如密码过期了：The password needs to be changed. Change now? [Y/N]:
    dimensions：连接窗口的大小，格式为(row, column)，某些设备会强制换行，需手动设置窗口的大小
    """

    if (port is None) or (port < 1):
        port = 23

    cmdline = 'ssh {0}@{1} -p {2}'.format(user, host, port)
    print '> ' + cmdline

    cmd.sendline(cmdline)
    try:
        prompts = passwd_prompt + other_prompts + cmd_prompt
        loopcount = 0
        second_passwd_prompt = False
        while loopcount < 3 :
            loopcount = loopcount +1
            i = expect_exact(cmd, prompts, timeout=open_timeout)
            print 'prompts2: ' + prompts[i]
            if i < len(passwd_prompt):
                #如果是第二次出现passwd提示符,则认为密码错误
                if second_passwd_prompt:
                    raise WrongPasswordError
                else:
                    print 'input passwd... '
                    cmd.sendline(passwd)
                    second_passwd_prompt = True
            elif i < len(passwd_prompt) + len(other_prompts):
                if prompts[i] == '[Y/N]:':
                    cmd.sendline('y')
                if prompts[i] == '(yes/no):':
                    cmd.sendline('yes')
                if prompts[i] == '(yes/no)?':
                    cmd.sendline('yes')
                if prompts[i] == 'Do you want to save the server public key? [Y/N]:':
                    cmd.sendline('n')
            else:
                print user + ' jump login...'
                return cmd
                

    except pexpect.EOF as e:
        print 'EOF'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except WrongPasswordError as e:
        print 'Password Wrong'
        print_cli_output(cmd.before + cmd.after)
        cmd.close()
        raise e
    except Exception as e:
        print 'Exception: ' + repr(e)
        print_cli_output(cmd.before)
        cmd.close()
        raise e

    return


#ssh免密：登录时不需要输入密码
def open_ssh_nopasswd(user, passwd, host, port=22, open_timeout=30, default_timeout=60,
             cont_prompt=['continue connecting (yes/no)?', 'continue connecting (yes/no/[fingerprint])?'],
             passwd_prompt=['password:', 'Password:'],
             cmd_prompt=['>','#','%','$'],
             other_prompts=['[Y/N]:','(yes/no):'],
             dimensions=(2000, 5100)):
    """
    打开ssh会话
    user：ssh用户名
    passwd：ssh密码
    host：ssh连接IP
    port：ssh连接端口
    open_timeout：ssh连接超时
    default_timeout：命令超时
    cont_prompt：ssh连接时的提示，例如continue connecting (yes/no)?
    passwd_prompt：ssh连接时，提示输入密码，例如password:
    cmd_prompt：设备输入命令后，显示的终端提示符
    other_prompts：一些特殊的提示消息：例如密码过期了：The password needs to be changed. Change now? [Y/N]:
    dimensions：连接窗口的大小，格式为(row, column)，某些设备会强制换行，需手动设置窗口的大小
    """

    if (port is None) or (port < 1):
        port = 22

    cmdline = 'ssh {0}@{1} -p {2}'.format(user, host, port)
    cmdline_v1 = 'ssh -1 {0}@{1} -p {2}'.format(user, host, port)

    print '> ' + cmdline

    cmd = pexpect.spawn(cmdline, timeout=default_timeout, dimensions=dimensions, maxread=5000)
    try:
        first_time = True
        while True:
            prompts = [pexpect.EOF] + cmd_prompt + cont_prompt + other_prompts
            time.sleep(3)
            i = expect_exact(cmd, prompts, timeout=open_timeout)
            if i == 0:
                # EOF
                if first_time:
                    first_time = False
                    # 尝试sshv1
                    print_cli_output(cmd.before)
                    cmd.close()
                    print 'failed.retry with SSHv1:'
                    print '>' + cmdline_v1
                    cmd = pexpect.spawn(cmdline_v1, timeout=default_timeout, dimensions=dimensions, maxread=5000)
                    continue
                else:
                    raise pexpect.EOF("Failed to connect with SSH v1.")
            elif i < len(cmd_prompt) + 1:
                print user + ' login...'
                return cmd
            else:
                if prompts[i] == '[Y/N]:':
                    cmd.sendline('N')
                if prompts[i] == '(yes/no):':
                    cmd.sendline('no')
                if prompts[i] == '[y]:':
                    cmd.sendline('y')
                break

    except pexpect.EOF as e:
        print 'EOF'
        print_cli_output(cmd.before)
        cmd.close()
        raise e
    except pexpect.TIMEOUT as e:
        print 'TIMEOUT'
        print_cli_output( cmd.before )
        cmd.close()
        raise e
    except WrongPasswordError as e:
        print 'Password Wrong'
        print_cli_output( cmd.before + cmd.after )
        cmd.close()
        raise e

    return

