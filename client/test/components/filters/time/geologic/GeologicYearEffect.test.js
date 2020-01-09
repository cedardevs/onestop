import React from 'react'
import {renderHook, act} from '@testing-library/react-hooks'
import {initHook} from '../../../EffectTestHelper'
import {useYear} from '../../../../../src/components/filters/time/geologic/GeologicYearEffect'

const init = (year, format) => {
  return initHook(useYear, year, format)
}

const getYear = hook => {
  let [ year ] = hook.current
  return year
}

const simulateUserInteraction = (hook, value) => {
  act(() => {
    // this is currently how a component is expected to update values based on user interaction events:
    const [ year ] = hook.current
    year.setYear(value)
  })
}

describe('The GeologicYearEffect hook', () => {
  describe('initial conditions', () => {
    test('default', () => {
      const hook = init(null, null)
      let year = getYear(hook)
      expect(year.year).toEqual('')
    })

    test('with a year', () => {
      const hook = init(-12341234, null)
      let year = getYear(hook)
      expect(year.year).toEqual('-12341234')
    })

    test('with a year and format', () => {
      const hook = init(-12341234, 'BP')
      let year = getYear(hook)
      expect(year.year).toEqual('12343184')
    })

    test('change input values...', () => {
      let initialValue = null
      let initialFormat = null

      // including rerender in the result seems to make things more fragile but is needed to test external changes to variable used to init the hook
      // note naming the variable 'result' as 'hook' makes everything break for no reason
      const {result, rerender} = renderHook(() =>
        useYear(initialValue, initialFormat)
      )

      let year = getYear(result)
      expect(year.year).toEqual('')
      expect(year.CE).toEqual('')

      initialValue = 1950
      rerender()
      year = getYear(result)
      expect(year.year).toEqual('1950')
      expect(year.CE).toEqual('1950')

      // simulate external change to variable + rerender
      initialFormat = 'BP'
      rerender()
      year = getYear(result)
      expect(year.year).toEqual('0')
      expect(year.CE).toEqual('1950')
    })
  })

  describe('validation', () => {
    // validation done with util that has it's own specific tests
    // TODO and group validation is still done in the component instead of an effect
    test('valid input', () => {
      const hook = init(null, null)
      simulateUserInteraction(hook, '50')
      let year = getYear(hook)
      expect(year.year).toEqual('50')
    })
    test('invalid input', () => {
      const hook = init('50', null)
      simulateUserInteraction(hook, 'foo')
      let year = getYear(hook)
      expect(year.year).toEqual('foo')
      expect(year.CE).toEqual('foo') // this matches year input value if it's not a valid year
      expect(year.valid).toBeFalsy()
    })
  })
})
