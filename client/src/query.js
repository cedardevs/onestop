import queryString from 'query-string'
import _ from 'lodash'
import Immutable from 'immutable'
import store from './store'
import { triggerSearch } from './search/SearchActions'
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
  
  let params = JSON.stringify(queryParams)
  if(params !== '{}') {
    store.dispatch(triggerSearch(params, null, loadFacets(queryParams.filters)))
  }
}

const loadFacets = filters => {
  const facets = _.filter(filters, (o) => {
    return o.type === 'facet'
  })
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
