const logoPath = require('../../img/noaa_logo_circle_72x72.svg')

import React from 'react'
import { Link } from 'react-router'
import DetailContainer from '../detail/DetailContainer'
import Footer from './Footer.jsx'
import BannerContainer from './banner/BannerContainer'
import styles from './root.css'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import LoadingContainer from '../loading/LoadingContainer'

class RootComponent extends React.Component {
  constructor(props) {
    super(props)

    this.renderSearchBarInHeader = this.renderSearchBarInHeader.bind(this)
    this.renderOrgBoxStyle = this.renderOrgBoxStyle.bind(this)
    this.renderLogoTextStyle = this.renderLogoTextStyle.bind(this)

    this.location = props.location.pathname
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
  }

  renderSearchBarInHeader() {
    if(this.location !== '/') {
      return (
        <div className={`pure-u-1 pure-u-sm-3-4 ${styles.landingComponents}`}>
          <SearchFieldsContainer/>
        </div>
      )
    }
  }

  renderOrgBoxStyle() {
    if(this.location === '/') {
      return `pure-u-1 ${styles.orgBoxLanding}`
    }
    else {
      return `pure-u-5-24 ${styles.orgBox}`
    }
  }

  renderLogoTextStyle() {
    if(this.location === '/') {
      return `${styles.oneStopTextLanding}`
    }
    else {
      return `${styles.oneStopText}`
    }
  }

  render() {
    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>
          <div className={'pure-g'}>
            <div className={`${this.renderOrgBoxStyle()}`}>
              <div className={styles.logoLinks}>
                <a href="http://www.noaa.gov"><img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/></a>
                <Link to='/' activeClassName="active" onlyActiveOnIndex={true}>
                  <span className={`${this.renderLogoTextStyle()}`}><i className={`fa fa-stop-circle-o fa-md ${this.renderLogoTextStyle()}`}></i>neStop</span>
                </Link>
              </div>
            </div>
            {this.renderSearchBarInHeader()}
          </div>
        </div>
        <div className={styles.main}>
          <LoadingContainer/>
          {this.props.children}
        </div>
      </div>
      <div className={styles.footer}>
        <Footer/>
     </div>
   </div>
  }
}


export default RootComponent
