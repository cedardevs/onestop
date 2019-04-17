import Immutable from 'seamless-immutable'
import reducer from '../../src/reducer'

describe('The main reducer', function(){
  it('returns a composed initial state', function(){
    const initialState = new Map()
    const initialAction = {type: 'init'}

    const result = reducer(initialState, initialAction)

    expect(result).toBeInstanceOf(Object)
    expect(result.domain).toBeInstanceOf(Object)
    expect(result.behavior).toBeInstanceOf(Object)
    expect(result.behavior.collectionFilter).toBeInstanceOf(Object)
    expect(result.behavior.routing).toBeInstanceOf(Object)
    expect(result.behavior.errors).toBeInstanceOf(Object)
    expect(result.ui).toBeInstanceOf(Object)
  })
})
