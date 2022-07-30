from ast import arguments
import argparse
import math
import os
from syslog import LOG_ERR
import typing
import yaml
import logging as log
from datetime import datetime
import re

log.basicConfig(filename='genLogReport.log', encoding='utf-8', level=log.DEBUG)

def log_to_list(file: str) -> list:
    ''' Phân tích log file và convert dữ liệu thành 1 list'''
    lines = []
    ret = []
    try:
        log.info("Opening file: {}".format(file))
        with open(file, 'r') as file:
            lines = file.read().split('\n')
            print(lines)
    except Exception as exc:
        return None

    for line in lines:
        print(line)
        dict = {
            'date': None,
            'time': None,
            'logger': None,
            'type': None,
            'location': None,
            'detail': None
        }
        match = re.search(r'\d{4}-\d{2}-\d{2}', line)
        if match != None:
            dict['date'] = match.group()
        
        match = re.search(r'\d{2}:\d{2}:\d{2}.\d{3}', line)
        if match != None:
            dict['time'] = match.group()

        dict['logger'] = "vtx_logger"

        match = re.search(r'warning|info|error|debug', line)
        if match != None:
            dict['type'] = match.group()

        match = re.search(r'\[\s.*.cpp:.*\s]', line)
        if match != None:
            dict['location'] = match.group()[2: -2]
            dict['detail'] = line.split(match.group())[1]
        
        ret.append(dict)

    print(ret)
    return ret


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser("Tạo báo cáo từ log file")
    parser.add_argument('-f', '--log-file', default='application.log',
                        help='Log file input')

    return parser.parse_args()

def main() -> None:
    args = parse_args()
    
    list = log_to_list(args.log_file)
    if list == None:
        log.error("Can not open log file {}".format(args.log_file))

if __name__ == '__main__' :
    main()
