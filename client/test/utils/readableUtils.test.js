import {abbreviateNumber, displayBigYears} from '../../src/utils/readableUtils'

describe('The readableUtils displayBigYears function', function(){
  const testCases = [
    {input: 0, output: '0'},
    {input: 1999, output: '1999'},
    {input: 9999, output: '9999'},
    {input: 10000, output: '10,000'},
    {input: -2000, output: '-2000'},
    {input: -55998050, output: '-55,998,050'},
    {input: -7800000000, output: '-7,800,000,000'},
  ]

  testCases.forEach(c => {
    it(`formats ${c.input} as ${c.output}`, function(){
      expect(displayBigYears(c.input)).toBe(c.output)
    })
  })
})

describe('The readableUtils abbreviateNumber function', function(){
  describe('converts numbers to the appropriate abbreviated strings with 1 decimal place', function(){
    const decPlace = 1

    const testCases = [
      {input: 0, output: '0'},
      {input: 1, output: '1'},
      {input: 2, output: '2'},
      {input: 4, output: '4'},
      {input: 8, output: '8'},
      {input: 16, output: '16'},
      {input: 32, output: '32'},
      {input: 64, output: '64'},
      {input: 128, output: '128'},
      {input: 256, output: '256'},
      {input: 512, output: '512'},
      {input: 1024, output: '1k'},
      {input: 2048, output: '2k'},
      {input: 4096, output: '4.1k'},
      {input: 8192, output: '8.2k'},
      {input: 16384, output: '16.4k'},
      {input: 32768, output: '32.8k'},
      {input: 65536, output: '65.5k'},
      {input: 131072, output: '131.1k'},
      {input: 262144, output: '262.1k'},
      {input: 524288, output: '524.3k'},
      {input: 1048576, output: '1m'},
      {input: 2097152, output: '2.1m'},
      {input: 4194304, output: '4.2m'},
      {input: 8388608, output: '8.4m'},
      {input: 16777216, output: '16.8m'},
      {input: 33554432, output: '33.6m'},
      {input: 67108864, output: '67.1m'},
      {input: 134217728, output: '134.2m'},
      {input: 268435456, output: '268.4m'},
      {input: 536870912, output: '536.9m'},
      {input: 1073741824, output: '1.1b'},
      {input: 2147483648, output: '2.1b'},
    ]

    testCases.forEach(c => {
      it(`formats ${c.input} as ${c.output}`, function(){
        expect(abbreviateNumber(c.input, decPlace)).toBe(c.output)
      })
    })
  })
})
