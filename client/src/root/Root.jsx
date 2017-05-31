import React from 'react'
import BannerContainer from './banner/BannerContainer'
import HeaderContainer from './HeaderContainer'
import FooterContainer from './FooterContainer'
import InfoContainer from '../common/info/infoContainer'
import DetailContainer from '../detail/DetailContainer'
import LoadingContainer from '../loading/LoadingContainer'
import styles from './root.css'

class RootComponent extends React.Component {
  constructor(props) {
    super(props)

    this.hasUnsupportedFeatures = this.hasUnsupportedFeatures.bind(this)

    this.location = props.location.pathname
    this.state = {
      browserWarning: this.hasUnsupportedFeatures()
    }
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
  }

  hasUnsupportedFeatures(){
    let browserWarning = false
    const htmlClasses = document.documentElement.className.split(' ')
    htmlClasses.forEach(htmlClass => {
      if(htmlClass.startsWith('no-')){ browserWarning = true; return; }
    })
    return browserWarning
  }

  unsupportedBrowserWarning() {
    const wikiUrl = 'https://github.com/cires-ncei/onestop/wiki/OneStop-Client-Supported-Browsers'
    return <aside role='alert' className={styles.browserWarning}>
        <span className={styles.close}
          onClick={()=>{this.setState({browserWarning: false})}}>x</span>
        <p>
          The browser that you are using to view this page is not currently supported.
          For a list of currently supported & tested browsers, please visit the
          <span> <a href={wikiUrl}>OneStop Documentation</a></span>
        </p>
    </aside>
  }

  render() {
    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <HeaderContainer showSearch={this.isNotLanding() && this.isNot508()}
           homeUrl={this.homeUrl()}/>
        {this.state.browserWarning ? this.unsupportedBrowserWarning() : <div></div>}
        <div className={styles.main}>
          <InfoContainer modalMode={this.isNotLanding()}/>
          <LoadingContainer/>
          {this.props.children}
        </div>
      </div>
      <div className={styles.footer}>
        <FooterContainer/>
     </div>
   </div>
  }

  isNotLanding() {
    return this.location !== '/' && this.location !== '/508/'
  }

  isNot508() {
    return this.location.indexOf('508') === -1
  }

  homeUrl() {
    const { host, pathname } = location
    return `//${host}${pathname ? pathname : '/'}#/${this.isNot508() ? '' : '508/'}`
  }
}


export default RootComponent
