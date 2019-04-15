import React, {Component} from 'react'
import {Route, Switch} from 'react-router'

import Container from '../layout/Container'

import DisclaimerContainer from '../disclaimer/DisclaimerContainer'
import HeaderContainer from '../header/HeaderContainer'
import CollectionMapContainer from '../collections/filters/CollectionMapContainer'

import CollectionFiltersContainer from '../collections/filters/CollectionFiltersContainer'
import FiltersHiddenContainer from '../common/filters/FiltersHiddenContainer'

import GranuleFiltersContainer from '../granules/filters/GranuleFiltersContainer'

import CollectionResult from '../collections/results/CollectionResult'
import CollectionsContainer from '../collections/results/CollectionsContainer'
import GranuleListContainer from '../granules/results/GranuleListContainer'
import ErrorContainer from '../error/ErrorContainer'
import LandingContainer from '../landing/LandingContainer'
import DetailContainer from '../collections/detail/DetailContainer'
import Help from '../help/Help'

import AboutContainer from '../about/AboutContainer'
import CartContainer from '../cart/CartContainer'

import LoadingBarContainer from '../loading/LoadingBarContainer'

import FooterContainer from '../footer/FooterContainer'

import {SiteColors} from '../../style/defaultStyles'
import {FEATURE_CART} from '../../utils/featureUtils'
import {ROUTE} from '../../utils/urlUtils'
import NotFoundContainer from '../404/NotFoundContainer'

import earth from '../../../img/Earth.jpg'

const styleBrowserWarning = {
  background: SiteColors.WARNING,
  textAlign: 'center',
  padding: '0.618em',
  fontSize: '1.2em',
}

const styleBrowserWarningLink = {
  color: 'rgb(169, 226, 255)',
}

const styleBrowserWarningParagraph = {
  textAlign: 'center',
}

const styleClose = {}

// component
export default class Root extends Component {
  constructor(props) {
    super(props)
    this.hasUnsupportedFeatures = this.hasUnsupportedFeatures.bind(this)
    this.state = {
      leftVisible: true,
      rightVisible: false,
      browserWarning: this.hasUnsupportedFeatures(),
    }
  }

  hasUnsupportedFeatures() {
    let browserWarning = false
    const flexSupport =
      document.body.style.flex !== undefined &&
      document.body.style.flexFlow !== undefined
    if (!flexSupport) {
      browserWarning = true
    }
    return browserWarning
  }

  unsupportedBrowserWarning() {
    const wikiUrl =
      'https://github.com/cedardevs/onestop/wiki/OneStop-Client-Supported-Browsers'
    return (
      <aside role="alert" style={styleBrowserWarning}>
        <p style={styleBrowserWarningParagraph}>
          The browser that you are using to view this page is not currently
          supported. For a list of currently supported & tested browsers, please
          visit the
          <span>
            {' '}
            <a style={styleBrowserWarningLink} href={wikiUrl}>
              OneStop Documentation
            </a>
          </span>.
        </p>
      </aside>
    )
  }

  render() {
    const {match, leftOpen, rightOpen, featuresEnabled} = this.props

    console.log('ROUTER match=', match)

    const header = (
      <div>
        <DisclaimerContainer />
        <Route path="/">
          <HeaderContainer />
        </Route>
      </div>
    )

    const bannerVisible = match.path === '/' && match.isExact

    // TODO: cleanup visible vs open behavior a bit (plus make right/left layout consistent)
    const leftWidth = leftOpen ? '20em' : '2em'
    const collectionFilter = leftOpen ? (
      <CollectionFiltersContainer />
    ) : (
      <FiltersHiddenContainer />
    )
    const granuleFilter = leftOpen ? (
      <GranuleFiltersContainer />
    ) : (
      <FiltersHiddenContainer />
    )

    const left = (
      <Switch>
        <Route path={ROUTE.search.path} exact>
          {collectionFilter}
        </Route>
        <Route path={ROUTE.granules.parameterized}>{granuleFilter}</Route>
      </Switch>
    )
    const leftVisible = match.path === ROUTE.search.path && match.isExact

    const right = null
    const rightVisible = false

    const cart = featuresEnabled.includes(FEATURE_CART) ? (
      <Route path={ROUTE.cart.path}>
        <CartContainer />
      </Route>
    ) : null

    const middle = (
      <div style={{width: '100%'}}>
        <Switch>
          <Route path="/" exact />
          <Route path="/">
            <LoadingBarContainer />
          </Route>
        </Switch>
        <Switch>
          <Route path={ROUTE.search.path} exact>
            <CollectionMapContainer selection={true} features={false} />
          </Route>
        </Switch>

        <Switch>
          {/*Each page inside this switch should have a Meta!*/}
          <Route path="/" exact>
            <LandingContainer />
          </Route>

          <Route path={ROUTE.details.path}>
            {/*TODO parameterize this path!*/}
            <DetailContainer />
          </Route>

          <Route path={ROUTE.search.path}>
            <Switch>
              <Route path={ROUTE.search.path} exact>
                <CollectionResult>
                  <CollectionsContainer />
                </CollectionResult>
              </Route>
              <Route path={ROUTE.granules.parameterized}>
                <GranuleListContainer />
              </Route>
            </Switch>
          </Route>

          <Route path={ROUTE.about.path}>
            <AboutContainer />
          </Route>

          <Route path={ROUTE.help.path}>
            <Help />
          </Route>

          {cart}

          <Route path={ROUTE.error.path}>
            <ErrorContainer />
          </Route>

          {/* 404 not found */}
          <Route component={NotFoundContainer} />
        </Switch>
      </div>
    )

    if (this.state.browserWarning) {
      return this.unsupportedBrowserWarning()
    }
    else {
      return (
        <Container
          header={header}
          bannerGraphic={earth}
          bannerHeight={'30em'}
          bannerArcHeight={'15em'}
          bannerVisible={bannerVisible}
          left={left}
          leftWidth={leftWidth}
          leftOpen={leftOpen}
          leftVisible={leftVisible}
          middle={middle}
          right={right}
          rightWidth={256}
          rightOpen={rightOpen}
          rightVisible={rightVisible}
          footer={<FooterContainer />}
        />
      )
    }
  }
}
