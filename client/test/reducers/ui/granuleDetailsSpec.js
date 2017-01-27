import '../../specHelper'
import Immutable from 'seamless-immutable'
import { granuleDetails, initialState } from '../../../src/reducers/ui/granuleDetails'
import { toggleGranuleFocus } from '../../../src/search/SearchActions'

describe('The granuleDetails reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granuleDetails(initialState, initialAction)

    // result.focusedGranules.should.deep.equal([])
  })

  it('toggles focused granules', function () {
    const toggleA = toggleGranuleFocus('A')
    const toggleB = toggleGranuleFocus('B')
    // toggle A --> ['A']
    const addedAResult = granuleDetails(initialState, toggleA)
    addedAResult.focusedGranules.should.deep.equal(['A'])
    // toggle B --> ['A', 'B']
    const addedBResult = granuleDetails(addedAResult, toggleB)
    addedBResult.focusedGranules.should.deep.equal(['A', 'B'])
    // toggle A --> ['B']
    const removedAResult = granuleDetails(addedBResult, toggleA)
    removedAResult.focusedGranules.should.deep.equal(['B'])
  })

})
