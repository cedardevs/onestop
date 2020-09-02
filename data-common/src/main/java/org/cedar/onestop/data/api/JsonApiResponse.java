package org.cedar.onestop.data.api;

import java.util.*;

abstract public class JsonApiResponse {
  List<JsonApiData> data;
  List<JsonApiErrorResponse> errors;
  JsonApiMeta meta;
//  List<JsonApiLink> links; ?
}