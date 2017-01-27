import '../specHelper'
import * as actions from '../../src/detail/DetailActions'

describe('The detail action', function () {

  it('sets the focused id', function () {
    const id = 'a'
    const detailsAction = actions.setFocus(id)
    const expectedAction =  { type: 'SET_FOCUS', id: 'a' }

    detailsAction.should.deep.equal(expectedAction)
  })
})
