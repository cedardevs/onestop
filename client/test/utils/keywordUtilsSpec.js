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

  describe('hierarchy map util', function() {

    it('can build a hierarchy map with science theme data', function() {
      const input = {
        'science': {
          'Atmosphere': { count: 3 },
          'Atmosphere > Air': { count: 2 },
          'Atmosphere > Air > Particles': { count: 2 },
          'Atmosphere > Wind': { count: 1 },
          'Land Surface': { count: 2 },
          'Land Surface > Topography': { count: 2 },
          'Oceans': { count: 1 }
        }
      }

      const expected = {
        'Data Theme': {
          'Atmosphere': {
            count: 3,
            children: {
              'Air': {
                count: 2,
                children: {
                  'Particles': {
                    count: 2,
                    children: {},
                    category: 'science',
                    term: 'Atmosphere > Air > Particles'
                  }
                },
                category: 'science',
                term: 'Atmosphere > Air'
              },
              'Wind': {
                count: 1,
                children: {},
                category: 'science',
                term: 'Atmosphere > Wind'
              }
            },
            category: 'science',
            term: 'Atmosphere'
          },
          'Land Surface': {
            count: 2,
            children: {
              'Topography': {
                count: 2,
                children: {},
                category: 'science',
                term: 'Land Surface > Topography'
              }
            },
            category: 'science',
            term: 'Land Surface'
          },
          'Oceans': {
            count: 1,
            children: {},
            category: 'science',
            term: 'Oceans'
          }
        }
      }

      keywordUtils.buildKeywordHierarchyMap(input).should.deep.equal(expected)
    })

    it('will not fail upon encountering bad science theme data', function() {
      const input = {
        'science': {
          "Spectral/Engineering": { count: 10 },
          "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave": { count: 1 },
          "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave > Brightness Temperature": { count: 1 },
          "Spectral/Engineering > Microwave": { count: 6 },
          "Spectral/Engineering > Microwave > Antenna Temperature": { count: 1 },
          "Spectral/Engineering > Microwave > Brightness Temperature": { count: 6 }
        }
      }

      const expected = {
        'Data Theme': {
          "Spectral/Engineering": {
            count: 10,
            children: {
              "microwave": {
                count: 1,
                children: {
                  "Brightness Temperature": {
                    count: 1,
                    children: {},
                    category: 'science',
                    term: "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave > Brightness Temperature"
                  }
                },
                category: 'science',
                term: "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
              },
              "Microwave": {
                count: 6,
                children: {
                  "Antenna Temperature": {
                    count: 1,
                    children: {},
                    category: 'science',
                    term: "Spectral/Engineering > Microwave > Antenna Temperature"
                  },
                  "Brightness Temperature": {
                    count: 6,
                    children: {},
                    category: 'science',
                    term: "Spectral/Engineering > Microwave > Brightness Temperature"
                  }
                },
                category: 'science',
                term: "Spectral/Engineering > Microwave"
              }
            },
            category: 'science',
            term: "Spectral/Engineering"
          }
        }
      }

      keywordUtils.buildKeywordHierarchyMap(input).should.deep.equal(expected)
    })

    it('will handle non-science category data', function() {
      const input = {
        'totesDifferentCategory': {
          'Short Name > Long Name': { count: 10 },
          'TLA > Three Letter Acronym': { count: 5 }
        }
      }

      const expected = {
        'Totes Different Category': {
          'Short Name > Long Name': {
            count: 10,
            children: {},
            category: 'totesDifferentCategory',
            term: 'Short Name > Long Name'
          },
          'TLA > Three Letter Acronym': {
            count: 5,
            children: {},
            category: 'totesDifferentCategory',
            term: 'TLA > Three Letter Acronym'
          }
        }
      }

      keywordUtils.buildKeywordHierarchyMap(input).should.deep.equal(expected)
    })

  })

})
