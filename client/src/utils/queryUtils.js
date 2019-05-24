import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {recenterGeometry} from './geoUtils'
import {initialState} from '../reducers/search/collectionFilter'
import {
  convertBboxStringToGeoJson,
  convertGeoJsonToBboxString,
} from './geoUtils'

export const PAGE_SIZE = 20

// export const assembleCollectionSearchRequestString = (
//   state,
//   granules,
//   retrieveFacets
// ) => {
//   if (granules) {
//     return JSON.stringify(assembleGranuleSearchRequest(state, retrieveFacets))
//   }
//   return JSON.stringify(assembleCollectionSearchRequest(state, retrieveFacets))
// }

export const assembleSearchRequest = (
  filter,
  retrieveFacets,
  maxPageSize = PAGE_SIZE
) => {
  return {
    queries: assembleQueries(filter),
    filters: assembleFilters(filter),
    facets: retrieveFacets,
    page: assemblePagination(filter, maxPageSize),
  }
}

const assembleFilters = filter => {
  let filters = _.concat(
    assembleFacetFilters(filter),
    assembleGeometryFilters(filter),
    assembleTemporalFilters(filter),
    assembleAdditionalFilters(filter),
    assembleSelectedCollectionsFilters(filter)
  )

  filters = _.flatten(_.compact(filters))
  return filters
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
  if (selectedIds && selectedIds.length > 0) {
    return {type: 'collection', values: selectedIds}
  }
}

const assemblePagination = ({pageOffset = 0}, maxPageSize) => {
  // TODO tests to verify that
  return {max: maxPageSize, offset: pageOffset}
}

export const encodeQueryString = searchParamsState => {
  const queryParams = _.map(searchParamsState, (v, k) => {
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
  // {
  //   longKey: 'selectedIds',
  //   shortKey: 'i',
  //   encode: ids => _.map(ids, id => encodeURIComponent(id)).join(','),
  //   decode: text => _.map(text.split(','), id => decodeURIComponent(id)),
  // },
  {
    longKey: 'excludeGlobal',
    shortKey: 'eg',
    encode: bool => (bool ? '1' : '0'),
    decode: text => text === '1',
  },
]
