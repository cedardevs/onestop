import Immutable from 'immutable';
import {SEARCH, SEARCH_COMPLETE} from '../search/SearchActions';
import {FETCH_DETAILS, RECEIVE_DETAILS, FLIP_CARD} from './DetailActions';

export const initialState = {
  id: null,
  details: null,
  flipped: false
};

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH:
    case SEARCH_COMPLETE:
      return Immutable.Map();

    case FETCH_DETAILS:
      return Immutable.fromJS({id: action.id});

    case RECEIVE_DETAILS:
        console.log("Action Details is "+ action.details);
      return state.merge({
        details: action.details
      });

    case FLIP_CARD:
      if (state._root.entries[0][1]!== action.id){  //state.id == action.
        return state
      }
      return state.merge({
        flipped: state._root.entries[2][1] //state.flipped
      });

    default:
      return state;
  }
};

export default details;