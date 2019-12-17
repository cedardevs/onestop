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

  public static final Map<String, String> filterableFields = [
      'beginDate'                : 'dateTimeFilter',
      'endDate'                  : 'dateTimeFilter',
      'beginYear'                : 'yearFilter',
      'endYear'                  : 'yearFilter',
      'spatialBounding'          : 'geometryFilter',
      'isGlobal'                 : 'excludeGlobalFilter',
      'internalParentIdentifier' : 'collectionFilter'
  ]

  static {
    facetNameMappings.each { String name, String field ->
      filterableFields.put(field, "facetFilter with 'name' value of '$name'")
    }
  }

  public static Map<String, Map> generateAttributesInfo(Map<String, Map> esMapping, String groupName = null) {
    Map<String, Map> attributesInfo = new HashMap<>()
    Map<String, Map> fields = esMapping.properties
    fields.each { name, details ->
      String fieldName = groupName == null ? name : "$groupName.$name"
      if(details.type == 'nested') {
        attributesInfo.putAll(generateAttributesInfo(details, fieldName))
      }
      else {
        attributesInfo.put(fieldName, [
            queryable: isQueryable(details),
            exactMatchRequired : isExactMatchRequired(details),
            applicableFilter : filterableFields.get(name) ?: 'None'
        ])
      }
    }

    return attributesInfo
  }

  private static boolean isQueryable(Map fieldMappingDetails) {
    return fieldMappingDetails.index != false
  }

  private static Boolean isExactMatchRequired(Map fieldMappingDetails) {
    if(isQueryable(fieldMappingDetails)) {
      return fieldMappingDetails.type != 'text'
    }
    return null
  }
}
