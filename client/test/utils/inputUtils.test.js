import {
  isValidDate,
  isValidDateRange,
  textToNumber,
  ymdToDateMap,
  convertYearToCE,
  isValidYear,
  isValidYearRange,
} from '../../src/utils/inputUtils'

import moment from 'moment/moment'

describe('The inputUtils', function(){
  describe('converts geologic year to CE', function(){
    const testCases = [
      {format: 'CE', input: '-1000000', output: '-1000000'}, // NO-OP
      {format: 'BP', input: '1001950', output: '-1000000'},
      {format: 'BP', input: '0', output: '1950'},
      {format: 'BP', input: '2019', output: '-69'},
      {format: 'CE', input: '1ka', output: '1000'},
      {format: 'BP', input: '1ka', output: '950'},
      {format: 'CE', input: '-3.5 ka', output: '-3500'},
      {format: 'CE', input: '2 Ma', output: '2000000'},
      {format: 'CE', input: '2ma', output: '2000000'}, // case insensitive
      {format: 'CE', input: '2KA', output: '2000'}, // case insensitive
      {format: 'CE', input: '2 gA', output: '2000000000'}, // case insensitive
      {format: 'CE', input: '-7.6 Ga', output: '-7600000000'},
      {format: 'CE', input: '', output: ''},
      {format: 'BP', input: null, output: null},
      {format: 'CE', input: 'foo', output: 'foo'}, // doesn't mutate non-numbers
      {format: 'CE', input: 34234, output: 34234}, // BAD DO NOT DO THIS
      {format: 'invalid', input: '1234', output: '1234'},
      {format: 'CE', input: 'nonsense ka', output: 'nonsense ka'},
    ]

    testCases.forEach(c => {
      it(`for input ${c.input} with format ${c.format}`, function(){
        expect(convertYearToCE(c.input, c.format)).toBe(c.output)
      })
    })
  })

  describe('validates geologic years', function(){
    const testCases = [
      {input: '', output: true},
      {input: null, output: true},
      {input: 20000, output: true}, /// TODO BAD (integer input in general is not good here)
      {input: 'notanumber', output: false},
      {input: '200000', output: false}, // in the future
      {input: '-3000000000', output: true},
      {input: '2019', output: true},
      {input: '1ka', output: false}, // doesn't allow unconverted years
    ]

    testCases.forEach(c => {
      it(`for input ${c.input}`, function(){
        expect(isValidYear(c.input)).toBe(c.output) // TODO rename to isValidGeologicYear?
      })
    })
  })

  describe('can check geologic year range validity', function(){
    const testCases = [
      {start: '-1000', end: '0', output: true},
      {start: '-10000000', end: '', output: true},
      {start: '', end: '-3000000000', output: true},
      {start: '0', end: '-1000', output: false},
      {start: '-1 ka', end: '1ka', output: false}, // doesn't allow unconverted years
      {start: 0, end: -1000, output: true}, // TODO bad, should not allow numeric input
    ]

    testCases.forEach(c => {
      it(`for input range ${c.start} - ${c.end}`, function(){
        expect(isValidYearRange(c.start, c.end)).toBe(c.output)
      })
    })
  })

  describe('converts text to numbers', function(){
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
      it(`for input '${c.input}'`, function(){
        expect(textToNumber(c.input)).toBe(c.output)
      })
    })
  })

  describe('can builds date maps', function(){
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
      it(`for input date ${c.year}-${c.month}-${c.day}`, function(){
        expect(ymdToDateMap(c.year, c.month, c.day)).toEqual(c.output)
      })
    })
  })

  describe('can check date validity', function(){
    const testCases = [
      {
        year: '',
        month: '',
        day: '',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '',
        day: '',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '4',
        day: '',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '4',
        day: '24',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '0',
        day: '1',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '',
        day: '1',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: true},
          day: {field: '', required: false},
        },
      },
      {
        year: 'notayear',
        month: '',
        day: '',
        output: {
          year: {field: 'invalid', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '1.23',
        month: '',
        day: '',
        output: {
          year: {field: 'invalid', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '-1',
        month: '-2',
        day: '-3',
        output: {
          year: {field: 'must be greater than zero', required: false},
          month: {field: 'cannot be in the future', required: false},
          day: {field: 'invalid', required: false},
        },
      }, // cannot be in the future is a weird error there
      {
        year: '2000',
        month: 'notamonth',
        day: '',
        output: {
          year: {field: '', required: false},
          month: {field: 'invalid', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '1.23',
        day: '',
        output: {
          year: {field: '', required: false},
          month: {field: 'invalid', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '0',
        day: 'notaday',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: 'invalid', required: false},
        },
      },
      {
        year: '2000',
        month: '0',
        day: '1.23',
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: 'invalid', required: false},
        },
      },
      {
        year: '200000000',
        month: '',
        day: '',
        output: {
          year: {field: 'cannot be in the future', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '',
        month: '4',
        day: '',
        output: {
          year: {field: '', required: true},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '',
        month: '',
        day: '1',
        output: {
          year: {field: '', required: true},
          month: {field: '', required: true},
          day: {field: '', required: false},
        },
      },
      {
        year: '2001',
        month: '',
        day: '',
        now: moment(ymdToDateMap('2000', '5', '5')),
        output: {
          year: {field: 'cannot be in the future', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2001',
        month: '6',
        day: '6',
        now: moment(ymdToDateMap('2000', '5', '5')),
        output: {
          year: {field: 'cannot be in the future', required: false},
          month: {field: '', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '6',
        day: '',
        now: moment(ymdToDateMap('2000', '5', '5')),
        output: {
          year: {field: '', required: false},
          month: {field: 'cannot be in the future', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '6',
        day: '1',
        now: moment(ymdToDateMap('2000', '5', '5')),
        output: {
          year: {field: '', required: false},
          month: {field: 'cannot be in the future', required: false},
          day: {field: '', required: false},
        },
      },
      {
        year: '2000',
        month: '5',
        day: '6',
        now: moment(ymdToDateMap('2000', '5', '5')),
        output: {
          year: {field: '', required: false},
          month: {field: '', required: false},
          day: {field: 'cannot be in the future', required: false},
        },
      },
    ]

    testCases.forEach(c => {
      it(`for input date ${c.year}-${c.month}-${c.day}`, function(){
        expect(isValidDate(c.year, c.month, c.day, c.now)).toEqual(c.output)
      })
    })
  })

  describe('can check date range validity', function(){
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
      it(`for input range ${c.start.year}-${c.start.month}-${c.start.day} - ${c
        .end.year}-${c.end.month}-${c.end.day}`, function(){
        expect(isValidDateRange(c.start, c.end)).toBe(c.output) //,
      })
    })
  })
})
