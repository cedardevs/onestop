import Immutable from 'immutable'
import { TOGGLE_GRANULE_FOCUS } from './GranulesActions'

export const initialState = Immutable.Map({
  selectedCollections: Immutable.Set(),
  focusedGranules: Immutable.Set(),
  granules: Immutable.Map()
})

export const granules = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_GRANULE_FOCUS:
      const current = state.get('focusedGranules')
      const next = current.has(action.id) ? current.delete(action.id) : current.add(action.id)
      return state.set('focusedGranules', next)

    default:
      return state
  }
}

export default granules