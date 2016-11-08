import '../specHelper'
import Immutable from 'immutable'
import { granules, initialState } from '../../src/granules/GranulesReducer'
import { toggleGranuleFocus, toggleCollectionSelection } from '../../src/granules/GranulesActions'

describe('The granules reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granules(initialState, initialAction)

    result.has('focusedGranules').should.equal(true)
  })

  it('toggles focused granules', function () {
    const toggleA = toggleGranuleFocus('A')
    const toggleB = toggleGranuleFocus('B')
    // toggle A --> ['A']
    const addedAResult = granules(initialState, toggleA)
    addedAResult.get('focusedGranules').should.equal(Immutable.Set(['A']))
    // toggle B --> ['A', 'B']
    const addedBResult = granules(addedAResult, toggleB)
    addedBResult.get('focusedGranules').should.equal(Immutable.Set(['A', 'B']))
    // toggle A --> ['B']
    const removedAResult = granules(addedBResult, toggleA)
    removedAResult.get('focusedGranules').should.equal(Immutable.Set(['B']))
  })

  it('toggles selected collections', function () {
    const toggleA = toggleCollectionSelection('A')
    const toggleB = toggleCollectionSelection('B')
    // toggle A --> ['A']
    const addedAResult = granules(initialState, toggleA)
    addedAResult.get('selectedCollections').should.equal(Immutable.Set(['A']))
    // toggle B --> ['A', 'B']
    const addedBResult = granules(addedAResult, toggleB)
    addedBResult.get('selectedCollections').should.equal(Immutable.Set(['A', 'B']))
    // toggle A --> ['B']
    const removedAResult = granules(addedBResult, toggleA)
    removedAResult.get('selectedCollections').should.equal(Immutable.Set(['B']))
  })
})