package org.cedar.onestop.api.search.service

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity


class SearchRequestBuilder {

  HttpEntity buildSearchQuery(Map params) {

    def requestBody = [
        query: [
            bool: [
                must  : [],
                filter: []
            ]
        ],
        aggs : [],

    ]

    return new NStringEntity(requestBody, ContentType.APPLICATION_JSON)
  }

}
