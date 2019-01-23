import React from 'react'
import {Provider} from 'react-redux'
import {Route, Switch} from 'react-router'
import {ConnectedRouter} from 'connected-react-router'
import RootContainer from './root/RootContainer'
import AnalyticsContainer from './analytics/AnalyticsContainer'
import {ROUTE} from './utils/urlUtils'
import _ from 'lodash'

// this higher-order component is kept separate from index.jsx
// to parameterize the store and history for tests
const isTest = process.env.NODE_ENV === 'test'

const App = (store, history) => {
  const reload = () => window.location.reload()
  if (history.location.hash) {
    const hash = history.location.hash.replace('#', '')
    const parts = _.split(hash, '?')
    const locationDescriptor = {
      pathname: parts[0],
      search: parts[1] ? `?${parts[1]}` : '',
    }
    history.push(locationDescriptor)
  }
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <div>
          {isTest ? null : <AnalyticsContainer />}
          <Switch>
            <Route path={ROUTE.sitemap.path} exact onEnter={reload} />
            <RootContainer />
          </Switch>
        </div>
      </ConnectedRouter>
    </Provider>
  )
}

export default App
