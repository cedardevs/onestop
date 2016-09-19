import queryString from 'query-string'
import _ from 'lodash'
import Immutable from 'immutable'
import store from './store'
import { triggerSearch, updateQuery } from './search/SearchActions'
import { modifySelectedFacets } from './search/facet/FacetActions'

const loadQuery = () => {
  const urlString = document.location.hash
  let queryParams = queryString.parse(urlString.slice(urlString.indexOf('?')+1,-1))
  for (const key in queryParams){
    if (key !== '_k') { // Don't query hash history param
      queryParams[key] = JSON.parse(queryParams[key])
    } else {
      delete(queryParams[key])
    }
  }
  if (!_.isEmpty(queryParams)){
    const queryText = getQueryContent(queryParams.queries, 'queryText')
    if (!_.isEmpty(queryText)) store.dispatch(updateQuery(queryText[0].value))

    const facets = getQueryContent(queryParams.filters, 'facet')

    let params = JSON.stringify(queryParams)
    store.dispatch(triggerSearch(params, null, dispatchFacets(facets)))
  }
}

const getQueryContent = (querySection, type) => {
  const queryContent = _.filter(querySection, (o) => {
    return o.type === type
  })
  return queryContent ? queryContent : []
}

const dispatchFacets = facets => {
  if (facets) {
    let selectedFacets = Immutable.Map()
    for (let facet of facets) {
      for (let value of facet.values) {
        selectedFacets = selectedFacets.setIn([facet.name, value, 'selected'], true)
      }
    }
    store.dispatch(modifySelectedFacets(selectedFacets))
  }
  return !!facets
}

export default loadQuery
