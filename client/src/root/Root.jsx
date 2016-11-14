const logoPath = require('../../img/noaa_logo_circle_72x72.svg')
//const logoPath = require('../../img/cireslogo-cc.png')

import React from 'react'
import DetailContainer from '../detail/DetailContainer'
//import Favicon from 'react-favicon'
import Footer from './Footer.jsx'
import AlphaBanner from './AlphaBanner.jsx'
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
        <AlphaBanner/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>
          <div className={'pure-g'}>
            <div className={`pure-u-1-4 ${styles.orgBox}`}>
              <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
              <div className={styles.orgInfo}>
                <a className={styles.noaa}>National Oceanic and Atmospheric Administration</a>
                <a className={styles.doc} href="//www.commerce.gov">U.S. Department of Commerce</a>
              </div>
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
