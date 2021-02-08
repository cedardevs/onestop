package org.cedar.onestop.api.search.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentationService {

  public static final Map<String, String> facetNameMappings = new HashMap<>();
  public static final Map<String, String> filterableFields = new HashMap<>();
  public static final List<String> summaryFields = List.of(
      "internalParentIdentifier",
      "title",
      "thumbnail",
      "spatialBounding",
      "beginDate",
      "beginYear",
      "endDate",
      "endYear",
      "links",
      "citeAsStatements",
      "serviceLinks",
      "filesize"
  );

  static {
    facetNameMappings.put("dataFormats"         , "dataFormat");
    facetNameMappings.put("linkProtocols"       , "links.linkProtocol");
    facetNameMappings.put("serviceLinkProtocols", "serviceLinkProtocol");
    facetNameMappings.put("science"             , "gcmdScience");
    facetNameMappings.put("services"            , "gcmdScienceServices");
    facetNameMappings.put("locations"           , "gcmdLocations");
    facetNameMappings.put("instruments"         , "gcmdInstruments");
    facetNameMappings.put("platforms"           , "gcmdPlatforms");
    facetNameMappings.put("projects"            , "gcmdProjects");
    facetNameMappings.put("dataCenters"         , "gcmdDataCenters");
    facetNameMappings.put("horizontalResolution", "gcmdHorizontalResolution");
    facetNameMappings.put("verticalResolution"  , "gcmdVerticalResolution");
    facetNameMappings.put("temporalResolution"  , "gcmdTemporalResolution");
    facetNameMappings.put("fileFormats"         , "fileFormat");
    facetNameMappings.put("linkAccessTypes"     , "links.linkFunction");

    filterableFields.put("beginDate"                , "dateTimeFilter");
    filterableFields.put("endDate"                  , "dateTimeFilter");
    filterableFields.put("beginYear"                , "yearFilter");
    filterableFields.put("endYear"                  , "yearFilter");
    filterableFields.put("spatialBounding"          , "geometryFilter");
    filterableFields.put("isGlobal"                 , "excludeGlobalFilter");
    filterableFields.put("internalParentIdentifier" , "collectionFilter");

    facetNameMappings.forEach( (name, field) ->
        filterableFields.put(field, "facetFilter with 'name' value of '" + name + "'")
    );
  }

  public static Map<String, Map> generateAttributesInfo(Map esMapping) {
    return generateAttributesInfo(esMapping, null);
  }

  public static Map<String, Map> generateAttributesInfo(Map<String, Map> esMapping, String groupName) {
    Map<String, Map> attributesInfo = new HashMap<>();
    Map<String, Map> fields = esMapping.get("properties");
    fields.forEach( (String name, Map details) -> {
      String fieldName = groupName == null ? name : groupName + "." + name;
      if(details.get("type") == "nested") {
        attributesInfo.putAll(generateAttributesInfo(details, fieldName));
      }
      else {
        Map<String, Object> info = new HashMap<>();
        info.put("queryable", isQueryable(details));
        info.put("exactMatchRequired", isExactMatchRequired(details));
        info.put("applicableFilter", filterableFields.get(name) == null ? "None" : filterableFields.get(name));
        attributesInfo.put(fieldName, info);
      }
    });

    return attributesInfo;
  }

  private static boolean isQueryable(Map fieldMappingDetails) {
    Boolean indexVal = (Boolean) fieldMappingDetails.get("index");
    return indexVal == null || indexVal;
  }

  private static Boolean isExactMatchRequired(Map fieldMappingDetails) {
    if(isQueryable(fieldMappingDetails)) {
      return !fieldMappingDetails.get("type").equals("text");
    }
    return null;
  }
}
