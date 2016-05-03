import Immutable from 'immutable';
import {SEARCH, SEARCH_COMPLETE} from '../search/SearchActions';
import {FETCH_DETAILS, RECEIVE_DETAILS} from './DetailActions';

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