import '../specHelper'
import {textToNumber} from '../../src/utils/inputUtils'
import {expect} from 'chai'

describe('The inputUtils', function() {

  it('converts text to numbers', function () {
    const testCases = [
      {input: 1, output: 1},
      {input: 1.2, output: 1.2},
      {input: '1', output: 1},
      {input: '1.2', output: 1.2},
      {input: '', output: null},
      {input: 'a', output: null},
    ]

    testCases.forEach((testCase) => {
      // using expect here for when result is null (can't call should on null)
      expect(textToNumber(testCase.input)).to.equal(testCase.output)
    })
  })

})
