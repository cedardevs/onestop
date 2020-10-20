package org.cedar.onestop.data.api;

import java.util.List;

abstract public class JsonApiResponse {
  protected List<JsonApiData> data;
  protected List<JsonApiError> errors;
  protected JsonApiMeta meta;
//  List<JsonApiLink> links; ?
}