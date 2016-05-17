import Immutable from 'immutable';
import {SEARCH_COMPLETE} from '../search/SearchActions';

export const initialState = Immutable.List();

const results = (state = initialState, action) => {
  switch(action.type) {
    case SEARCH_COMPLETE:
      return Immutable.fromJS(action.items);

    default:
      return state
  }
};

export default results;