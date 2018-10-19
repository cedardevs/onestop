import '../specHelper'
import {
  isValidDate,
  isValidDateRange,
  textToNumber,
  ymdToDateMap,
} from '../../src/utils/inputUtils'
import {assert} from 'chai'

describe('The inputUtils', function(){
  it('converts text to numbers', function(){
    const testCases = [
      {input: 1, output: 1},
      {input: 1.2, output: 1.2},
      {input: '0', output: 0},
      {input: '1', output: 1},
      {input: '1.2', output: 1.2},
      {input: '', output: null},
      {input: 'a', output: null},
    ]

    testCases.forEach(c => {
      assert.equal(textToNumber(c.input), c.output, `for input ${c.input}`)
    })
  })

  it('builds date maps', function(){
    const testCases = [
      {
        year: '2000',
        month: '4',
        day: '24',
        output: {year: 2000, month: 4, day: 24},
      },
      {
        year: '2000',
        month: '4',
        day: '',
        output: {year: 2000, month: 4, day: null},
      },
      {
        year: '2000',
        month: '',
        day: '',
        output: {year: 2000, month: null, day: null},
      },
      {
        year: 'notayear',
        month: '1.23',
        day: '',
        output: {year: null, month: null, day: null},
      },
    ]

    testCases.forEach(c => {
      assert.deepEqual(
        ymdToDateMap(c.year, c.month, c.day),
        c.output,
        `for input date ${c.year}-${c.month}-${c.day}`
      )
    })
  })

  it('validates dates', function(){
    const testCases = [
      {year: '', month: '', day: '', output: true},
      {year: '2000', month: '', day: '', output: true},
      {year: '2000', month: '4', day: '', output: true},
      {year: '2000', month: '4', day: '24', output: true},
      {year: '2000', month: '0', day: '1', output: true},
      {year: '2000', month: '', day: '1', output: false},
      {year: 'notayear', month: '', day: '', output: false},
      {year: '1.23', month: '', day: '', output: false},
      {year: '2000', month: 'notamonth', day: '', output: false},
      {year: '2000', month: '1.23', day: '', output: false},
      {year: '2000', month: '0', day: 'notaday', output: false},
      {year: '2000', month: '0', day: '1.23', output: false},
    ]

    testCases.forEach(c => {
      assert.equal(
        isValidDate(c.year, c.month, c.day),
        c.output,
        `for input date ${c.year}-${c.month}-${c.day}`
      )
    })
  })

  it('validates date ranges', function(){
    const testCases = [
      {
        start: {year: null, month: null, day: null},
        end: {year: null, month: null, day: null},
        output: true,
      },
      {
        start: {year: 2000, month: 4, day: 24},
        end: {year: null, month: null, day: null},
        output: true,
      },
      {
        start: {year: null, month: null, day: null},
        end: {year: 2000, month: 4, day: 24},
        output: true,
      },
      {
        start: {year: 2000, month: 4, day: 24},
        end: {year: 2000, month: 4, day: 24},
        output: true,
      },
      {
        start: {year: 2000, month: 4, day: 24},
        end: {year: 2000, month: 0, day: 1},
        output: false,
      },
    ]

    testCases.forEach(c => {
      assert.equal(
        isValidDateRange(c.start, c.end),
        c.output,
        `for input range ${c.start.year}-${c.start.month}-${c.start.day} - ${c
          .end.year}-${c.end.month}-${c.end.day}`
      )
    })
  })
})
