import Immutable from 'seamless-immutable'
import {
  collectionDetailRequest,
  initialState,
} from '../../../src/reducers/search/collectionDetailRequest'
import {
  collectionGetDetailStart,
  collectionGetDetailComplete,
  collectionGetDetailError,
} from '../../../src/actions/get/CollectionDetailRequestActions'

describe('The collection detail request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionDetailRequest(initialState, initialAction)

    expect(result).toEqual({
      inFlight: false,
      requestedID: null,
      errorMessage: '',
    })
  })

  it('new GET marks inFlight, requestedID', function(){
    const initial = Immutable({inFlight: false})
    const result = collectionDetailRequest(
      initial,
      collectionGetDetailStart('uuid')
    )
    expect(result.inFlight).toBeTruthy()
    expect(result.requestedID).toBe('uuid')
  })

  it('new GET resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous GET request',
    })
    const result = collectionDetailRequest(
      initial,
      collectionGetDetailStart('uuid')
    )
    expect(result.errorMessage).toBe('')
  })

  it('result from GET resets inFlight, requestedID', function(){
    const initial = Immutable({inFlight: true})
    const result = collectionDetailRequest(
      initial,
      collectionGetDetailComplete({id: 'ABC'}, 3)
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.requestedID).toBeNull()
  })

  it('error resets inFlight, provides requestedID and errorMessage', function(){
    const initial = Immutable({
      inFlight: true,
      requestedID: '123',
    })
    const result = collectionDetailRequest(
      initial,
      collectionGetDetailError('Cause of Error: test case')
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.requestedID).toBe('123')
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })
})
