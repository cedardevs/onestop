import '../specHelper'
import * as actions from '../../src/search/temporal/TemporalActions'

describe('The datetime action', function () {

  const datetime = '2016-07-25T15:45:00-06:00'

  it('set start date time ', function () {
    const temporalAction = actions.startDate(datetime);
    const expectedAction =  { type: 'START_DATE', datetime: '2016-07-25T15:45:00-06:00'}

    temporalAction.should.deep.equal(expectedAction)
  })

  it('set end date time ', function () {
    const temporalAction = actions.endDate(datetime);
    const expectedAction =  { type: 'END_DATE', datetime: '2016-07-25T15:45:00-06:00'}

    temporalAction.should.deep.equal(expectedAction)
  })
})

