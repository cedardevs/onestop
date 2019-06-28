import Immutable from 'seamless-immutable'
import {
  collectionDetailRequest,
  initialState,
} from '../../../src/reducers/search/collectionDetailRequest'
import {
  collectionDetailRequested,
  collectionDetailReceived,
  collectionDetailError,
  granuleMatchingCountRequested,
  granuleMatchingCountReceived,
  granuleMatchingCountError,
} from '../../../src/actions/routing/CollectionDetailStateActions'

// describe('collectionDetailRequest', function() {
//   testCases.forEach(function(testCase){
//     it(`with ${testCase.name}`, function(){
//       const initial = {
//         inFlight: false,
//
//       }
//       const result = collectionDetailRequest(
//         initial,
//         collectionDetailRequested('uuid')
//       )
//       expect(result.inFlight).toEqual(testCase.inFlight)
//       expect(result.requestedID).toEqual(testCase.requestedID)
//       expect(result.errorMessage).toEqual('')
//     })
//   })
// })

describe('The collection detail request reducer', function(){
  it('has a default state', function(){
    const result = collectionDetailRequest(initialState, {})

    expect(result).toEqual({
      inFlight: false,
      requestedID: null,
      errorMessage: '',
      backgroundInFlight: false,
      backgroundErrorMessage: '',
    })
  })

  it('new GET marks inFlight, requestedID', function(){
    const initial = Immutable({inFlight: false})
    const result = collectionDetailRequest(
      initial,
      collectionDetailRequested('uuid')
    )
    expect(result.inFlight).toBeTruthy()
    expect(result.requestedID).toBe('uuid')
  })

  it('background request started marks background inFlight', function(){
    const initial = Immutable({
      backgroundInFlight: false,
      backgroundErrorMessage: 'error from previous call',
    })
    const result = collectionDetailRequest(
      initial,
      granuleMatchingCountRequested()
    )
    expect(result.backgroundInFlight).toBeTruthy()
    expect(result.backgroundErrorMessage).toBe('')
  })

  it('new GET resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous GET request',
    })
    const result = collectionDetailRequest(
      initial,
      collectionDetailRequested('uuid')
    )
    expect(result.errorMessage).toBe('')
  })

  it('result from GET resets inFlight, requestedID', function(){
    const initial = Immutable({inFlight: true})
    const result = collectionDetailRequest(
      initial,
      collectionDetailReceived({id: 'ABC'}, 3)
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.requestedID).toBeNull()
  })

  it('result from backround resets inFlight', function(){
    const initial = Immutable({backgroundInFlight: true})
    const result = collectionDetailRequest(
      initial,
      granuleMatchingCountReceived(7)
    )
    expect(result.backgroundInFlight).toBeFalsy()
  })

  it('error resets inFlight, provides requestedID and errorMessage', function(){
    const initial = Immutable({
      inFlight: true,
      requestedID: '123',
    })
    const result = collectionDetailRequest(
      initial,
      collectionDetailError('Cause of Error: test case')
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.requestedID).toBe('123')
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })

  it('error resets background for inFlight, and errorMessage', function(){
    const initial = Immutable({
      backgroundInFlight: true,
    })
    const result = collectionDetailRequest(
      initial,
      granuleMatchingCountError('Cause of Error: test case')
    )
    expect(result.backgroundInFlight).toBeFalsy()
    expect(result.backgroundErrorMessage).toBe('Cause of Error: test case')
  })
})
