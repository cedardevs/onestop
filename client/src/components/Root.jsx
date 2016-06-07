const logoPath = require('../../img/BS_noaalogo1.jpg')

import React from 'react'
import ResultsContainer from '../result/ResultContainer'
import DetailContainer from '../detail/DetailContainer'
import SearchContainer from '../search/SearchContainer'
import Footer from './Footer.jsx'
import Header from './Header.jsx'
import CSSModules from 'react-css-modules'
import './root.css'


const Root = () => (
    <div>
      <div styleName='header' id='header'>
        <Header/>
      </div>
      <div styleName='appbar' id='appbar' rounded={false}>
        <img styleName='logo' id='logo' src={logoPath} alt="NOAA Logo"/>
        <div styleName='search-facet' id='search-facet'>
          <SearchContainer/>
        </div>
      </div>
      <div styleName='results' id='results'>
        <ResultsContainer/>
      </div>
      <div styleName='details' id='details'>
        <DetailContainer/>
      </div>
      <div styleName='footer' id='footer'>
        <Footer/>
      </div>
    </div>
)

export default Root
