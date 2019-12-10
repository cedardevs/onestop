package org.cedar.onestop.api.search.service

class DocumentationService {

  public static final Map<String, String> facetNameMappings = [
      'dataFormats'         : 'dataFormat',
      'linkProtocols'       : 'linkProtocol',
      'serviceLinkProtocols': 'serviceLinkProtocol',
      'science'             : 'gcmdScience',
      'services'            : 'gcmdScienceServices',
      'locations'           : 'gcmdLocations',
      'instruments'         : 'gcmdInstruments',
      'platforms'           : 'gcmdPlatforms',
      'projects'            : 'gcmdProjects',
      'dataCenters'         : 'gcmdDataCenters',
      'horizontalResolution': 'gcmdHorizontalResolution',
      'verticalResolution'  : 'gcmdVerticalResolution',
      'temporalResolution'  : 'gcmdTemporalResolution',
  ]
}
