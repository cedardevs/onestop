import Immutable from 'immutable';
import {OPEN, CLOSED} from './FacetActions'
export const initialState = Immutable.Map({
    visible: false
});

const facets = (state = initialState, action) => {
    switch(action.type) {
        case OPEN:
            return state.merge({
                visible: true
            });
        case CLOSED:
            return state.merge({
                visible: false
            });

        default:
            return state
    }
};

export default facets;