const logoPath = require('../../img/noaa_logo_circle_72x72.svg')

import React from 'react'
import ResultsListContainer from '../result/ResultsListContainer'
import SearchContainer from '../search/SearchContainer'
//import FacetContainer from '../facet/FacetContainer'
import Favicon from 'react-favicon'
import Footer from './Footer.jsx'
import Header from './Header.jsx'
import AlphaBanner from './AlphaBanner.jsx'
import styles from './root.css'

const Root = () => (
    <div>
      <Favicon url={["http://www.noaa.gov/sites/all/themes/custom/noaa/favicon.ico"]}/>
      <AlphaBanner/>
      <div className={styles.bottomBorder}>
        <div className={styles.panel}>
          <div className={styles['pure-g']}>
            <div className={styles['pure-u-3-5']}>
                <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
                <div className={styles.orgBox}>
                    <a className={styles.orgName} href="/">National Oceanic and Atmospheric Administration</a>
                    <a className={styles.deptName} href="http://www.commerce.gov">U.S. Department of Commerce</a>
                </div>
            </div>
            <div className={`${styles['pure-u-2-5']} ${styles.searchFacet}`}>
              <SearchContainer/>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.results}>
        <ResultsListContainer/>
      </div>
      <Footer/>
    </div>
)


export default Root
