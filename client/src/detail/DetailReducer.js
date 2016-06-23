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
      return Immutable.fromJS({id: action.id, flipped: false});

    case RECEIVE_DETAILS:
        console.log("Action Details is "+ action.details);
      return state.merge({
        details: action.details
      })

    case FLIP_CARD:
      if (state.get('id') == action.id){
        return state.merge({
          flipped:  !state.get('flipped')
        })
      }

    default:
      return state;
  }
};

export default details;
