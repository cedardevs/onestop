import '../specHelper'
import * as detail from '../../src/detail/DetailReducer'

describe('The details reducer',() => {
  it('handles search request',() => {
    const initialState = detail.initialState
    const testItems = {'a': {id: 'a'}}
    const initalAction =  { type: 'search_complete',
                              searchText: 'test',
                              items: testItems}

    const result = detail.details(initialState, initalAction)
    result['a'].cardStatus.should.equal('SHOW_FRONT')
  })
})
