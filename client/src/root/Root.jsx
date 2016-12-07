const logoPath = require('../../img/noaa_logo_circle_72x72.svg')

import React from 'react'
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
    let searchLabel = this.props.location.pathname === "/" ? "searchMapSpace" : 'searchHover';

    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>
          <div className={'pure-g'}>
            <div className={`pure-u-5-24 ${styles.orgBox}`}>
              <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
              <span className={styles.oneStopText}><i className={`fa fa-stop-circle-o fa-md ${styles.oneStopText}`}></i>neStop</span>
            </div>
            <div className={`pure-u-3-4 ${styles.landingComponents} ${styles[searchLabel]}`}>
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
