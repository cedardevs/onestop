import 'babel-polyfill'
import chai from 'chai'
import chaiAsPromised from 'chai-as-promised'

before(function () {
  chai.use(chaiAsPromised)
  chai.should()
  chai.expect()
})
