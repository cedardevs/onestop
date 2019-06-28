import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../utils/urlUtils'
import CollectionFiltersContainer from './collections/CollectionFiltersContainer'
import GranuleFiltersContainer from './granules/GranuleFiltersContainer'

export default class Filters extends React.Component {
  render() {
    return (
      <Switch>
        <Route path={ROUTE.collections.path} exact>
          <CollectionFiltersContainer />
        </Route>
        <Route path={ROUTE.granules.parameterized}>
          <GranuleFiltersContainer />
        </Route>
      </Switch>
    )
  }
}
