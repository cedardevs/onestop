import Immutable from 'immutable';
import {SEARCH, SEARCH_COMPLETE} from '../actions/search.js';

const initialState = Immutable.fromJS({
  search: '',
  inFlight: false,
  results: []
});

const reducer = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH:
      return state.merge({
        search: action.searchText,
        inFlight: true
      });

    case SEARCH_COMPLETE:
      return state.merge({
        results: action.items,
        inFlight: false
      });

    default:
      return state;
  }
};

export default reducer;

