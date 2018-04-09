import React, {Component} from 'react'

import Background from '../layout/Background'
import Container from '../layout/Container'

import BannerContainer from './banner/BannerContainer'
import HeaderContainer from './HeaderContainer'
import MapContainer from '../search/map/MapContainer'

import FiltersContainer from '../filter/FiltersContainer'
import FiltersHiddenContainer from '../filter/FiltersHiddenContainer'

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
    this.location = props.location.pathname
    this.state = {
      leftVisible: true,
      rightVisible: false,
      browserWarning: this.hasUnsupportedFeatures(),
    }
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
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

  isNotLanding() {
    return this.location !== '/'
  }

  isAboutPage() {
    return this.location.startsWith('/about')
  }

  isHelpPage() {
    return this.location.startsWith('/help')
  }

  homeUrl() {
    const {host, pathname} = location
    return `//${host}${pathname ? pathname : '/'}#/`
  }

  render() {
    const {showLeft, leftOpen, showRight, onDetailPage} = this.props

    const header = (
      <div>
        <BannerContainer />
        <HeaderContainer
          showSearch={this.isNotLanding()}
          homeUrl={this.homeUrl()}
        />
        {this.state.browserWarning ? this.unsupportedBrowserWarning() : <div />}
      </div>
    )

    const layoutContext =
      this.isNotLanding() && !this.isAboutPage() && !this.isHelpPage()

    let left = null
    let leftWidth = '20em'

    if (layoutContext) {
      if (showLeft) {
        if (leftOpen) {
          left = <FiltersContainer />
        }
        else {
          leftWidth = '2em' // must match width + 2x padding of container in FilterHidden.jsx
          left = <FiltersHiddenContainer />
        }
      }
    }

    const loadingBarStyle = this.isNotLanding() ? {} : {display: 'none'}
    const onHomePage = !this.isNotLanding()

    const middle = (
      <div style={{width: '100%'}}>
        <LoadingBarContainer style={loadingBarStyle} />
        {/*TODO: replace this with ArcGIS map?*/}
        <MapContainer selection={true} features={false} />
        {this.props.children}
      </div>
    )

    // constrain middle gives the middle section a max-width
    const middleBackgroundColor = onDetailPage ? 'white' : 'initial'

    return (
      <Background onHomePage={onHomePage}>
        <Container
          header={header}
          left={left}
          leftWidth={leftWidth}
          leftVisible={leftOpen}
          middle={middle}
          middleBackgroundColor={middleBackgroundColor}
          onHomePage={onHomePage}
          right={null}
          rightWidth={256}
          rightVisible={showRight}
          footer={<FooterContainer />}
        />
      </Background>
    )
  }
}
