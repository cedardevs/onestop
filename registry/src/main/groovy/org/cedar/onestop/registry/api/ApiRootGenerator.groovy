package org.cedar.onestop.registry.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest


@Service
class ApiRootGenerator {

  private String apiRootUrl

  ApiRootGenerator(@Value('${api.root.url:}') String apiRootUrl) {
    this.apiRootUrl = apiRootUrl
  }

  String getApiRoot(HttpServletRequest request) {
    if (!request) {
      return null
    }
    if (apiRootUrl) {
      return apiRootUrl
    }

    def forwarded = parseForwardedHeader(request.getHeader('forwarded'))
    def proto = forwarded?.proto ?: request.getHeader('x-forwarded-proto') ?: request.getScheme()
    def host = forwarded?.host ?: request.getHeader('x-forwarded-host') ?: request.getHeader('host')
    def context = request.getContextPath()

    return proto + '://' + host + context
  }

  Map parseForwardedHeader(String header) {
    return header?.split(';')?.collectEntries({ String part ->
      def i = part.indexOf('=')
      if (i != -1) {
        def key = part.substring(0, i).toLowerCase().trim().replaceAll('"', '')
        def value = part.substring(i + 1).toLowerCase().trim().replaceAll('"', '')
        if (key in ['host', 'proto']) {
          return [(key): value ?: null]
        }
      }
      return [:]
    })
  }

}
