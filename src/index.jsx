import 'babel-polyfill'
import React from 'react';
import {render} from 'react-dom';
import {Router, Route, hashHistory} from 'react-router';
import {createStore, applyMiddleware} from 'redux';
import thunk from 'redux-thunk';
import {Provider} from 'react-redux';
import Root from './components/Root.jsx'
import reducer from './reducers/main';

let store = createStore(reducer, applyMiddleware(thunk));

const routes =
    <Route component={Root}>
      <Route path="/" component={Root}/>
    </Route>;

render(
    <Provider store={store}>
      <Router history={hashHistory}>{routes}</Router>
    </Provider>,
    document.getElementById('app')
);