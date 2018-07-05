import React, {Component} from 'react'
import {Route, Switch} from 'react-router'

import Background from '../layout/Background'
import Container from '../layout/Container'

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

import LoadingBarContainer from '../loading/LoadingBarContainer'

import FooterContainer from './FooterContainer'

import {COLOR_SECONDARY_DARK} from '../common/defaultStyles'

const styleBrowserWarning = {
  background: COLOR_SECONDARY_DARK,
  width: '96%',
  margin: '1em auto',
  padding: '0.3em 1em',
  borderRadius: '3px',
  color: '#fff',
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
    const htmlClasses = document.documentElement.className.split(' ')
    htmlClasses.forEach(htmlClass => {
      if (htmlClass.startsWith('no-')) {
        browserWarning = true
        return
      }
    })
    return browserWarning
  }

  unsupportedBrowserWarning() {
    const wikiUrl =
      'https://github.com/cedardevs/onestop/wiki/OneStop-Client-Supported-Browsers'
    return (
      <aside role="alert" style={styleBrowserWarning}>
        <span
          style={styleClose}
          onClick={() => {
            this.setState({browserWarning: false})
          }}
        >
          x
        </span>
        <p style={styleBrowserWarningParagraph}>
          The browser that you are using to view this page is not currently
          supported. For a list of currently supported & tested browsers, please
          visit the
          <span>
            {' '}
            <a style={styleBrowserWarningLink} href={wikiUrl}>
              OneStop Documentation
            </a>
          </span>
        </p>
      </aside>
    )
  }

  render() {
    const {showLeft, leftOpen, showRight} = this.props

    const header = (
      <div>
        <BannerContainer />
        <Route path="/">
          <HeaderContainer />
        </Route>
        {this.state.browserWarning ? this.unsupportedBrowserWarning() : <div />}
      </div>
    )

    const left = leftOpen ? <FiltersContainer /> : <FiltersHiddenContainer />
    const leftWidth = leftOpen ? '20em' : '2em'

    const middle = (
      <div style={{width: '100%'}}>
        <Switch>
          <Route path="/" exact />
          <Route path="/">
            <LoadingBarContainer />
          </Route>
        </Switch>
        <Switch>
          <Route path="/collections" exact>
            {/*TODO: replace this with ArcGIS map?*/}
            <MapContainer selection={true} features={false} />
          </Route>
        </Switch>
        {this.props.children}

        <Switch>
          {/*Each page inside this switch should have a Meta!*/}
          <Route path="/" exact>
            <LandingContainer />
          </Route>

          <Route path="/collections/details">
            {/*TODO parameterize this path!*/}
            <DetailContainer />
          </Route>

          <Route path="/collections">
            <Result>
              <Switch>
                <Route path="/collections" exact>
                  <CollectionsContainer />
                </Route>
                <Route path="/collections/granules">
                  {/*TODO parameterize this path!*/}
                  <GranuleListContainer />
                </Route>
              </Switch>
            </Result>
          </Route>

          <Route path="/about">
            <AboutContainer />
          </Route>
          <Route path="/help">
            <Help />
          </Route>

          <Route path="/error">
            <ErrorContainer />
          </Route>
        </Switch>
      </div>
    )

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
