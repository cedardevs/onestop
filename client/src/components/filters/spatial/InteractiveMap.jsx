import React from 'react'
import {Route, Switch} from 'react-router'
import {ROUTE} from '../../../utils/urlUtils'
import CollectionMapContainer from '../collections/CollectionMapContainer'
import GranuleMapContainer from '../granules/GranuleMapContainer'

export default class InteractiveMap extends React.Component {
  render() {
    const {content} = this.props
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
}
