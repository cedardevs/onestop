import Immutable from 'seamless-immutable'
import {CLEAR_INFO, SET_INFO} from '../../actions/InfoActions'

export const initialState = Immutable({
    version: ""
})

export const info = (state = initialState, action) => {
    switch (action.type) {
        case SET_INFO:
            return Immutable.merge(state, action.info)

        case CLEAR_INFO:
            return initialState

        default:
            return state
    }
}

export default info
