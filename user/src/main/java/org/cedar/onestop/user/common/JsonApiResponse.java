package org.cedar.onestop.user.common;

import java.util.*;

/**
 * Temporary copy of what should eventually exist in array-iteration module.
 */

abstract public class JsonApiResponse {
  List<JsonApiData> data;
  List<JsonApiError> errors;
  JsonApiMeta meta;
//  List<JsonApiLink> links; ?
}