#!/usr/bin/env python
__author__ = 'wrowland'


def dscovr_file_name_parser(dscovr_data_file_name):
    import datetime

    dscovr_data_file_name_short = dscovr_data_file_name.split('/')[-1]
    dscovr_file_info_list = dscovr_data_file_name_short.split('_')

    environment_type = dscovr_file_info_list[0]
    data_type = dscovr_file_info_list[1]
    satellite = dscovr_file_info_list[2]
    start_date_str = dscovr_file_info_list[3]
    start_datetime = datetime.datetime(int(start_date_str[1:5]), int(start_date_str[5:7]), int(start_date_str[7:9]),
                                       int(start_date_str[9:11]), int(start_date_str[11:13]),
                                       int(start_date_str[13:15]))
    end_date_str = dscovr_file_info_list[4]
    end_datetime = datetime.datetime(int(end_date_str[1:5]), int(end_date_str[5:7]), int(end_date_str[7:9]),
                                       int(end_date_str[9:11]), int(end_date_str[11:13]),
                                       int(end_date_str[13:15]))
    proc_date_str = dscovr_file_info_list[5]
    proc_datetime = datetime.datetime(int(proc_date_str[1:5]), int(proc_date_str[5:7]), int(proc_date_str[7:9]),
                                       int(proc_date_str[9:11]), int(proc_date_str[11:13]),
                                       int(proc_date_str[13:15]))
    embargo_flag = dscovr_file_info_list[6][0:3] == 'emb'

    dscovr_file_info_dict = {'environment_type': environment_type, 'data_type': data_type, 'satellite': satellite,
    'start_datetime': start_datetime, 'end_datetime': end_datetime, 'proc_datetime': proc_datetime,
    'embargo_flag': embargo_flag}

    return dscovr_file_info_dict

if __name__ == '__main__':
    import sys

    dscovr_data_file_name_outer = sys.argv[1]

    print dscovr_file_name_parser(dscovr_data_file_name_outer)


