import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../utils/urlUtils'
import ResultWithFilters from './ResultWithFilters'
import CollectionsContainer from './collections/CollectionsContainer'
import GranuleListContainer from './granules/GranuleListContainer'
import CollectionAppliedFiltersContainer from '../filters/collections/CollectionAppliedFiltersContainer'
import GranuleAppliedFiltersContainer from '../filters/granules/GranuleAppliedFiltersContainer'
import GranuleListNavigationContainer from './granules/GranuleListNavigationContainer'
import GranuleListLegendContainer from './granules/GranuleListLegendContainer'

export default class Results extends React.Component {
  render() {
    return (
      <Switch>
        <Route path={ROUTE.collections.path} exact>
          <ResultWithFilters>
            <CollectionAppliedFiltersContainer />
            <CollectionsContainer />
          </ResultWithFilters>
        </Route>
        <Route path={ROUTE.granules.parameterized}>
          <ResultWithFilters>
            <GranuleListNavigationContainer />
            <GranuleListLegendContainer />
            <GranuleAppliedFiltersContainer />
            <GranuleListContainer />
          </ResultWithFilters>
        </Route>
      </Switch>
    )
  }
}
