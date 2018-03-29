#!/usr/bin/env python
__author__ = 'wrowland'


# This is not what I'd call an elegant solution, but it will work and I'd rather not invest too much time in doing code
# that constructs everything from scratch when this can so clearly be done via a template and a template should be at
# least as resilient to future changes that can't be specified at this time.
# William still needs to
# 0) Will att, fc0, fc1, rt0, rt1 (not intended for most science users) will go through this
# 0.5) If 0 is true, then create templates for att, fc0, fc1, rt0, rt1
# dscovrIsoLiteHackyWorkaround needs to do the following
#  1) Open dscovrIsoLiteTemplate.xml
#  2) Replace dscovrFileNamePlaceholder with file name
#  3) Replace dscovrDataTypePlaceholder with 3-letter filetype identifier.
#  4) Replace todaysDatePlaceholder with today's date in ISO8601 format
#  5) Replace dscovrIngestDatePlaceholder with date that file was written to disk or CI's 'lastUpdated' field.
#  6) Replace dscovrGranuleStartDateTimePlaceholder with ISO8601 datetime string for beginning of file window.
#  6.5) Replace syncYYYY with YYYY from file name
#  6.6) replace syncMM with MM from file name
#  [TBD] 6.7) Validate that https://www.ngdc.noaa.gov/dscovr/data/syncYYYY/syncMM/dscovrFileNamePlaceholder.gz exists
#  7) Replace dscovrGranuleEndDateTimePlaceholder with ISO8601 datetime string for end of file window.
#  8) Replace dscovrEnterpriseDownloadLinkPlaceholder with an enterprise link if that's provided.
#  9) Replace dscovrEnterpriseDownloadLinkProtocolPlaceholder with the protocol for the enterprise link if provided.
# 10) Save modified xml as a dscovrIsoLite.xml record for the granule.
def dscovrIsoLiteHackyWorkaround (input_json, logLevel = 'Error',
                                  IsoLiteTemplateLocation = './scripts/dscovrIsoLiteTemplate.xml'):
    import json
    import dscovr_file_name_parser
    import datetime
    import re
    import sys
    import pdb

    # NCEI-CO Enterprise Agile team has currently agreed to pass one argument to this function.
    # If a second argument was agreed to, it would make sense for it to be a full path to this template.


    # Read in the input_json provided
    try:
        input_json_dict = json.loads(input_json)
    except:
        # json.loads could not decode input_json
        raise Exception('input_json does not appear to be valid json format')
        sys.exit()

    # Try to extract all of the necessary information from input_json and the filename.
    try:
        common_ingest_time = input_json_dict['lastUpdated']
        common_ingest_time = common_ingest_time[0:10]
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract lastUpdated.')
        sys.exit()
    try:
        # 2) Replace dscovrFileNamePlaceholder with the following
        dscovrFileName = input_json_dict['relativePath']
        dscovrFileName = re.sub(r'.gz', '', dscovrFileName)
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract relativePath')
        sys.exit()
    try:
        parsed_file_name_dict = dscovr_file_name_parser.dscovr_file_name_parser(dscovrFileName)
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to parse filename.')
        sys.exit()
    try:
        # 3) Replace dscovrDataTypePlaceholder with the following
        dscovrDataType = parsed_file_name_dict['data_type']
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract data_type.')
        sys.exit()
    try:
        # 4) Replace todaysDatePlaceholder with appropriate value
        todaysDate = datetime.date.today().isoformat()

        # 5) Replace dscovrIngestDatePlaceholder with the following
        dscovrIngestDate = common_ingest_time
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract todaysDatePlaceholder.')
        sys.exit()
    try:
        #6 Replace dscovrGranuleStartDateTimePlaceholder
        dscovrGranuleStartDateTimeObject=parsed_file_name_dict['start_datetime']
        dscovrGranuleStartDateTime = dscovrGranuleStartDateTimeObject.isoformat()
        #  6.5) Replace syncYYYY with YYYY from file name
        syncYYYY = str(dscovrGranuleStartDateTimeObject.year + 10000)[1:5]

        #  6.6) replace syncMM with MM from file name
        syncMM = str(dscovrGranuleStartDateTimeObject.month + 100)[1:3]

    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract start_datetime.')
        sys.exit()
    try:
        # TODO  6.7) Validate https://www.ngdc.noaa.gov/dscovr/data/syncYYYY/syncMM/dscovrFileNamePlaceholder.gz
        # Currently this is not implemented, should be implemented by enterprise system in the long term instead of this
        # script.

        #7 Replace dscovrGranuleEndDateTimePlaceholder
        dscovrGranuleEndDateTime = parsed_file_name_dict['end_datetime'].isoformat()

        with open(IsoLiteTemplateLocation, 'r') as fileDataObject:
            IsoLiteXML = fileDataObject.read()
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract end_datetime.')
        sys.exit()

    try:
        #8 IF dscovrEnterpriseDownloadLinkPlaceholder and dscovrEnterpriseDownloadLinkProtocolPlaceholder are present
        #   THEN replace dscovrEnterpriseDownloadLinkPlaceholder and dscovrEnterpriseDownloadLinkProtocolPlaceholder
        if 'links' in input_json_dict:
            link = input_json_dict['links'][0]
            dscovrEnterpriseDownloadLinkUrl = link['linkUrl']
            dscovrEnterpriseDownloadLinkName = link['linkName']
            dscovrEnterpriseDownloadLinkProtocol = link['linkProtocol']
            IsoLiteXML = re.sub(r'			<!-- commonAccessPlaceholderStart\n', '', IsoLiteXML)
            IsoLiteXML = re.sub(r'			commonAccessPlaceholderEnd -->\n', '', IsoLiteXML)
        else:
            if logLevel == 'Info': print('enterpriseDownloadLink and/or enterpriseDownloadLinkProtocol were missing from input_json_dict\n')
            #Add log message to this effect at some point after location/format agreed to.
            dscovrEnterpriseDownloadLinkUrl = 'NotAvailable'
            dscovrEnterpriseDownloadLinkName = 'NotAvailable'
            dscovrEnterpriseDownloadLinkProtocol = 'NotAvailable'
    except:
        # Something was missing, not currently tracking what.
        raise Exception('Unable to extract one or more of the of data with the input_json provided.')
        sys.exit()

    # Substitute the correct values into the template
    IsoLiteXML = re.sub(r'dscovrFileNamePlaceholder', dscovrFileName, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrDataTypePlaceholder', dscovrDataType, IsoLiteXML)
    IsoLiteXML = re.sub(r'todaysDatePlaceholder', todaysDate, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrIngestDatePlaceholder', dscovrIngestDate, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrGranuleStartDateTimePlaceholder', dscovrGranuleStartDateTime, IsoLiteXML)
    IsoLiteXML = re.sub(r'syncYYYY', syncYYYY, IsoLiteXML)
    IsoLiteXML = re.sub(r'syncMM', syncMM, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrGranuleEndDateTimePlaceholder', dscovrGranuleEndDateTime, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrEnterpriseDownloadLinkUrlPlaceholder', dscovrEnterpriseDownloadLinkUrl, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrEnterpriseDownloadLinkNamePlaceholder', dscovrEnterpriseDownloadLinkName, IsoLiteXML)
    IsoLiteXML = re.sub(r'dscovrEnterpriseDownloadLinkProtocolPlaceholder', dscovrEnterpriseDownloadLinkProtocol, IsoLiteXML)

    # Print detailed info
    if logLevel == 'Info':
        print('input_json_dict\n', input_json_dict, '\n')
        print(json.dumps(input_json_dict))
        print('common_ingest_time\n' + common_ingest_time + '\n')
        print('parsed_file_name_dict\n', parsed_file_name_dict, '\n')
        print('todaysDate\n', todaysDate, '\n')
        print('dscovrIngestDate\n', dscovrIngestDate, '\n')
        print('dscovrGranuleStartTime\n', dscovrGranuleStartDateTime, '\n')
        print('syncYYYY\n', syncYYYY, '\n')
        print('syncMM\n', syncMM, '\n')
        print('dscovrGranuleEndDateTime\n', dscovrGranuleEndDateTime, '\n')
        print('IsoLiteTemplateLocation\n', IsoLiteTemplateLocation, '\n')
        print('IsoLiteXML\n', IsoLiteXML, '\n')
        print('outFileName\n', outFileName, '\n')

    publish = not parsed_file_name_dict['embargo_flag']
    result = {'publish': publish, 'isoXml': IsoLiteXML}
    return json.dumps(result)


if __name__ == '__main__':
    import sys

#    logLevelOuter='Info'
#    IsoLiteTemplateLocation='/home/wrowland/Desktop/dscovrIsoLiteTemplate.xml'
    example_json = '{"dataStream": "dscovr", "trackingId": "1b0a2b2a-42d9-410c-b713-bc72ac31a1d9", "checksum": "c3d8ca4ec4b0aa22f900a8d66b3dd1f8", "relativePath": "oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "path": "/dscovr/valid/oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "fileSize": 50566, "lastUpdated":"2018-02-02T16:01:10Z"}'
    example_with_enterprise_link_json = '{"dataStream": "dscovr", "trackingId": "1b0a2b2a-42d9-410c-b713-bc72ac31a1d9", "checksum": "c3d8ca4ec4b0aa22f900a8d66b3dd1f8", "relativePath": "oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "path": "/dscovr/valid/oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "fileSize": 50566, "lastUpdated": "2018-02-02T16:01:10Z", "enterpriseDownloadLink": "https://www.ngdc.noaa.gov/dscovr/next/", "enterpriseDownloadLinkProtocol": "https"}'
    example_incomplete_json = '{"dataStream": "dscovr", "trackingId": "1b0a2b2a-42d9-410c-b713-bc72ac31a1d9", "checksum": "c3d8ca4ec4b0aa22f900a8d66b3dd1f8", "relativePath": "oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "path": "/dscovr/valid/oe_f1m_dscovr_s20180201000000_e20180201235959_p20180202024241_pub.nc.gz", "fileSize": 50566}'

    arguments = sys.argv
    if len(arguments) == 2:
        #Get json argument from Agile code
        input_json = arguments[1]

        if input_json == 'test':
            input_json = example_json
        elif input_json == 'testWithEnterpriseLink':
            input_json = example_with_enterprise_link_json
        elif input_json == 'testIncomplete':
            input_json = example_incomplete_json
        elif input_json == 'stdin':
            input_json = sys.stdin.read()

        if 'logLevelOuter' in locals():
            print(dscovrIsoLiteHackyWorkaround(input_json, logLevel=logLevelOuter))
        else:
            print(dscovrIsoLiteHackyWorkaround(input_json))
    else:
        print('Requires input_json, json provided by the person/application calling it.')
        print('Example: "/path/to/code/dscovrIsoLiteConstructor.py  "' + example_json)
        print("Running the code with 'test' or 'testWithEnterpriseLink' as arguments will test code with known json")

