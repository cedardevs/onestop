import React, {Component} from 'react'

import Container from '../layout/Container'

import Background from '../landing/background/Background'

import BannerContainer from './banner/BannerContainer'
import DetailContainer from '../detail/DetailContainer'
import HeaderContainer from './HeaderContainer'

import Filters from '../filter/Filters'

import LoadingBarContainer from '../loading/LoadingBarContainer'

import FooterContainer from './FooterContainer'

import styles from './root.css'

// component
export default class Root extends Component {
  constructor(props) {
    super(props)

    this.hasUnsupportedFeatures = this.hasUnsupportedFeatures.bind(this)
    this.location = props.location.pathname
    this.state = {
      leftVisible: true,
      rightVisible: false,
      tabCurrent: 'Search Results',
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
      <aside role="alert" className={styles.browserWarning}>
        <span
          className={styles.close}
          onClick={() => {
            this.setState({browserWarning: false})
          }}
        >
          x
        </span>
        <p>
          The browser that you are using to view this page is not currently
          supported. For a list of currently supported & tested browsers, please
          visit the
          <span>
            {' '}
            <a href={wikiUrl}>OneStop Documentation</a>
          </span>
        </p>
      </aside>
    )
  }

  isNotLanding() {
    return this.location !== '/' && this.location !== '/508/'
  }

  isNot508() {
    return this.location.indexOf('508') === -1 // TODO move this to redux state
  }

  isAboutPage() {
    return (
      this.location.startsWith('/about') ||
      this.location.startsWith('/508/about')
    )
  }

  isHelpPage() {
    return (
      this.location.startsWith('/help') || this.location.startsWith('/508/help')
    )
  }

  homeUrl() {
    const {host, pathname} = location
    return `//${host}${pathname ? pathname : '/'}#/${this.isNot508()
      ? ''
      : '508/'}`
  }

  render() {
    const header = (
      <div>
        <BannerContainer />
        <DetailContainer />
        <HeaderContainer
          showSearch={this.isNotLanding() && this.isNot508()}
          homeUrl={this.homeUrl()}
        />
        {this.state.browserWarning ? this.unsupportedBrowserWarning() : <div />}
      </div>
    )

    const left =
      this.isNotLanding() && !this.isAboutPage() && !this.isHelpPage() ? (
        <Filters />
      ) : null

    const middle = (
      <div>
        <LoadingBarContainer />
        <Background
          showImage={this.isNot508()}
          showOverlay={this.isNotLanding() && this.isNot508()}
        />
        {this.props.children}
      </div>
    )

    return (
      <Container
        header={header}
        left={left}
        leftWidth={256}
        leftVisible={this.state.leftVisible}
        middle={middle}
        right={null}
        rightWidth={256}
        rightVisible={this.state.rightVisible}
        footer={<FooterContainer />}
      />
    )
  }
}
