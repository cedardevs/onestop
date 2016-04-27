import Immutable from 'immutable';
import {INDEX_CHANGE, SEARCH, SEARCH_COMPLETE} from '../actions/search.js';
import {FETCH_DETAILS, RECEIVE_DETAILS} from '../actions/detail.js';

export const initialState = Immutable.fromJS({
  search: '',
  indexText: '',
  inFlight: false,
  results: [],
  details: {}
});

const reducer = (state = initialState, action) => {
  switch (action.type) {
    case INDEX_CHANGE:
      return state.merge({
        indexText: action.indexText
      });

    case SEARCH:
      return state.merge({
        search: action.searchText,
        inFlight: true,
        details: {}
      });

    case SEARCH_COMPLETE:
      return state.merge({
        search: action.searchText,
        results: action.items,
        inFlight: false,
        details: {}
      });

    case FETCH_DETAILS:
      return state.merge({
        inFlight: true,
        details: {id: action.id}
      });

    case RECEIVE_DETAILS:
      return state.merge({
        inFlight: false,
        details: action.details
      });

    default:
      return state;
  }
};

export default reducer;

