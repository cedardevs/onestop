import React, {Component} from 'react'
import {Route, Switch} from 'react-router'

import Container from '../layout/Container'
import Background from '../layout/Background'

import BannerContainer from './banner/BannerContainer'
import HeaderContainer from './HeaderContainer'
import MapContainer from '../search/map/MapContainer'

import FiltersContainer from '../filter/FiltersContainer'
import FiltersHiddenContainer from '../filter/FiltersHiddenContainer'

import Result from '../result/Result'
import CollectionsContainer from '../result/collections/CollectionsContainer'
import GranuleListContainer from '../result/granules/GranuleListContainer'
import ErrorContainer from '../error/ErrorContainer'
import LandingContainer from '../landing/LandingContainer'
import DetailContainer from '../detail/DetailContainer'
import Help from '../common/info/Help'

import AboutContainer from '../common/info/AboutContainer'
import CartContainer from '../cart/CartContainer'

import LoadingBarContainer from '../loading/LoadingBarContainer'

import FooterContainer from './FooterContainer'

import {SiteColors} from '../common/defaultStyles'
import {FEATURE_CART} from '../utils/featureUtils'
import {ROUTE} from '../utils/urlUtils'
import NotFoundContainer from '../common/info/NotFoundContainer'

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
    const {
      showLeft,
      leftOpen,
      showRight,
      featuresEnabled,
      authEnabled,
    } = this.props

    const header = (
      <div>
        <BannerContainer />
        <Route path="/">
          <HeaderContainer />
        </Route>
      </div>
    )

    const left = leftOpen ? <FiltersContainer /> : <FiltersHiddenContainer />
    const leftWidth = leftOpen ? '20em' : '2em'
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
            {/*TODO: replace this with ArcGIS map?*/}
            <MapContainer selection={true} features={false} />
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
                <Result>
                  <CollectionsContainer />
                </Result>
              </Route>
              <Route path={ROUTE.granules.parameterized}>
                {/*TODO parameterize this path!*/}
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
        <Background>
          <Container
            header={header}
            left={left}
            leftWidth={leftWidth}
            leftVisible={leftOpen}
            middle={middle}
            right={null}
            rightWidth={256}
            rightVisible={showRight}
            footer={<FooterContainer />}
          />
        </Background>
      )
    }
  }
}
