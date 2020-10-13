import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {getIdFromPath} from './urlUtils'
import {initialState} from '../reducers/search/collectionFilter'
import {convertBboxToQueryGeoJson} from './geoUtils'
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

const assembleQueries = ({queryText, title, allTermsMustMatch}) => {
  let trimmedText = _.trim(queryText)
  if (trimmedText && trimmedText !== '*') {
    return [ {type: 'queryText', value: trimmedText} ]
  }

  if (title) {
    let trimmedText = _.trim(title)
    if (trimmedText) {
      return [
        {
          type: 'granuleName',
          value: `${trimmedText}`,
          allTermsMustMatch: allTermsMustMatch,
        },
      ]
    }
  }
  return []
}

const assembleFacetFilters = ({selectedFacets}) => {
  return _.map(selectedFacets, (v, k) => ({
    type: 'facet',
    name: k,
    values: v,
  }))
}

const assembleGeometryFilters = ({bbox, geoRelationship}) => {
  if (bbox) {
    let geometry = convertBboxToQueryGeoJson(
      bbox.west,
      bbox.south,
      bbox.east,
      bbox.north
    )
    if (geometry) {
      return {
        type: 'geometry',
        geometry: geometry.geometry,
        relation: geoRelationship,
      }
    }
  }
}

const assembleTemporalFilters = ({
  timeRelationship,
  startDateTime,
  endDateTime,
  startYear,
  endYear,
}) => {
  if (startDateTime && endDateTime) {
    return {
      type: 'datetime',
      after: startDateTime,
      before: endDateTime,
      relation: timeRelationship,
    }
  }
  else if (startDateTime) {
    return {
      type: 'datetime',
      after: startDateTime,
      relation: timeRelationship,
    }
  }
  else if (endDateTime) {
    return {type: 'datetime', before: endDateTime, relation: timeRelationship}
  }

  // note: while this method does not explicitly prevent both datetime and year being used together (which is not actually valid), it also won't return anything for year if either datetime filter is used...
  if (startYear && endYear) {
    return {
      type: 'year',
      after: startYear,
      before: endYear,
      relation: timeRelationship,
    }
  }
  else if (startYear) {
    return {type: 'year', after: startYear, relation: timeRelationship}
  }
  else if (endYear) {
    return {type: 'year', before: endYear, relation: timeRelationship}
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
    const encode = codec && codec.encodable(v, searchParamsState)
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

const encodeRelationship = text => {
  if (text == 'intersects') return 'i' // TODO don't know if I can get it to skip it (and if I did, it'd also skip for no actual time filters)
  if (text == 'within') return 'w'
  if (text == 'disjoint') return 'd'
  if (text == 'contains') return 'c'
}
const decodeRelationship = text => {
  if (text == 'd') return 'disjoint'
  if (text == 'c') return 'contains'
  if (text == 'w') return 'within'
  if (text == 'i') return 'intersects'
  return null
}

const convertArgsToBbox = (west, south, east, north) => {
  return {
    north: north,
    east: east,
    south: south,
    west: west,
  }
}

const decodeBbox = coordString => {
  const coordArray = coordString.split(',').map(x => parseFloat(x))
  return convertArgsToBbox(...coordArray)
}

const encodeBbox = bbox => {
  return bbox ? `${bbox.west},${bbox.south},${bbox.east},${bbox.north}` : ''
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
    longKey: 'allTermsMustMatch',
    shortKey: 'tm',
    encode: bool => (bool ? '1' : '0'),
    decode: text => text === '1',
    encodable: bool => bool === false, // only include when changed from default
  },
  {
    longKey: 'bbox',
    shortKey: 'g',
    encode: bbox => encodeBbox(bbox),

    decode: text => decodeBbox(text),
    encodable: bbox => !_.isEmpty(bbox),
  },
  {
    longKey: 'geoRelationship',
    shortKey: 'gr',
    encode: text => encodeRelationship(text),
    decode: text => decodeRelationship(text),
    encodable: (text, queryState) => {
      return !_.isEmpty(text) && !_.isEmpty(queryState.bbox)
    },
  },
  {
    longKey: 'timeRelationship',
    shortKey: 'tr',
    encode: text => encodeRelationship(text),
    decode: text => decodeRelationship(text),
    encodable: (text, queryState) => {
      return (
        !_.isEmpty(text) &&
        (!_.isEmpty(queryState.startDateTime) ||
          !_.isEmpty(queryState.endDateTime) ||
          queryState.startYear != null ||
          queryState.endYear != null)
      )
    },
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
