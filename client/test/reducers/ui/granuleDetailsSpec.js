import '../../specHelper'
import { granuleDetails, initialState } from '../../../src/reducers/ui/granuleDetails'
import { toggleGranuleFocus } from '../../../src/actions/FlowActions'

describe('The granuleDetails reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granuleDetails(initialState, initialAction)

    // result.focusedGranules.should.deep.equal([])
  })

  it('toggles focused granules', function () {
    const toggleATrue = toggleGranuleFocus('A', true)
    const toggleAFalse = toggleGranuleFocus('A', false)
    // toggle A --> ['A']
    const addedAResult = granuleDetails(initialState, toggleATrue)
    addedAResult.focusedGranules.should.deep.equal(['A'])
    const removedAResult = granuleDetails(initialState, toggleAFalse)
    removedAResult.focusedGranules.should.deep.equal([])
  })

})
