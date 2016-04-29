import Immutable from 'immutable';
import {INDEX_CHANGE, SEARCH, SEARCH_COMPLETE} from '../actions/search.js';

const initialState = Immutable.Map({
  text: '',
  index: '',
  inFlight: false
});

const search = (state = initialState, action) => {
  switch (action.type) {
    case INDEX_CHANGE:
      return state.merge({
        index: action.indexText
      });

    case SEARCH:
      return state.merge({
        text: action.searchText,
        inFlight: true
      });

    case SEARCH_COMPLETE:
      return state.merge({
        text: action.searchText,
        inFlight: false
      });

    default:
      return state
  }
};

export default search;