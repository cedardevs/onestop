import 'isomorphic-fetch'
import {apiPath} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'

export const fetchSitemap = () => {
  return dispatch => {
    const endpoint = apiPath() + '/sitemap.xml'
    const fetchParams = {
      method: 'GET',
    }
    return (
      fetch(endpoint, fetchParams)
        .then(response => checkForErrors(response))
        // TODO: can we leverage dispatch here to use router like we are elsewhere instead of window.location.href?
        .then(response => (window.location.href = response.url))
    )
  }
}
