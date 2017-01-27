import '../../specHelper'
import { cardDetails, initialState } from '../../../src/reducers/ui/cardDetails'
import { setFocus } from '../../../src/actions/DetailActions'

describe('The cardDetails reducer',() => {
  it('handles set focus actions',() => {
    const initialState = initialState
    const id = 'a'
    const action =  setFocus(id)

    const result = cardDetails(initialState, action)
    result.focusedId.should.equal(id)
  })
})
