import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {getIdFromPath} from './urlUtils'
import {initialState} from '../reducers/search/collectionFilter'
import {
  recenterGeometry,
  convertBboxStringToGeoJson,
  convertGeoJsonToBboxString,
} from './geoUtils'
import {textToNumber} from './inputUtils'

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

const assembleQueries = ({queryText, title}) => {
  let trimmedText = _.trim(queryText)
  if (trimmedText && trimmedText !== '*') {
    return [ {type: 'queryText', value: trimmedText} ]
  }
  if (title) {
    let trimmedText = _.trim(title)
    if (trimmedText && trimmedText !== '*') {
      return [ {type: 'queryText', value: `title:${trimmedText}`} ]
    }
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

const assembleTemporalFilters = ({
  startDateTime,
  endDateTime,
  startYear,
  endYear,
}) => {
  if (startDateTime && endDateTime) {
    return {type: 'datetime', after: startDateTime, before: endDateTime}
  }
  else if (startDateTime) {
    return {type: 'datetime', after: startDateTime}
  }
  else if (endDateTime) {
    return {type: 'datetime', before: endDateTime}
  }

  // note: while this method does not explicitly prevent both datetime and year being used together (which is not actually valid), it also won't return anything for year if either datetime filter is used...
  if (startYear && endYear) {
    return {type: 'year', after: startYear, before: endYear}
  }
  else if (startYear) {
    return {type: 'year', after: startYear}
  }
  else if (endYear) {
    return {type: 'year', before: endYear}
  }
}

const assembleAdditionalFilters = ({excludeGlobal}) => {
  if (excludeGlobal) {
    return {type: 'excludeGlobal', value: excludeGlobal}
  }
}

const assembleSelectedCollectionsFilters = ({selectedCollectionIds}) => {
  if (selectedCollectionIds && selectedCollectionIds.length > 0) {
    return {type: 'collection', values: selectedCollectionIds}
  }
}

const assemblePagination = ({pageOffset = 0}, maxPageSize) => {
  return {max: maxPageSize, offset: pageOffset}
}

export const encodeLocationDescriptor = (route, searchParamsState) => {
  const query = encodeQueryString(searchParamsState)
  return {
    pathname: route.toLocation(
      searchParamsState.selectedCollectionIds
        ? searchParamsState.selectedCollectionIds[0]
        : null
    ),
    search: _.isEmpty(query) ? '' : `?${query}`,
  }
}

export const decodePathAndQueryString = (path, queryString) => {
  if (queryString.indexOf('?') === 0) {
    queryString = queryString.slice(1)
  }
  let search = decodeQueryString(queryString)
  const id = getIdFromPath(path)
  if (id) {
    search = Immutable.merge(search, {selectedCollectionIds: [ id ]})
  }
  return {id: id, filters: search}
}

export const encodeQueryString = searchParamsState => {
  const queryParams = _.map(searchParamsState, (v, k) => {
    const codec = _.find(codecs, c => c.longKey === k)
    const encode = codec && codec.encodable(v)
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
    encodable: text => !_.isEmpty(text),
  },
  {
    longKey: 'title',
    shortKey: 't',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
    encodable: text => !_.isEmpty(text),
  },
  {
    longKey: 'geoJSON',
    shortKey: 'g',
    encode: geoJSON => convertGeoJsonToBboxString(geoJSON),
    decode: text => convertBboxStringToGeoJson(text),
    encodable: getJSON => !_.isEmpty(getJSON),
  },
  {
    longKey: 'startDateTime',
    shortKey: 's',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
    encodable: text => !_.isEmpty(text),
  },
  {
    longKey: 'endDateTime',
    shortKey: 'e',
    encode: text => encodeURIComponent(text),
    decode: text => decodeURIComponent(text),
    encodable: text => !_.isEmpty(text),
  },
  {
    longKey: 'startYear',
    shortKey: 'sy',
    encode: num => encodeURIComponent(num),
    decode: text => textToNumber(decodeURIComponent(text)),
    encodable: num => num != null,
  },
  {
    longKey: 'endYear',
    shortKey: 'ey',
    encode: num => encodeURIComponent(num),
    decode: text => textToNumber(decodeURIComponent(text)),
    encodable: num => num != null,
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
    encodable: facets => !_.isEmpty(facets),
  },
  // {
  //   longKey: 'selectedCollectionIds',
  //   shortKey: 'i',
  //   encode: ids => _.map(ids, id => encodeURIComponent(id)).join(','),
  //   decode: text => _.map(text.split(','), id => decodeURIComponent(id)),
  //   encodable:  // TODO
  // },
  {
    longKey: 'excludeGlobal',
    shortKey: 'eg',
    encode: bool => (bool ? '1' : '0'),
    decode: text => text === '1',
    encodable: bool => bool === true,
  },
]
