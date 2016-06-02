import 'babel-polyfill'
import injectTapEventPlugin from 'react-tap-event-plugin'
import React from 'react'
import {render} from 'react-dom'
import {Router, Route, hashHistory} from 'react-router'
import {createStore, applyMiddleware} from 'redux'
import thunk from 'redux-thunk'
import {Provider} from 'react-redux'
import Root from './components/Root.jsx'
import reducer from './reducers/main'
import '../style/style.css'

// Needed for onTouchTap
// Can go away when react 1.0 release
// Check this repo:
// https://github.com/zilverline/react-tap-event-plugin
injectTapEventPlugin();

let store = createStore(reducer, applyMiddleware(thunk));

const routes =
    <Route component={Root}>
      <Route path="/" component={Root}/>
    </Route>;

const body =
    <Provider store={store}>
      <Router history={hashHistory}>{routes}</Router>
    </Provider>;

var appDiv = document.createElement('div');
appDiv.setAttribute('id', 'app');
document.body.appendChild(appDiv);

render(body, appDiv);
