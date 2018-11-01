import React from 'react'
import {Provider} from 'react-redux'
import {Route, Switch} from 'react-router'
import {ConnectedRouter} from 'connected-react-router'
import RootContainer from './root/RootContainer'

// this higher-order component is kept separate from index.jsx
// to parameterize the store and history for tests
const App = (store, history) => {
  const reload = () => window.location.reload()
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <Switch>
          <Route path="/sitemap.xml" exact onEnter={reload} />
          <RootContainer />
        </Switch>
      </ConnectedRouter>
    </Provider>
  )
}

export default App
