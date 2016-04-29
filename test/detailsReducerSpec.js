import './specHelper'
import Immutable from 'immutable';
import { details, initialState } from '../src/reducers/details';
import { startDetails, completeDetails } from '../src/actions/detail';

describe('The details reducer', function () {

  it('handles new details request', function () {
    const id = 'a';
    const detailsAction = startDetails(id);
    const result = details(initialState, detailsAction);

    result.should.not.equal(initialState);
    result.should.equal(Immutable.fromJS({id: id}));
  });

  it('handles completed details request', function () {
    it('for a new details request', function () {
      const id = 'a';
      const detailContent = {id: 'a', summary: 'this is a super cool test result'};
      const detailsAction = completeDetails(id, detailContent);
      const result = details(initialState, detailsAction);

      result.should.not.equal(initialState);
      result.should.equal(Immutable.fromJS(details));
    });
  });

});