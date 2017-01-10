import _ from 'lodash'
import Immutable from 'immutable'
import moment from 'moment'
import { triggerSearch, updateQuery } from '../search/SearchActions'
import { fetchGranules } from '../result/granules/GranulesActions'
import { modifySelectedFacets } from '../search/facet/FacetActions'
import { startDate, endDate } from '../search/temporal/TemporalActions'
import { newGeometry } from '../search/map/MapActions'
import store from '../store'

export const instantiateAppState = () => {
  return (dispatch, getState) => {
    const { queryParams, baseURL } = validUrlQuery()

    if (!_.isEmpty(queryParams)){
      const { queries, filters } = queryParams
      const queryText = getQueryContent(queries, 'queryText')
      if (!_.isEmpty(queryText)) dispatch(updateQuery(queryText[0].value))

      const geometry = getQueryContent(filters, 'geometry')
      if (!_.isEmpty(geometry[0])) dispatchGeometry(dispatch, geometry[0].geometry)

      const datetime = getQueryContent(filters, 'datetime')
      if (!_.isEmpty(datetime[0])) dispatchDatetimes(dispatch, datetime[0])

      const facets = getQueryContent(filters, 'facet')
      dispatchFacets(dispatch, facets)

      dispatch(triggerSearch())
      if (baseURL.endsWith('files')) {
        // Requires both original collection search and granule search
        const checkInitialize = () => store.getState().getIn(['routing', 'initialized'])
        const subscribeInitialized = () => {
          if (checkInitialize()) {
            unsubscribe()
            dispatch(fetchGranules())
          }
        }
        let unsubscribe = store.subscribe(subscribeInitialized)
      }
    }

    function handleChange() {
      let previousValue = currentValue
      currentValue = select(store.getState())

      if (previousValue !== currentValue) {
        console.log('Some deep nested property changed from', previousValue, 'to', currentValue)
      }
    }

    function validUrlQuery() {
      const urlString = decodeURIComponent(document.location.hash)
      if (urlString.includes('?')){
        const queryArray = urlString.split('?')
        return { baseURL: queryArray[0], queryParams: JSON.parse(queryArray[1]) }
      } else {
        return {}
      }
    }

    function getQueryContent(querySection, type) {
      const queryContent = _.filter(querySection, (o) => {
        return o.type === type
      })
      return queryContent ? queryContent : []
    }

    function dispatchDatetimes(dispatch, {before, after}) {
      if (after) {
        dispatch(startDate(after))
      }
      if (before) {
        dispatch(endDate(before))
      }
    }

    function dispatchFacets(dispatch, facets) {
      if (facets) {
        let selectedFacets = Immutable.Map()
        for (let facet of facets) {
          for (let value of facet.values) {
            selectedFacets = selectedFacets.setIn([facet.name, value, 'selected'], true)
          }
        }
        dispatch(modifySelectedFacets(selectedFacets))
      }
      return !!facets
    }

    function dispatchGeometry(dispatch, geometry) {
      dispatch(newGeometry({
          type: "Feature",
          properties: {},
          geometry
        }
      ))
    }
  }
}
