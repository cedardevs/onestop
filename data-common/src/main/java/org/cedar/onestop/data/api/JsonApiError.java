package org.cedar.onestop.data.api;

import java.util.List;
import java.util.Map;

public class JsonApiError {
  String id;
//  List<JsonApiLink> links; // FIXME
  String status; // FIXME HTTP status code
  String code; // FIXME app-specific error code
  String title;
  String detail;
  ErrorSource source; // FIXME JSON pointer string like '/data/attributes/title'
  Map<String, Object> meta;

  private class ErrorSource {
    String pointer;
    String parameter;
  }
}
