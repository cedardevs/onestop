import Immutable from 'seamless-immutable'
import { results, initialState } from '../../../src/reducers/domain/results'
import { completeSearch, clearCollections, fetchedGranules, clearGranules, FACETS_RECEIVED } from '../../../src/actions/SearchRequestActions'

describe('The results reducer', function () {

  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = results(initialState, initialAction)

    expect(result.collections).toBeInstanceOf(Object)
    expect(result.granules).toBeInstanceOf(Object)
    expect(result.facets).toBeInstanceOf(Object)
  })

  it('merges received collections into the map of collections', function () {
    const firstSetCollections = new Map()
    firstSetCollections.set('A', {id: 1})
    const expectedFirstMap = {A: {id: 1}}
    const firstRoundResult = results(initialState, completeSearch(firstSetCollections))
    expect(firstRoundResult.collections).toEqual(expectedFirstMap)

    const secondSetCollections = new Map()
    secondSetCollections.set('B', {id: 2})
    secondSetCollections.set('C', {id: 3})
    const expectedSecondMap = {A: {id: 1}, B: {id: 2}, C: {id: 3}}
    const secondRoundResult = results(firstRoundResult, completeSearch(secondSetCollections))
    expect(secondRoundResult.collections).toEqual(expectedSecondMap)
  })

  it('can clear existing collection state', function () {
    const stateWithCollections = Immutable({
      collections: {A: {id: 123}},
      totalCollections: 1,
      collectionsPageOffset: 20
    })
    const result = results(stateWithCollections, clearCollections())
    expect(result.collections).toEqual({})
    expect(result.totalCollections).toBe(0)
    expect(result.collectionsPageOffset).toBe(0)
  })

  it('merges received granules into the map of granules', function () {
    const firstRoundData = [{id: 'A', attributes: {version: 1}}, {id: 'B', attributes: {version: 1}}]
    const firstRoundMap = {A: {version: 1}, B: {version: 1}}
    const firstRoundResult = results(initialState, fetchedGranules(firstRoundData))
    expect(firstRoundResult.granules).toEqual(firstRoundMap)

    const secondRoundData = [{id: 'B', attributes: {version: 2}}, {id: 'C', attributes: {version: 1}}]
    const secondRoundMap = {A: {version: 1}, B: {version: 2}, C: {version: 1}}
    const secondRoundResult = results(firstRoundResult, fetchedGranules(secondRoundData))
    expect(secondRoundResult.granules).toEqual(secondRoundMap)
  })

  it('can clear existing granule state', function () {
    const stateWithGranules = Immutable({
      granules: {A: {id: 'A'}},
      totalGranules: 1,
      granulesPageOffset: 20
    })
    const result = results(stateWithGranules, clearGranules())
    expect(result.granules).toEqual({})
    expect(result.totalGranules).toBe(0)
    expect(result.granulesPageOffset).toBe(0)
  })

  it('should handle FACETS_RECEIVED', () => {
    const facetsRecAction = {
      type:"FACETS_RECEIVED",
      metadata:{
        facets:{
          science:{
            "Oceans":{
              count:5
            },
            "Oceans > Ocean Temperature":{
              count:5
            },
            "Oceans > Ocean Temperature > Sea Surface Temperature":{
              count:5
            },
            dataResolution:{}
          }
        }
      },
      procSelectedFacets:true
    }

    let expectedState = {
      collectionDetail: null,
      collections: {},
      granules: {},
      facets: {
        science: {
          "Oceans": {
            count: 5
          },
          "Oceans > Ocean Temperature": {
            count: 5
          },
          "Oceans > Ocean Temperature > Sea Surface Temperature": {
            count: 5
          },
          dataResolution: {}
        }
      },
      totalCollections: 0,
      totalGranules: 0,
      collectionsPageOffset: 0,
      granulesPageOffset: 0,
      pageSize: 20
    }
    let stateWithFacets = results(initialState, facetsRecAction)
    expect(stateWithFacets).toEqual(expectedState)
  })

})
