import Immutable from 'immutable';
import {SEARCH_COMPLETE} from '../actions/search.js';

const results = (state = Immutable.List(), action) => {
  switch(action.type) {
    case SEARCH_COMPLETE:
      return Immutable.fromJS(action.items);

    default:
      return state
  }
};

export default results;