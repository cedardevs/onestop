import React from 'react'
import {renderHook, act} from '@testing-library/react-hooks'

import {
  useDatetime,
  useDateRange,
} from '../../../../../src/components/filters/time/standard/DateTimeEffect'

const initHook = (hookFunction, ...hookArgs) => {
  // generic init hook function
  let hook = null
  // act(() => {
  const {result} = renderHook(() => hookFunction(...hookArgs)) // TODO test ...hookArgs with hook that has more than one param (DONE) TODO now pull this out into a generic import for both test files!
  hook = result
  return hook
  // })

  // return [hook, rerender]
}

// const initHookWithRerender = (hookFunction, ...hookArgs) => {
//   // generic init hook function
//   let hook = null
//   // act(() => {
//     const {result, rerender} = renderHook(() => hookFunction(...hookArgs)) // TODO test ...hookArgs with hook that has more than one param (DONE) TODO now pull this out into a generic import for both test files!
//     hook = result
//     return [hook, rerender]
//   // })
//
//   // return [hook, rerender]
// }
//
// const initHookTake2 = (hookFunction, ...hookArgs) => {
//   let hook = null
//   const { result, rerender } = renderHook(() => hookFunction(...hookArgs))
//   hook = result
//   return [hook, rerender]
// }

const initDate = (name, date) => {
  return initHook(useDatetime, name, date) // note: DO NOT PASS IN '' for date
}

const getDate = hook => {
  let [ date ] = hook.current
  return date
}

const initDateRange = (start, end) => {
  return initHook(useDateRange, start, end)
}
// const init = bbox => {
//   return initHook(useBoundingBox, bbox)
// }
//
// const getBounds = hook => {
//   // get updated bounds from the hook
//   // made this a separate method because the notation is a little weird
//   let [ bounds ] = hook.current
//   return bounds
// }
//
const simulateStartUserInteraction = (hook, field, value) => {
  act(() => {
    // this is currently how a component is expected to update values based on user interaction events:

    const [ start, end ] = hook.current
    start[field].set(value)
  })
}
const simulateEndUserInteraction = (hook, field, value) => {
  act(() => {
    // this is currently how a component is expected to update values based on user interaction events:

    const [ start, end ] = hook.current
    end[field].set(value)
  })
}
//
// const simulateValidationRequest = hook => {
//   // must be a separate act, because the other useEffects need to propagate side effects first (also must get the current updated bounds value)
//   // group level validation only done on request (specifically prior to submitting)
//   let result = null
//   act(() => {
//     const bounds = getBounds(hook)
//     result = bounds.validate()
//   })
//   return result
// }

describe('The DateTimeEffect hook', () => {
  describe('useDatetime', () => {
    describe('initial conditions', () => {
      test('default', () => {
        const hook = initDate('default', null)
        let date = getDate(hook)
        expect(date.year.value).toEqual('')
        expect(date.month.value).toEqual('')
        expect(date.day.value).toEqual('')
      })

      test('with a date', () => {
        const hook = initDate('default', '2010-02-13T14:32:12')
        let date = getDate(hook)
        expect(date.year.value).toEqual('2010')
        expect(date.month.value).toEqual('1') // 0 - 11 internally
        expect(date.day.value).toEqual('13')
      })
    })
  })

  describe('useDateRange', () => {
    describe('initial conditions', () => {
      test('default', () => {
        const hook = initDateRange(null, null)
        let [ start, end ] = hook.current
        expect(start.year.value).toEqual('')
        expect(start.month.value).toEqual('')
        expect(start.day.value).toEqual('')
        expect(end.year.value).toEqual('')
        expect(end.month.value).toEqual('')
        expect(end.day.value).toEqual('')
      })

      test('with a date', () => {
        const hook = initDateRange('2001-02-03', '2004-05-06')
        let [ start, end ] = hook.current
        expect(start.year.value).toEqual('2001')
        expect(start.month.value).toEqual('1') // 0 - 11 internally
        expect(start.day.value).toEqual('3')
        expect(end.year.value).toEqual('2004')
        expect(end.month.value).toEqual('4') // 0 - 11 internally
        expect(end.day.value).toEqual('6')
      })
    })
  })

  describe('DO NOT DO THIS', () => {
    test('bad initial value', () => {
      // empty string isn't a valid - the way the redux store is populated should prevent this, but if you see NaN rendered into the form, that's where to start debugging
      const hook = initDate('default', '')
      let date = getDate(hook)
      expect(date.year.value).toEqual('NaN')
      expect(date.month.value).toEqual('NaN')
      expect(date.day.value).toEqual('NaN')
    })
  })

  describe('flow', () => {
    test('init variable updated externally', () => {
      //
      let initialValue = null

      // including rerender in the result seems to make things more fragile but is needed to test external changes to variable used to init the hook
      // note naming the variable 'result' as 'hook' makes everything break for no reason
      const {result, rerender} = renderHook(() =>
        useDatetime('test', initialValue)
      )

      let date = getDate(result)
      expect(date.year.value).toEqual('')
      expect(date.month.value).toEqual('')
      expect(date.day.value).toEqual('')

      // simulate external change to variable + rerender
      initialValue = '2001-01-01'
      rerender()

      date = getDate(result)
      expect(date.year.value).toEqual('2001')
      expect(date.month.value).toEqual('0')
      expect(date.day.value).toEqual('1')
    })

    test('external update clears errors', () => {
      let initialValue = '2001-01-01'

      // including rerender in the result seems to make things more fragile but is needed to test external changes to variable used to init the hook
      // note naming the variable 'result' as 'hook' makes everything break for no reason
      const {result, rerender} = renderHook(() =>
        useDatetime('test', initialValue)
      )

      let date = getDate(result)
      expect(date.year.value).toEqual('2001')
      expect(date.year.valid).toBeTruthy()
      expect(date.month.value).toEqual('0')
      expect(date.day.value).toEqual('1')

      act(() => {
        const d = getDate(result)
        // this is currently how a component is expected to update values based on user interaction events:
        d.year.set('foo')
      })

      date = getDate(result)
      expect(date.year.valid).toBeFalsy()
      expect(date.year.value).toEqual('foo')

      // simulate external change to variable + rerender
      initialValue = null
      rerender()

      date = getDate(result)
      expect(date.year.valid).toBeTruthy()
      expect(date.year.value).toEqual('')
      expect(date.month.value).toEqual('')
      expect(date.day.value).toEqual('')
    })
  })

  describe('validation - field level', () => {
    const testCases = {
      NaN: [
        {
          field: 'year',
          value: 'foo',
          startError: 'Start year invalid.',
          endError: 'End year invalid.',
        },
        {
          field: 'month',
          value: 'hello',
          startError: 'Start month invalid.',
          endError: 'End month invalid.',
        },
        {
          field: 'day',
          value: 'abc',
          startError: 'Start day invalid.',
          endError: 'End day invalid.',
        },
      ],
    }

    _.each(testCases.NaN, c => {
      test(`start date: not a number - for ${c.field}='${c.value}'`, function(){
        const hook = initDateRange(null, null)

        simulateStartUserInteraction(hook, c.field, c.value)

        const [ start, end ] = hook.current

        // generic invalid expectations
        expect(start[c.field].valid).toBeFalsy()

        // internal tracking side effects
        expect(start[c.field].validInternal).toBeFalsy()
        expect(start[c.field].errors.fieldset).toEqual('')

        // specific invalid expectations
        expect(start[c.field].value).toEqual(c.value)
        expect(start[c.field].errors.field).toEqual(c.startError)
      })

      test(`end date: not a number - for ${c.field}='${c.value}'`, function(){
        const hook = initDateRange(null, null)

        simulateEndUserInteraction(hook, c.field, c.value)

        const [ start, end ] = hook.current

        // generic invalid expectations
        expect(end[c.field].valid).toBeFalsy()

        // internal tracking side effects
        expect(end[c.field].validInternal).toBeFalsy()
        expect(end[c.field].errors.fieldset).toEqual('')

        // specific invalid expectations
        expect(end[c.field].value).toEqual(c.value)
        expect(end[c.field].errors.field).toEqual(c.endError)
      })
    })
  })
})
