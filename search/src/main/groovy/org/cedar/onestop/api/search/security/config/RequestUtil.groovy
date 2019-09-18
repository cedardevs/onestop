package org.cedar.onestop.api.search.security.config

import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletRequest

class RequestUtil {
    static String getURL(HttpServletRequest request, String path) {
        URI requestURI = new URI(request.requestURL.toString())
        URI contextURI = new URI(
                requestURI.getScheme(),
                requestURI.getAuthority(),
                request.getContextPath(),
                null,
                null
        )
        return UriComponentsBuilder.fromUri(contextURI)
            .path(path)
            .toUriString()
    }
}
