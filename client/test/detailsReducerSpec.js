import './specHelper'
import * as  detail from '../src/detail/DetailReducer';

describe('The details reducer',() => {
  it('handles search request',() => {
    const initialState = detail.initialState;
    const initalAction =  { type: 'search_complete',
                              searchText: 'test',
                              items: [{id:'1'}]}

    const result = detail.details(initialState, initalAction);

    result.every(function(elem) {
      var card = elem.cardStatus;
      card.should.deep.equal('SHOW_FRONT')
    });
  });

});