import '../specHelper'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import reducer from '../../src/reducers/reducer'
import * as actions from '../../src/actions/SearchParamActions'

describe('The search params actions', function () {
  describe('for geometries', function () {

    const geoJSON = {geometry: 'test object'}

    it('set geoJSON', function () {
      const mapAction = actions.newGeometry(geoJSON)
      const expectedAction = { type: 'new_geometry', geoJSON: {geometry: 'test object'} }

      mapAction.should.deep.equal(expectedAction)
    })
  })

  describe('for datetimes', function () {

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

  describe('for facets', function () {
    const middlewares = [ thunk ]
    const mockStore = configureMockStore(middlewares)
    const initialState = reducer(undefined, {})

    it('adds facet to facets selected', function () {
      const facets = {name: "a", value: "a", selected: true}
      const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {a: ['a']}}

      const store = mockStore(initialState)
      store.dispatch(actions.toggleFacet(facets.name, facets.value, facets.selected))
      store.getActions()[0].should.deep.equal(expectedActions)
    })

    it('removes facet from facets selected', function () {
      const toggleOnAction = { type: 'TOGGLE_FACET', selectedFacets: {a: ['a']}}
      const state = reducer(initialState, toggleOnAction)
      const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {}}
      const store = mockStore(state)

      store.dispatch(actions.toggleFacet('a', 'a', false))
      store.getActions()[0].should.deep.equal(expectedActions)
    })
  })
})
