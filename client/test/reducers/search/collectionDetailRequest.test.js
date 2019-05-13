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
      collectionDetailRequestInFlight: false,
      requestedID: null,
      errorMessage: '',
    })
  })

  describe('updates in-flight value', function(){
    it('when starting a new get-by-id', function(){
      const initial = Immutable({collectionDetailRequestInFlight: false})
      const result = collectionDetailRequest(
        initial,
        collectionGetDetailStart('uuid')
      )
      expect(result.collectionDetailRequestInFlight).toBeTruthy()
      expect(result.requestedID).toBe('uuid')
      expect(result.errorMessage).toBe('')
    })
    it('when starting a new get-by-id asdf', function(){
      const initial = Immutable({
        errorMessage: 'error from previous GET request',
      })
      const result = collectionDetailRequest(
        initial,
        collectionGetDetailStart('uuid')
      )
      expect(result.collectionDetailRequestInFlight).toBeTruthy()
      expect(result.requestedID).toBe('uuid')
      expect(result.errorMessage).toBe('')
    })

    it('when detail request completes', function(){
      const initial = Immutable({collectionDetailRequestInFlight: true})
      const result = collectionDetailRequest(
        initial,
        collectionGetDetailComplete({id: 'ABC'}, 3)
      )
      expect(result.collectionDetailRequestInFlight).toBeFalsy()
      expect(result.requestedID).toBeNull()
    })

    it('when request errors', function(){
      const initial = Immutable({
        collectionDetailRequestInFlight: true,
        requestedID: '123',
      })
      const result = collectionDetailRequest(
        initial,
        collectionGetDetailError('Cause of Error: test case')
      )
      expect(result.collectionDetailRequestInFlight).toBeFalsy()
      expect(result.requestedID).toBe('123')
      expect(result.errorMessage).toBe('Cause of Error: test case')
    })
  })
})
