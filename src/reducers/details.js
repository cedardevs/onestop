import Immutable from 'immutable';
import {SEARCH, SEARCH_COMPLETE} from '../actions/search';
import {FETCH_DETAILS, RECEIVE_DETAILS} from '../actions/detail';

export const initialState = Immutable.Map();

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH:
    case SEARCH_COMPLETE:
      return Immutable.Map();

    case FETCH_DETAILS:
      return Immutable.fromJS({id: action.id});

    case RECEIVE_DETAILS:
      return Immutable.fromJS(action.details);

    default:
      return state;
  }
};

export default details;