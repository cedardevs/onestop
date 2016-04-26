import 'babel-polyfill';
import chai from 'chai';
import chaiImmutable from 'chai-immutable';
import Immutable from 'immutable';
import reducer, { initialState } from '../src/reducers/main';
import { SEARCH, SEARCH_COMPLETE, startSearch, completeSearch } from '../src/actions/search';
import { FETCH_DETAILS, RECEIVE_DETAILS, startDetails, completeDetails} from '../src/actions/detail';

describe('The main reducer', function() {
  before(function () {
    chai.use(chaiImmutable);
    chai.should();
  });

  it('has a default state', function() {
    initialState.should.have.keys('search', 'inFlight', 'results', 'details');
    const initialAction = {type: 'init'};
    const result = reducer(initialState, initialAction);
    result.should.equal(initialState);
  });

  describe('handles search actions', function () {

    it('for a new search', function () {
      const searchText = 'test';
      const searchAction = startSearch(searchText);
      const result = reducer(initialState, searchAction);

      result.should.not.equal(initialState);
      result.get('inFlight').should.equal(true);
      result.get('search').should.equal(searchText);
    });

    it('for a completed search', function () {
      const searchText = 'test';
      const searchResults = [{id: '1'}, {id: '2'}];
      const searchAction = completeSearch(searchText, searchResults);
      const result = reducer(initialState, searchAction);

      result.should.not.equal(initialState);
      result.get('inFlight').should.equal(false);
      result.get('search').should.equal('test');
      result.get('results').should.equal(Immutable.fromJS(searchResults));
    });

  });

  describe('handles detail actions', function () {

    it('for a new details request', function () {
      const id = 'a';
      const detailsAction = startDetails(id);
      const result = reducer(initialState, detailsAction);

      result.should.not.equal(initialState);
      result.get('inFlight').should.equal(true);
      result.get('details').should.equal(Immutable.fromJS({id: id}));
    });

    it('for a completed details request', function () {
      it('for a new details request', function () {
        const id = 'a';
        const details = {id: 'a', summary: 'this is a super cool test result'};
        const detailsAction = completeDetails(id, details);
        const result = reducer(initialState, detailsAction);

        result.should.not.equal(initialState);
        result.get('inFlight').should.equal(false);
        result.get('details').should.equal(Immutable.fromJS(details));
      });
    });
  })

});