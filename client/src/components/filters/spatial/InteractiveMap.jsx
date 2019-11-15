import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../../utils/urlUtils'
import CollectionMapContainer from '../collections/CollectionMapContainer'
import GranuleMapContainer from '../granules/GranuleMapContainer'

const InteractiveMap = () => {
  return (
    <Switch>
      <Route path={ROUTE.collections.path} exact>
        <CollectionMapContainer selection={true} features={false} />
      </Route>
      <Route path={ROUTE.granules.parameterized}>
        <GranuleMapContainer selection={true} features={false} />
      </Route>
    </Switch>
  )
}

export default InteractiveMap
