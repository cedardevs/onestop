import React, {useState} from 'react'
import {Route, Switch} from 'react-router'

import Layout from '../layout/Layout'
import DrawerContent from '../filters/spatial/DrawerContent'

import DisclaimerContainer from '../disclaimer/DisclaimerContainer'
import HeaderContainer from '../header/HeaderContainer'
import FiltersContainer from '../filters/FiltersContainer'
import FiltersHiddenContainer from '../filters/FiltersHiddenContainer'
import ResultsContainer from '../results/ResultsContainer'
import ErrorContainer from '../error/ErrorContainer'
import LandingContainer from '../landing/LandingContainer'
import DetailContainer from '../collections/detail/DetailContainer'
import Help from '../help/Help'

import AboutContainer from '../about/AboutContainer'
import CartContainer from '../cart/CartContainer'

import CollectionGetDetailLoadingContainer from '../loading/CollectionGetDetailLoadingContainer'
import GranuleSearchLoadingContainer from '../loading/GranuleSearchLoadingContainer'
import CollectionSearchLoadingContainer from '../loading/CollectionSearchLoadingContainer'

import FooterContainer from '../footer/FooterContainer'

import {SiteColors} from '../../style/defaultStyles'
import {
  isGranuleListPage,
  isHome,
  isSearch,
  ROUTE,
  validHomePaths,
} from '../../utils/urlUtils'
import NotFoundContainer from '../404/NotFoundContainer'

import earth from '../../../img/Earth.jpg'
import {isBrowserUnsupported} from '../../utils/browserUtils'
import FocusProxy, {FocusTarget, useFocusProxy} from '../common/ui/FocusProxy'

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

const BrowserUnsupportedWarning = () => {
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

const Root = props => {
  const {location, leftOpen, rightOpen, showMap} = props

  const {
    proxyRef,
    setProxyFocusing,
    targetRef,
    setTargetBlurring,
  } = useFocusProxy(true, false, false)

  // store browser support in component state to prevent checking every render
  const [ browserUnsupported, _ ] = useState(isBrowserUnsupported())
  if (browserUnsupported) {
    return <BrowserUnsupportedWarning />
  }

  const bannerVisible = isHome(location.pathname)
  const leftVisible = isSearch(location.pathname)
  const onGranuleListPage = isGranuleListPage(location.pathname)
  const rightVisible = false
  const drawerOpen = showMap

  const hiddenAccessibilityHeading = (
    <Switch>
      <Route path={ROUTE.collections.path} exact>
        <h1 key="collection-result-title">Collection search results</h1>
      </Route>
      <Route path={ROUTE.granules.path}>
        <h1 key="granule-result-title">Granule search results</h1>
      </Route>
    </Switch>
  )

  const drawerProxy = (
    <FocusProxy
      key="drawerProxy"
      ref={proxyRef}
      onFocus={() => setProxyFocusing(true)}
    />
  )

  const drawer = (
    <FocusTarget
      key="drawer"
      ref={targetRef}
      onBlur={() => setTargetBlurring(true)}
    >
      <DrawerContent />
    </FocusTarget>
  )

  const middle = (
    <div style={{width: '100%'}}>
      <Switch>
        {/*Each page inside this switch should have a Meta!*/}
        <Route path={`/:path(${validHomePaths.join('|')})`} exact>
          <LandingContainer />
        </Route>

        <Route path={ROUTE.details.path}>
          {/*TODO parameterize this path!*/}
          <div>
            <CollectionGetDetailLoadingContainer />
            <DetailContainer />
          </div>
        </Route>

        <Route path={ROUTE.granules.path}>
          <div>
            <GranuleSearchLoadingContainer />
            <ResultsContainer />
          </div>
        </Route>

        <Route path={ROUTE.collections.path}>
          <div>
            <CollectionSearchLoadingContainer />
            <ResultsContainer />
          </div>
        </Route>

        <Route path={ROUTE.about.path}>
          <AboutContainer />
        </Route>

        <Route path={ROUTE.help.path}>
          <Help />
        </Route>

        <Route path={ROUTE.cart.path}>
          <CartContainer />
        </Route>

        <Route path={ROUTE.error.path}>
          <ErrorContainer />
        </Route>

        {/* 404 not found */}
        <Route component={NotFoundContainer} />
      </Switch>
    </div>
  )

  return (
    <Layout
      location={location}
      /* - Disclaimer - */
      disclaimer={<DisclaimerContainer />}
      /* - Header - */
      header={<HeaderContainer showSearchInput={!isHome(location.pathname)} />}
      /* - Banner - */
      bannerGraphic={earth}
      bannerHeight={'30em'}
      bannerArcHeight={'15em'}
      bannerVisible={bannerVisible}
      hiddenAccessibilityHeading={hiddenAccessibilityHeading}
      /* - Left - */
      left={
        leftOpen ? (
          <FiltersContainer drawerProxy={drawerProxy} />
        ) : (
          <FiltersHiddenContainer
            text={onGranuleListPage ? 'File Filters' : 'Collection Filters'}
          />
        )
      }
      leftWidth={leftOpen ? '20em' : '2em'}
      leftOpen={leftOpen}
      leftVisible={leftVisible}
      /* - Drawer - */
      drawer={drawer}
      drawerOpen={drawerOpen}
      /* - Middle - */
      middle={middle}
      /* - Right - */
      right={rightOpen ? null : null}
      rightWidth={rightOpen ? '20em' : '2em'}
      rightOpen={rightOpen}
      rightVisible={rightVisible}
      /* - Footer - */
      footer={<FooterContainer />}
    />
  )
}

export default Root
