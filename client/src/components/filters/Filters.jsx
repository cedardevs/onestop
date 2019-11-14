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
        <CollectionFiltersContainer />
      </Route>
      <Route path={ROUTE.granules.parameterized}>
        <GranuleFiltersContainer />
      </Route>
    </Switch>
  )
}

export default Filters
