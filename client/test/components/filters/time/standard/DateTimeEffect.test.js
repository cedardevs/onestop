import React from 'react'
import {renderHook, act} from '@testing-library/react-hooks'
import {initHook} from '../../../EffectTestHelper'
import {
  useDatetime,
  useDateRange,
} from '../../../../../src/components/filters/time/standard/DateTimeEffect'

const initDate = (name, date) => {
  return initHook(useDatetime, name, date)
}

const getDate = hook => {
  let [ date ] = hook.current
  return date
}

const initDateRange = (start, end) => {
  return initHook(useDateRange, start, end)
}

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

const simulateValidationRequest = hook => {
  // must be a separate act, because the other useEffects need to propagate side effects first (also must get the current updated bounds value)
  // group level validation only done on request (specifically prior to submitting)
  let result = null
  act(() => {
    const [
      start,
      end,
      clear,
      validate,
      asDateStrings,
      errorCumulative,
    ] = hook.current
    result = validate()
  })
  return result
}

describe('The DateTimeEffect hook', () => {
  describe('useDatetime', () => {
    describe('initial conditions', () => {
      test('default', () => {
        const hook = initDate('default', null)
        let date = getDate(hook)
        expect(date.year.value).toEqual('')
        expect(date.month.value).toEqual('')
        expect(date.day.value).toEqual('')
        expect(date.time.value).toEqual('')
      })

      test('with a date', () => {
        const hook = initDate('default', '2010-02-13T14:32:12Z')
        let date = getDate(hook)
        expect(date.year.value).toEqual('2010')
        expect(date.month.value).toEqual('1') // 0 - 11 internally
        expect(date.day.value).toEqual('13')
        expect(date.time.value).toEqual('14:32:12')
      })

      describe('DO NOT DO THIS', () => {
        test('bad initial value', () => {
          // empty string isn't a valid - the way the redux store is populated should prevent this, but if you see NaN rendered into the form, that's where to start debugging
          // TODO this is the only filter effect that pukes on empty strings - fix this so that empty string is a valid init value
          const hook = initDate('default', '')
          let date = getDate(hook)
          expect(date.year.value).toEqual('NaN')
          expect(date.month.value).toEqual('NaN')
          expect(date.day.value).toEqual('NaN')
          expect(date.time.value).toEqual('NaN:NaN:NaN')
        })
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
        expect(start.time.value).toEqual('')
        expect(end.year.value).toEqual('')
        expect(end.month.value).toEqual('')
        expect(end.day.value).toEqual('')
        expect(end.time.value).toEqual('')
      })

      test('with a date', () => {
        const hook = initDateRange('2001-02-03T14:32:15', '2004-05-06T17:43:21')
        let [ start, end ] = hook.current
        expect(start.year.value).toEqual('2001')
        expect(start.month.value).toEqual('1') // 0 - 11 internally
        expect(start.day.value).toEqual('3')
        expect(start.time.value).toEqual('14:32:15')
        expect(end.year.value).toEqual('2004')
        expect(end.month.value).toEqual('4') // 0 - 11 internally
        expect(end.day.value).toEqual('6')
        expect(end.time.value).toEqual('17:43:21')
      })
    })
  })

  describe('flow', () => {
    test('init variable updated externally', () => {
      // move to init describe block?
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
      expect(date.time.value).toEqual('')

      // simulate external change to variable + rerender
      initialValue = '2001-01-01T11:12:13'
      rerender()

      date = getDate(result)
      expect(date.year.value).toEqual('2001')
      expect(date.month.value).toEqual('0')
      expect(date.day.value).toEqual('1')
      expect(date.time.value).toEqual('11:12:13')
    })

    test('external update clears errors', () => {
      let initialValue = '2001-01-01T11:12:13'

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
      expect(date.time.value).toEqual('11:12:13')

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
      expect(date.time.value).toEqual('')
    })

    test('does not bother with group validation if there are individual errors', () => {
      // TODO maybe this test goes under group errors, not flow?
      const hook = initDateRange(null, null)

      simulateStartUserInteraction(hook, 'year', '2020')
      // simulateStartUserInteraction(hook, 'month', '0') // missing month
      simulateStartUserInteraction(hook, 'day', 'foo') // day error
      simulateEndUserInteraction(hook, 'year', '2010')
      simulateEndUserInteraction(hook, 'month', '0')
      simulateEndUserInteraction(hook, 'day', '1')

      let validateResult = simulateValidationRequest(hook)
      expect(validateResult).toBeFalsy()

      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // individual field expectations
        expect(start.valid).toBeFalsy()
        expect(end.valid).toBeTruthy()
        // field error
        expect(start.day.errors.field).toEqual('Start day invalid.')
        // no range error (yet)
        expect(errorCumulative).toEqual('')
      }

      // fix day error
      simulateStartUserInteraction(hook, 'day', '1')
      validateResult = simulateValidationRequest(hook)
      expect(validateResult).toBeFalsy()

      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // individual field expectations
        expect(start.valid).toBeFalsy()
        expect(end.valid).toBeTruthy()
        // field error
        expect(start.day.errors.field).toEqual('')
        expect(start.month.errors.fieldset).toEqual('Start month required.')
        // no range error (yet)
        expect(errorCumulative).toEqual('')
      }
      // fix month error
      simulateStartUserInteraction(hook, 'month', '0')
      validateResult = simulateValidationRequest(hook)
      expect(validateResult).toBeFalsy()

      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // individual field expectations
        expect(start.valid).toBeFalsy()
        expect(end.valid).toBeFalsy()
        // field error
        expect(start.day.errors.field).toEqual('')
        expect(start.month.errors.fieldset).toEqual('')
        // no range error (yet)
        expect(errorCumulative).toEqual('Start date must be before end date.')
      }
    })

    test('changing values updates asMap', () => {
      const hook = initDateRange(null, null)

      {
        // scope variables from hook
        const [ start, end ] = hook.current
        expect(start.asMap).toEqual({
          year: null,
          month: null,
          day: null,
          hour: null,
          minute: null,
          second: null,
        })
      }

      simulateStartUserInteraction(hook, 'year', '2020')
      {
        // scope variables from hook
        const [ start, end ] = hook.current
        expect(start.asMap).toEqual({
          year: 2020,
          month: null,
          day: null,
          hour: null,
          minute: null,
          second: null,
        })
      }

      simulateStartUserInteraction(hook, 'month', '0')
      {
        // scope variables from hook
        const [ start, end ] = hook.current
        expect(start.asMap).toEqual({
          year: 2020,
          month: 0,
          day: null,
          hour: null,
          minute: null,
          second: null,
        })
      }
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

    test('invalid day value', () => {
      const hook = initDateRange(null, null)

      simulateStartUserInteraction(hook, 'year', '2000')
      simulateStartUserInteraction(hook, 'month', '1') // Febuary
      simulateStartUserInteraction(hook, 'day', '31')

      const [ start, end ] = hook.current

      expect(start.day.valid).toBeFalsy()
      expect(start.valid).toBeFalsy()
      expect(start.day.errors.field).toEqual('Start day invalid.')
    })

    _.each(testCases.NaN, c => {
      // note: more specific errors are covered by the test for the util funtion
      test(`start date: not a number - for ${c.field}='${c.value}'`, () => {
        const hook = initDateRange(null, null)

        simulateStartUserInteraction(hook, c.field, c.value)

        const [ start, end ] = hook.current

        // generic invalid expectations
        expect(start[c.field].valid).toBeFalsy()
        expect(start.valid).toBeFalsy()

        // internal tracking side effects
        expect(start[c.field].validInternal).toBeFalsy()
        expect(start[c.field].errors.fieldset).toEqual('')

        // specific invalid expectations
        expect(start[c.field].value).toEqual(c.value)
        expect(start[c.field].errors.field).toEqual(c.startError)
      })

      test(`end date: not a number - for ${c.field}='${c.value}'`, () => {
        const hook = initDateRange(null, null)

        simulateEndUserInteraction(hook, c.field, c.value)

        const [ start, end ] = hook.current

        // generic invalid expectations
        expect(end[c.field].valid).toBeFalsy()
        expect(end.valid).toBeFalsy()

        // internal tracking side effects
        expect(end[c.field].validInternal).toBeFalsy()
        expect(end[c.field].errors.fieldset).toEqual('')

        // specific invalid expectations
        expect(end[c.field].value).toEqual(c.value)
        expect(end[c.field].errors.field).toEqual(c.endError)
      })
    })
  })

  describe('validation - group level (range)', () => {
    // note: group level effects for a single date are essentially confirmed via anytime start.valid or end.valid is tested within another test
    test('start before end', () => {
      const hook = initDateRange(null, null)

      simulateStartUserInteraction(hook, 'year', '2020')
      simulateStartUserInteraction(hook, 'month', '0')
      simulateStartUserInteraction(hook, 'day', '1')
      simulateEndUserInteraction(hook, 'year', '2010')
      simulateEndUserInteraction(hook, 'month', '0')
      simulateEndUserInteraction(hook, 'day', '1')
      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // individual field expectations
        expect(start.valid).toBeTruthy()
        expect(end.valid).toBeTruthy()
        expect(errorCumulative).toEqual('')
      }

      const validateResult = simulateValidationRequest(hook)
      expect(validateResult).toBeFalsy()

      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // individual field expectations
        expect(start.valid).toBeFalsy()
        expect(end.valid).toBeFalsy()
        // no errors on any specific part of start
        expect(start.year.errors.fieldset).toEqual('')
        expect(start.year.errors.field).toEqual('')
        expect(start.month.errors.fieldset).toEqual('')
        expect(start.month.errors.field).toEqual('')
        expect(start.day.errors.fieldset).toEqual('')
        expect(start.day.errors.field).toEqual('')
        // just on the cumulative (range)
        expect(errorCumulative).toEqual('Start date must be before end date.')
      }
    })

    test('(flow) change clears cumulative error', () => {
      const hook = initDateRange(null, null)

      simulateStartUserInteraction(hook, 'year', '2020')
      simulateStartUserInteraction(hook, 'month', '0')
      simulateStartUserInteraction(hook, 'day', '1')
      simulateEndUserInteraction(hook, 'year', '2010')
      simulateEndUserInteraction(hook, 'month', '0')
      simulateEndUserInteraction(hook, 'day', '1')

      const validateResult = simulateValidationRequest(hook)
      expect(validateResult).toBeFalsy()

      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // just on the cumulative (range)
        expect(errorCumulative).toEqual('Start date must be before end date.')
      }

      simulateStartUserInteraction(hook, 'year', '2000')
      // range revalidation has not occured, just immediate validation
      {
        // scope variables from hook
        const [
          start,
          end,
          clear,
          validate,
          asDateStrings,
          errorCumulative,
        ] = hook.current

        // just on the cumulative (range)
        expect(errorCumulative).toEqual('')
      }
    })
  })

  describe('marks subfields as required', () => {
    const testCases = {
      required: [
        {
          field: 'month',
          value: '3',
          yearRequired: true,
          monthRequired: false,
          yearError: 'End year required.',
          monthError: '',
        },
        {
          field: 'day',
          value: '20',
          yearRequired: true,
          monthRequired: true,
          yearError: 'End year required.',
          monthError: 'End month required.',
        },
      ],
    }

    describe('group validation', () => {
      _.each(testCases.required, c => {
        test(`start date: ${c.field} marks other fields as required`, () => {
          const hook = initDateRange(null, null)

          simulateEndUserInteraction(hook, c.field, c.value)

          simulateValidationRequest(hook)

          const [ start, end ] = hook.current

          // unlike before validation, after validation, there are errors on the fields due to missing required fields
          expect(end.valid).toBeFalsy()
          expect(end[c.field].errors.field).toEqual('') // no field level errors
          expect(end.year.valid).toEqual(!c.yearRequired)
          expect(end.month.valid).toEqual(!c.monthRequired)
          expect(end.year.errors.fieldset).toEqual(c.yearError)
          expect(end.month.errors.fieldset).toEqual(c.monthError)

          // required field expectations
          expect(end.year.required).toEqual(c.yearRequired)
          expect(end.month.required).toEqual(c.monthRequired)
        })
      })
    })

    describe('immediate effects', () => {
      _.each(testCases.required, c => {
        test(`start date: ${c.field} marks other fields as required`, () => {
          const hook = initDateRange(null, null)

          simulateStartUserInteraction(hook, c.field, c.value)

          const [ start, end ] = hook.current

          // generic invalid expectations
          // marking year and month fields as optional doesn't immediately affect validation (not considered "field level" error - this is a standard 508 expectation)
          expect(start[c.field].valid).toBeTruthy()
          expect(start.valid).toBeTruthy()

          // required field expectations
          expect(start.year.required).toEqual(c.yearRequired)
          expect(start.month.required).toEqual(c.monthRequired)
        })
      })
    })
  })
})
