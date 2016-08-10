import {createStore, applyMiddleware} from 'redux'
import { routerMiddleware } from 'react-router-redux'
import { hashHistory } from 'react-router'
import Immutable from 'immutable'
import thunk from 'redux-thunk'
import reducer from './reducer'

const initialState = Immutable.Map()
const store = createStore(reducer, initialState,
    applyMiddleware(
        thunk,
        routerMiddleware(hashHistory)
    ))

export default store
