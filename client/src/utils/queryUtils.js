import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {recenterGeometry} from './geoUtils'
import {initialState} from '../reducers/search/collectionFilter'
import {
  convertBboxStringToGeoJson,
  convertGeoJsonToBboxString,
} from './geoUtils'

export const assembleSearchRequestString = (
  state,
  granules,
  retrieveFacets
) => {
  return JSON.stringify(assembleSearchRequest(state, granules, retrieveFacets))
}

export const assembleGranuleSearchRequest = (state, granules, retrieveFacets) => {
  const search = state.search || {}
  const granuleFilter = search.granuleFilter || {}
  const collectionFilter = search.collectionFilter || {}
  const granuleResult = search.granuleResult || {}
  const pageOffset = granuleResult.granulesPageOffset || 0
  const pageSize = granuleResult.pageSize || 20
  const page = assemblePagination(pageSize, pageOffset)

  // granule search, assembled for search API / elasticsearch
  let queries = [] //assembleQueries(granuleFilter)
  let filters = _.concat(
    assembleFacetFilters(granuleFilter),
    assembleGeometryFilters(granuleFilter),
    assembleTemporalFilters(granuleFilter),
    assembleAdditionalFilters(granuleFilter),
    assembleSelectedCollectionsFilters(collectionFilter)
  )

  filters = _.flatten(_.compact(filters))

  return {
    queries: queries,
    filters: filters,
    facets: retrieveFacets,
    page: page,
  }
}

export const assembleSearchRequest = (state, granules, retrieveFacets) => { // TODO rename to include collection in name
  const search = state.search || {}
  const collectionFilter = search.collectionFilter || {}
  const collectionResult = search.collectionResult || {}
  const pageOffset =
    (granules
      ? collectionResult.granulesPageOffset
      : collectionResult.collectionsPageOffset) || 0
  const pageSize = collectionResult.pageSize || 20
  const page = assemblePagination(pageSize, pageOffset)

  // collection search, assembled for search API / elasticsearch
  let queries = assembleQueries(collectionFilter)
  let filters = _.concat(
    assembleFacetFilters(collectionFilter),
    assembleGeometryFilters(collectionFilter),
    assembleTemporalFilters(collectionFilter),
    assembleAdditionalFilters(collectionFilter)
  )

  // change which filters are applied and drop query text for granules (until #445 allows changing filters applied to granules directly)
  if (granules) {
    filters = _.concat(assembleSelectedCollectionsFilters(collectionFilter))
    queries = []
  }
  filters = _.flatten(_.compact(filters))

  return {
    queries: queries,
    filters: filters,
    facets: retrieveFacets,
    page: page,
  }
}

const assembleQueries = ({queryText}) => {
  let trimmedText = _.trim(queryText)
  if (trimmedText && trimmedText !== '*') {
    return [ {type: 'queryText', value: trimmedText} ]
  }
  return []
}

const assembleFacetFilters = ({selectedFacets}) => {
  return _.map(selectedFacets, (v, k) => ({type: 'facet', name: k, values: v}))
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
  }
  else if (startDateTime) {
    return {type: 'datetime', after: startDateTime}
  }
  else if (endDateTime) {
    return {type: 'datetime', before: endDateTime}
  }
}

const assembleAdditionalFilters = ({excludeGlobal}) => {
  if (excludeGlobal) {
    return {type: 'excludeGlobal', value: excludeGlobal}
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

export const encodeQueryString = (state, filterStateName = 'collectionFilter') => { // TODO change default assumptions and make filterStateName explicitly required in all calls later
  const searchParams =
    (state && state.search && state.search[filterStateName]) || {}
  const queryParams = _.map(searchParams, (v, k) => {
    const codec = _.find(codecs, c => c.longKey === k)
    const encode = codec && (v === true || !_.isEmpty(v))
    return encode ? `${codec.shortKey}=${codec.encode(v)}` : null
  })
  return _.filter(queryParams).join('&')
}

export const decodeQueryString = queryString => {
  const queryParams = (queryString && queryString.split('&')) || []
  const searchParams = _.reduce(
    queryParams,
    (result, param) => {
      const [ k, v ] = param.split('=')
      const codec = _.find(codecs, c => c.shortKey === k)
      return codec
        ? Immutable.set(result, codec.longKey, codec.decode(v))
        : result
    },
    initialState
  )
  return searchParams
}

const codecs = [
  {
    longKey: 'queryText',
    shortKey: 'q',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
  },
  {
    longKey: 'geoJSON',
    shortKey: 'g',
    encode: geoJSON => convertGeoJsonToBboxString(geoJSON),
    decode: text => convertBboxStringToGeoJson(text),
  },
  {
    longKey: 'startDateTime',
    shortKey: 's',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
  },
  {
    longKey: 'endDateTime',
    shortKey: 'e',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
  },
  {
    longKey: 'selectedFacets',
    shortKey: 'f',
    encode: facets => {
      const encodedList = _.map(facets, (terms, category) => {
        return (
          category +
          ':' +
          _.map(terms, term => encodeURIComponent(term)).join(',')
        )
      })
      return encodedList.join(';')
    },
    decode: text => {
      return _.reduce(
        text.split(';'),
        (result, encodedList) => {
          const [ category, terms ] = encodedList.split(':')
          result[category] = _.map(terms.split(','), term =>
            decodeURIComponent(term)
          )
          return result
        },
        {}
      )
    },
  },
  {
    longKey: 'selectedIds',
    shortKey: 'i',
    encode: ids => _.map(ids, id => encodeURIComponent(id)).join(','),
    decode: text => _.map(text.split(','), id => decodeURIComponent(id)),
  },
  {
    longKey: 'excludeGlobal',
    shortKey: 'eg',
    encode: bool => (bool ? '1' : '0'),
    decode: text => text === '1',
  },
]
