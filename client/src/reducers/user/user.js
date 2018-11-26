import Immutable from 'seamless-immutable'
import {GET_USER_REQUEST, GET_USER_SUCCESS, GET_USER_FAILURE, LOGOUT_USER} from '../../actions/UserActions'

export const initialState = Immutable({});

export const info = (state = initialState, action) => {
    switch (action.type) {

        case GET_USER_REQUEST:
            const isFetchingState = state.setIn(
                [ 'isFetching'], true ,
                action.item
            );
            return isFetchingState;

        case GET_USER_SUCCESS:
            const userState = state.setIn(
                [ 'info'], action.payload
            ).setIn(
                ['isFetching'], false
            );
            return userState;

        case GET_USER_FAILURE:
            const userFailState = state.setIn(
                [ 'error', action.error ],
                action.item
            ).setIn(
                ['isFetching'], false
            );
            return userFailState;

        case LOGOUT_USER:
            const userLogoutState = state.setIn(
                [ 'info', {}],
                action.item
            ).setIn(['expired'], true);
            return userLogoutState;
        default:
            return state

    }
}

export default info
