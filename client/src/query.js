import queryString from 'query-string'
import _ from 'lodash'
import Immutable from 'immutable'
import store from './store'
import moment from 'moment'
import { triggerSearch, updateQuery } from './search/SearchActions'
import { modifySelectedFacets } from './search/facet/FacetActions'
import { startDate, endDate } from './search/temporal/TemporalActions'
import { newGeometry } from './search/map/MapActions'

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

    const geometry = getQueryContent(queryParams.filters, 'geometry')
    if (!_.isEmpty(geometry[0])) dispatchGeometry(geometry[0].geometry)

    const datetime = getQueryContent(queryParams.filters, 'datetime')
    if (!_.isEmpty(datetime[0])) dispatchDatetimes(datetime[0])

    const facets = getQueryContent(queryParams.filters, 'facet')
    dispatchFacets(facets)

    store.dispatch(triggerSearch())
  }
}

const getQueryContent = (querySection, type) => {
  const queryContent = _.filter(querySection, (o) => {
    return o.type === type
  })
  return queryContent ? queryContent : []
}

const dispatchDatetimes = ({before, after}) => {
    if (after) {
      let afterDate = moment(after).format()
      store.dispatch(startDate(afterDate))
    }
    if (before) {
      let beforeDate = moment(before).format()
      store.dispatch(endDate(beforeDate)
    }
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

const dispatchGeometry = geometry => {
  store.dispatch(newGeometry(
    {
      type: "Feature",
      properties: {},
      geometry
    }
  ))
}

export default loadQuery
