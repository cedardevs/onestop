import 'babel-polyfill';
import chai from 'chai';
import reducer from '../src/reducers/main';

describe('The main reducer', function() {
  before(function () {
    chai.should();
  });

  it('should exist', function() {
    reducer.should.exist
  });
});