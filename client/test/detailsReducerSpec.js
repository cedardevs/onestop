import './specHelper'
import Immutable from 'immutable';
import * as  detail from '../src/detail/DetailReducer';

describe('The details reducer', function () {
  it('handles completed details request', function () {
      // const id = 'a';
      const initialState = Map({})
      const initialAction =   {
        "flipped": false,
        "id": "a",
        "type": "SET_CARD_STATUS"
    }


      const result = detail.details(detail.initialState, initialAction);
      console.log(result)
      // // result.should.not.equal(detail.initialState);
      // result.should.equal(Immutable.fromJS(expectedResult));

  });

});