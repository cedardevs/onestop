import _ from 'lodash'
import { recenterGeometry } from './geoUtils'

export const assembleSearchRequestString = (state, granules = false) => {
  return JSON.stringify(assembleSearchRequest(state, granules))
}

export const assembleSearchRequest = (state, granules = false) => {
  const behavior = state.behavior || {}
  const search = behavior.search || {}

  const queries = assembleQueries(search)
  let filters = _.concat(
    assembleFacetFilters(search),
    assembleGeometryFilters(search),
    assembleTemporalFilters(search)
  )
  if (granules) {
    filters = _.concat(assembleSelectedCollectionsFilters(search))
  }
  filters =  _.flatten(_.compact(filters))

  const page = assemblePagination(search)

  return {queries: queries, filters: filters, facets: !granules, page: page}
}

const assembleQueries = ({queryText}) => {
  if (queryText) {
    return [{type: 'queryText', value: queryText}]
  }
  return []
}

const assembleFacetFilters = ({selectedFacets}) => {
  return _.map(selectedFacets, (v, k) => ({'type':'facet', 'name': k, 'values': v}))
}

const assembleGeometryFilters = ({geoJSON}) => {
  if (geoJSON && geoJSON.geometry) {
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

const assembleSelectedCollectionsFilters = ({selectedIds}) => {
  if (selectedIds.length > 0) {
    return {type: 'collection', values: selectedIds}
  }
}

const assemblePagination = ({pageOffset, pageMax}) => {
  if(pageOffset && pageMax) {
    return {max: pageMax, offset: pageOffset}
  }
  else {
    return {max: 20, offset: 0}
  }
}

export const encodeQueryString = (state) => {
  const searchParams = state && state.behavior && state.behavior.search
  if (_.every(searchParams, (e) => { return(_.isEmpty(e)) })) {
    return ''
  }

  // TODO - implement a better encoding scheme
  return JSON.stringify(searchParams)
}

export const decodeQueryString = (queryString) => {
  if (_.isEmpty(queryString)) {
    return {}
  }

  // TODO - decode the brilliant encoding scheme from above
  return {behavior: {search: JSON.parse(queryString)}}
}
