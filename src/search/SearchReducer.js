import Immutable from 'immutable';
import {INDEX_CHANGE, SEARCH, SEARCH_COMPLETE} from './SearchActions';

export const initialState = Immutable.Map({
  text: '',
  index: '',
  inFlight: false
});

export const search = (state = initialState, action) => {
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