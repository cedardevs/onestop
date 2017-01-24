import {createStore, applyMiddleware} from 'redux'
import { routerMiddleware } from 'react-router-redux'
import { hashHistory } from 'react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import reducer from './reducers/reducer'
import { initialState } from './utils/refreshUtils'
import _ from 'lodash'

const store = createStore(reducer, initialState(),
    applyMiddleware(
        thunk,
        routerMiddleware(hashHistory)
    ))

export default store
