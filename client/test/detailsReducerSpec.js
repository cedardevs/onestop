import './specHelper'
import * as  detail from '../src/detail/DetailReducer';

describe('The details reducer',() => {
  it('handles search request',() => {
    const initialState = detail.initialState;
    const initalAction =  { type: 'search_complete',
                              searchText: 'test',
                              items: [{id:'1'}]}

    const result = detail.details(initialState, initalAction);

    // result.should.not.equal(initialState);

    var str =result;
    console.log(str.toString())

  });

});