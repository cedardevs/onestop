import _ from 'lodash'
import { recenterGeometry } from './geoUtils'

export const assembleSearchRequestString = (state) => {
  return JSON.stringify(assembleSearchRequest(state))
}

export const assembleSearchRequest = (state) => {
  return {
    queries: assembleQueries(state.appState || {}),
    filters: assembleFilters(state.appState || {}),
    facets: true
  }
}

const assembleQueries = ({queryText}) => {
  if (queryText && queryText.text) {
    return [{type: 'queryText', value: queryText.text}]
  }
  return []
}

const assembleFilters = ({temporal, geometry, facets}) => {
  return _.flatten(_.compact(_.concat(
      assembleFacetFilters(facets || {}),
      assembleGeometryFilters(geometry || {}),
      assembleTemporalFilters(temporal || {})
  )))
}

const assembleFacetFilters = ({selectedFacets}) => {
  return _.map(selectedFacets, (v, k) => ({'type':'facet', 'name': k, 'values': v}))
}

const assembleGeometryFilters = ({geoJSON}) => {
  if (geoJSON) {
    const recenteredGeometry = recenterGeometry(geoJSON.geometry)
    return {type: 'geometry', geometry: recenteredGeometry}
  }
}

const assembleTemporalFilters = ({startDateTime, endDateTime}) => {
  if (startDateTime && endDateTime) {
    return {type: 'datetime', after: startDateTime, before: endDateTime}
  } else if (startDateTime) {
    return {type: 'datetime', after: startDateTime}
  } else if (endDateTime) {
    return {type: 'datetime', before: endDateTime}
  }
}
