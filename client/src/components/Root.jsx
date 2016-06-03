const logoPath = require('../../img/BS_noaalogo1.jpg')

import React from 'react'
import ResultsContainer from '../result/ResultContainer'
import DetailContainer from '../detail/DetailContainer'
import SearchContainer from '../search/SearchContainer'
import FacetContainer from '../facet/FacetContainer'
import Footer from './Footer.jsx'
import Header from './Header.jsx'
import CSSModules from 'react-css-modules'
import styles from './root.css'


const Root = () => (
    <div>
      <div styleName='header'>
        <Header/>
      </div>
      <div styleName='appbar' rounded={false}>
        <img styleName='logo' src={logoPath} alt="NOAA Logo"/>
        <div styleName='searchFacet'>
          <SearchContainer/>
        </div>
      </div>
      <div styleName="leftNav">
        <FacetContainer/>
      </div>
      <div styleName='results'>
        <ResultsContainer/>
      </div>
      <div styleName='details'>
        <DetailContainer/>
      </div>
      <div styleName='footer'>
        <Footer/>
      </div>
    </div>
)

export default Root
