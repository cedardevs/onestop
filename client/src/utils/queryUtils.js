import _ from 'lodash'
import { recenterGeometry } from './geoUtils'

export const assembleSearchRequestString = (state, granules, retrieveFacets) => {
  return JSON.stringify(assembleSearchRequest(state, granules, retrieveFacets))
}

export const assembleSearchRequest = (state, granules, retrieveFacets) => {
  const behavior = state.behavior || {}
  const search = behavior.search || {}
  const domain = state.domain || {}
  const results = domain.results || {}
  const pageOffset = (granules ? results.granulesPageOffset : results.collectionsPageOffset) || 0
  const pageSize = results.pageSize || 20

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

  const page = assemblePagination(pageSize, pageOffset)

  return {queries: queries, filters: filters, facets: retrieveFacets, page: page}
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

const assemblePagination = (max, offset) => {
  return {max: max, offset: offset}
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
