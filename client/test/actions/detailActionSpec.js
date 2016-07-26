import '../specHelper'
import * as actions from '../../src/detail/DetailActions'

describe('The detail action', function () {

  it('set Card Status ', function () {
    const id = 'a'
    const detailsAction = actions.setCardStatus(id);
    const expectedAction =  { type: 'SET_CARD_STATUS', id: 'a' }

    detailsAction.should.deep.equal(expectedAction)
  })
})
