import '../specHelper'
import * as keywordUtils from '../../src/utils/keywordUtils'

describe('The keyword utils', function () {
  describe('title case util', function () {
    it('reduces screaming', function () {
      const testCases = [
        {
          input: 'TRMM Microwave Imager',
          output: 'TRMM Microwave Imager'
        },
        {
          input: 'TRMM > TRMM Microwave Imager',
          output: 'TRMM Microwave Imager'
        },
        {
          input: 'THERMOMETER',
          output: 'Thermometer'
        },
        {
          input: 'Thermistor',
          output: 'Thermistor'
        },
        {
          input: 'TEMPERATURE > Thermistor',
          output: 'Thermistor'
        },
        {
          input: 'CRYOSPHERE > SNOW/ICE',
          output: 'Snow/Ice'
        },
        {
          input: 'Ocean Carbon > Biogeochemistry; Southern Ocean Iron Experiment (OCB-SOFeX)',
          output: 'Biogeochemistry; Southern Ocean Iron Experiment (OCB-SOFeX)'
        }
      ]

      testCases.forEach((testCase) => {
        const {input, output} = testCase
        keywordUtils.titleCaseKeyword(input).should.equal(output, `Failed for input: ${input}`)
      })
    })
  })
})
