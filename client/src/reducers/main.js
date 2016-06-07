import Immutable from 'immutable'
import search from './../search/SearchReducer'
import results from './../result/ResultReducer'
import details from './../detail/DetailReducer'

const reducer = (state = Immutable.Map(), action) => {
  return state.merge({
    search: search(state.get('search'), action),
    results: results(state.get('results'), action),
    details: details(state.get('details'), action)
  })
};

export default reducer
