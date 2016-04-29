import Immutable from 'immutable';
import search from './search'
import results from './results'
import details from './details'

const reducer = (state = Immutable.Map(), action) => {
  return state.merge({
    search: search(state.get('search'), action),
    results: results(state.get('results'), action),
    details: details(state.get('details'), action)
  })
};

export default reducer;

