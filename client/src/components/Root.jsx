const logoPath = require('../../img/noaa_logo_circle_72x72.svg')

import React from 'react'
import ResultsContainer from '../result/ResultContainer'
import SearchContainer from '../search/SearchContainer'
//import FacetContainer from '../facet/FacetContainer'
import Footer from './Footer.jsx'
import Header from './Header.jsx'
import styles from './root.css'


const Root = () => (
    <div>
      <div className={styles.header} id='header'>
        <Header/>
      </div>
      <div className={styles.appbar} id='appbar' rounded={false}>
        <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
        <div className={styles.noaaCaption}>NOAA</div>
        <div className={styles.searchFacet} id='search-facet'>
          <SearchContainer/>
        </div>
      </div>
      <div styleName={styles.results}>
        <ResultsContainer/>
      </div>
      <div className={styles.footer} id='footer'>
        <Footer/>
      </div>
    </div>
)

export default Root
