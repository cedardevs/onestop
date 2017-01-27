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
  }

  render() {
    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>
          <div className={'pure-g'}>
            <div className={`pure-u-5-24 ${styles.orgBox}`}>
              <Link to='/' activeClassName="active" onlyActiveOnIndex={true} className={styles.logoLink}>
                <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
                <span className={styles.oneStopText}><i
                  className={`fa fa-stop-circle-o fa-md ${styles.oneStopText}`}></i>neStop</span>
              </Link>
            </div>
            <div className={`pure-u-1 pure-u-sm-3-4 ${styles.landingComponents}`}>
              <SearchFieldsContainer/>
            </div>
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
