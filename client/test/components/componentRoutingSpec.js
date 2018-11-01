import '../specHelper'

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

  before(() => {
    history.push(url)
    component = mount(App(store, history))
  })

  it('should render banner if banner message exists', () => {
    const banner = component.find(Banner)
    banner.length.should.equal(1)
    const props = banner.props()
    if (!props.message) {
      banner.isEmptyRender().should.equal(true)
    }
    else {
      banner.isEmptyRender().should.equal(false)
    }
  })

  it('should have Header', () => {
    component.find(Header).length.should.equal(1)
  })

  it('should have Landing', () => {
    component.find(Landing).length.should.equal(1)
  })

  it('should have SearchFields under Landing', () => {
    const searchFields = component.find(SearchFields)
    searchFields.length.should.equal(1)
    searchFields.closest(Landing).length.should.equal(1)
  })

  it('should have TopicsMenu under Landing', () => {
    const topicsMenu = component.find(TopicsMenu)
    topicsMenu.length.should.equal(1)
    topicsMenu.closest(Landing).length.should.equal(1)
  })

  it('should have FeaturedDatasets under Landing', () => {
    const featuredDatasets = component.find(FeaturedDatasets)
    featuredDatasets.length.should.equal(1)
    featuredDatasets.closest(Landing).length.should.equal(1)
  })

  it('should have Footer w/expected links', () => {
    const footer = component.find(Footer)
    footer.length.should.equal(1)
    const footerLinks = footer.find(FooterLink)
    footerLinks.length.should.equal(11)

    const footerHrefs = footerLinks.map(link => {
      return link.prop('href')
    })

    const expectedHrefs = [
      '//www.ncdc.noaa.gov/about-ncdc/privacy',
      'http://www.noaa.gov/foia-freedom-of-information-act',
      'http://www.cio.noaa.gov/services_programs/info_quality.html',
      'http://www.noaa.gov/disclaimer.html',
      '//www.ncdc.noaa.gov/survey',
      'mailto:noaa.data.catalog@noaa.gov?Subject=NOAA%20OneStop%20Feedback',
      '//www.commerce.gov/',
      'http://www.noaa.gov/',
      '//www.nesdis.noaa.gov/',
      'https://github.com/cedardevs/onestop/releases',
      undefined,
    ]

    footerHrefs.should.have.same.members(expectedHrefs)
  })

  it('should NOT have Filters', () => {
    component.find(Filters).length.should.equal(0)
  })

  it('should NOT have Collections', () => {
    component.find(Collections).length.should.equal(0)
  })

  it('should NOT have Detail', () => {
    component.find(Detail).length.should.equal(0)
  })

  it('should NOT have GranuleList', () => {
    component.find(GranuleList).length.should.equal(0)
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

  before(() => {
    store.subscribe(listener)
    history.push(url)
    component = mount(App(store, history))
  })

  it('should have a different router location after url pushed to history', () => {
    locationBefore.pathname.should.not.equal(locationAfter.pathname)
  })

  it('should have a router search equal to the url query', () => {
    locationAfter.search.should.equal(query)
  })

  it('should have Filters', () => {
    component.find(Filters).length.should.equal(1)
  })

  it('should have Collections', () => {
    component.find(Collections).length.should.equal(1)
  })

  it('should NOT have Detail', () => {
    component.find(Detail).length.should.equal(0)
  })

  it('should NOT have GranuleList', () => {
    component.find(GranuleList).length.should.equal(0)
  })
})
