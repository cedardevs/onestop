import './specHelper'
import * as detail from '../src/detail/DetailReducer'

describe('The details reducer',() => {
  it('handles search request',() => {
    const initialState = detail.initialState
    const initalAction =  { type: 'search_complete',
                              searchText: 'test',
                              items: [{id:'1'}]}

    const result = detail.details(initialState, initalAction).toJS()
    console.log(JSON.stringify(result))

    for (let [k, v] of Object.entries(result)) {
      var cardStatus = v.cardStatus
      cardStatus.should.equal('SHOW_FRONT')
    }
  })
})
