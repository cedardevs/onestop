import React from 'react'
import {mount} from 'enzyme'

import App from '../../src/App'

import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'
import TopicsMenu from '../../src/landing/TopicsMenu'
import FeaturedDatasets from '../../src/landing/FeaturedDatasets'
import Banner from '../../src/root/banner/Banner'
import Header from '../../src/root/Header'
import Footer from '../../src/root/Footer'
import FooterLink from '../../src/root/FooterLink'
import SearchFields from '../../src/search/SearchFields'
import Landing from '../../src/landing/Landing'
import Filters from '../../src/filter/Filters'
import Collections from '../../src/result/collections/Collections'
import Detail from '../../src/detail/Detail'
import GranuleList from '../../src/result/granules/GranuleList' // create history object based on environment

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

describe('The home page', () => {
  const url = '/'
  let component = null

  beforeAll(() => {
    history.push(url)
    component = mount(App(store, history))
  })

  it('should render banner if banner message exists', () => {
    const banner = component.find(Banner)
    expect(banner.length).toBe(1)
    const props = banner.props()
    if (!props.message) {
      expect(banner.isEmptyRender()).toBeTruthy()
    }
    else {
      expect(banner.isEmptyRender()).not.toBeTruthy()
    }
  })

  it('should have Header', () => {
    expect(component.find(Header).length).toBe(1)
  })

  it('should have Landing', () => {
    expect(component.find(Landing).length).toBe(1)
  })

  it('should have SearchFields under Landing', () => {
    const searchFields = component.find(SearchFields)
    expect(searchFields.length).toBe(1)
    expect(searchFields.closest(Landing).length).toBe(1)
  })

  it('should have TopicsMenu under Landing', () => {
    const topicsMenu = component.find(TopicsMenu)
    expect(topicsMenu.length).toBe(1)
    expect(topicsMenu.closest(Landing).length).toBe(1)
  })

  it('should have FeaturedDatasets under Landing', () => {
    const featuredDatasets = component.find(FeaturedDatasets)
    expect(featuredDatasets.length).toBe(1)
    expect(featuredDatasets.closest(Landing).length).toBe(1)
  })

  it('should have Footer w/expected links', () => {
    const footer = component.find(Footer)
    expect(footer.length).toBe(1)
    const footerLinks = footer.find(FooterLink)
    expect(footerLinks.length).toBe(11)

    const footerHrefs = footerLinks.map(link => {
      return link.prop('href')
    })

    const expectedHrefs = [
      '//www.ncei.noaa.gov/privacy',
      '//www.noaa.gov/foia-freedom-of-information-act',
      '//www.cio.noaa.gov/services_programs/info_quality.html',
      '//www.noaa.gov/disclaimer.html',
      '//www.ncdc.noaa.gov/survey',
      'mailto:noaa.data.catalog@noaa.gov?Subject=NOAA%20OneStop%20Feedback',
      '//www.commerce.gov/',
      '//www.noaa.gov/',
      '//www.nesdis.noaa.gov/',
      'https://github.com/cedardevs/onestop/releases',
      undefined, // anchored link for image attribution (internal)
    ]

    expect(footerHrefs.sort()).toEqual(
      expect.arrayContaining(expectedHrefs.sort())
    )
  })

  it('should NOT have Filters', () => {
    expect(component.find(Filters).length).toBe(0)
  })

  it('should NOT have Collections', () => {
    expect(component.find(Collections).length).toBe(0)
  })

  it('should NOT have Detail', () => {
    expect(component.find(Detail).length).toBe(0)
  })

  it('should NOT have GranuleList', () => {
    expect(component.find(GranuleList).length).toBe(0)
  })
})

describe('The collections page', () => {
  const query = '?=alaska'
  const url = `/collections${query}`
  let component = null

  let locationBefore = store.getState().router.location
  let locationAfter = null

  const listener = x => {
    locationAfter = store.getState().router.location
  }

  beforeAll(() => {
    store.subscribe(listener)
    history.push(url)
    component = mount(App(store, history))
  })

  it('should have a different router location after url pushed to history', () => {
    expect(locationBefore.pathname).not.toBe(locationAfter.pathname)
  })

  it('should have a router search equal to the url query', () => {
    expect(locationAfter.search).toBe(query)
  })

  it('should have Filters', () => {
    expect(component.find(Filters).length).toBe(1)
  })

  it('should have Collections', () => {
    expect(component.find(Collections).length).toBe(1)
  })

  it('should NOT have Detail', () => {
    expect(component.find(Detail).length).toBe(0)
  })

  it('should NOT have GranuleList', () => {
    expect(component.find(GranuleList).length).toBe(0)
  })
})
