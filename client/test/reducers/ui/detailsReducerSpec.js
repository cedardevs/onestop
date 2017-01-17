import '../../specHelper'
import { cardDetails, initialState } from '../../../src/reducers/ui/cardDetails'

describe('The cardDetails reducer',() => {
  it('handles search request',() => {
    const initialState = initialState
    const testItems = {'a': {id: 'a'}}
    const initalAction =  { type: 'search_complete',
                              searchText: 'test',
                              items: testItems}

    const result = cardDetails(initialState, initalAction)
    result['a'].cardStatus.should.equal('SHOW_FRONT')
  })
})
