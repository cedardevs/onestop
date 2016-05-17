import 'babel-polyfill'
import chai from 'chai'
import chaiImmutable from 'chai-immutable'
import chaiAsPromised from 'chai-as-promised'

before(function () {
  chai.should();
  chai.use(chaiImmutable);
  chai.use(chaiAsPromised);
});