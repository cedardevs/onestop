import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../utils/urlUtils'
import CollectionResult from './collections/CollectionResult'
import CollectionsContainer from './collections/CollectionsContainer'
import GranuleListContainer from './granules/GranuleListContainer'

export default class Results extends React.Component {
  render() {
    return (
      <Switch>
        <Route path={ROUTE.collections.path} exact>
          <CollectionResult>
            <CollectionsContainer />
          </CollectionResult>
        </Route>
        <Route path={ROUTE.granules.parameterized}>
          <GranuleListContainer />
        </Route>
      </Switch>
    )
  }
}
