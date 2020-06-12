package org.cedar.onestop.data.api;

import java.util.List;

abstract class JsonApiResponse {
  List<JsonApiData> data;
  List<JsonApiError> errors;
  JsonApiMeta meta;
//  List<JsonApiLink> links; // FIXME
}
