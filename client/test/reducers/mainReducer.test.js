import Immutable from 'seamless-immutable'
import reducer from '../../src/reducer'
import history from '../../src/history'

describe('The main reducer', function(){
  it('returns a composed initial state', function(){
    const initialState = Immutable({})
    const initialAction = {type: 'init'}

    const result = reducer(history)(initialState, initialAction)

    expect(result).toBeInstanceOf(Object)
    expect(result.router).toBeInstanceOf(Object)
    expect(result.cart).toBeInstanceOf(Object)
    expect(result.config).toBeInstanceOf(Object)
    expect(result.errors).toBeInstanceOf(Object)
    expect(result.layout).toBeInstanceOf(Object)
    expect(result.routing).toBeInstanceOf(Object)
    expect(result.user).toBeInstanceOf(Object)
    expect(result.search.collectionFilter).toBeInstanceOf(Object)
    expect(result.search.collectionRequest).toBeInstanceOf(Object)
    expect(result.search.collectionResult).toBeInstanceOf(Object)
    // TODO: add granule reducer checks
    expect(result.search.info).toBeInstanceOf(Object)
    expect(result.search.loading).toBeFalsy()
  })
})
