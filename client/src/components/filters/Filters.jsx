import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../utils/urlUtils'
import CollectionFiltersContainer from './collections/CollectionFiltersContainer'
import GranuleFiltersContainer from './granules/GranuleFiltersContainer'

const Filters = props => {
  const {drawerProxy} = props
  return (
    <Switch>
      <Route path={ROUTE.collections.path} exact>
        <CollectionFiltersContainer drawerProxy={drawerProxy} />
      </Route>
      <Route path={ROUTE.granules.parameterized}>
        <GranuleFiltersContainer drawerProxy={drawerProxy} />
      </Route>
    </Switch>
  )
}

export default Filters
