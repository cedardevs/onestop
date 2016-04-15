import {Map, List} from 'immutable';
import {SEARCH} from '../actions/search.js';

const initialState = Map({
  message: 'Hello, world',
  search: {
    text: ''
  },
  resultsBySearch: {
    '': {
      items: []
    }
  }
});

const reducer = (state = initialState, action) => {
  console.dir(action);
  switch (action.type) {
    case SEARCH:
      console.log(`reducing for search with text: ${action.params.text}`);
      return state.search = Map(action.params);
    default:
      return state;
  }
};

export default reducer;

