import '../specHelper'
import * as actions from '../../src/search/temporal/TemporalActions'

describe('The datetime action', function () {

  const datetime = '2016-07-25T15:45:00-06:00'

  it('sets start date time ', function () {
    const temporalAction = actions.updateDateRange(datetime, '');
    const expectedAction =  { type: 'UPDATE_DATE_RANGE',
      startDate: '2016-07-25T15:45:00-06:00', endDate: ''}

    temporalAction.should.deep.equal(expectedAction)
  })

  it('sets end date time ', function () {
    const temporalAction = actions.updateDateRange('', datetime);
    const expectedAction =  { type: 'UPDATE_DATE_RANGE',
      startDate: '', endDate: '2016-07-25T15:45:00-06:00' }

    temporalAction.should.deep.equal(expectedAction)
  })
})
