const logoPath = require('../../img/BS_noaalogo1.jpg');

import React from 'react'
import Paper from 'material-ui/lib/paper';
import ResultsContainer from '../result/ResultContainer';
import DetailContainer from '../detail/DetailContainer';
import SearchContainer from '../search/SearchContainer';
import Footer from './Footer.jsx';
import Header from './Header.jsx';
import CSSModules from 'react-css-modules';
import styles from './root.css';


const Root = () => (
    <div>
      <div styleName='header'>
        <Header/>
      </div>
      <Paper styleName='appbar' rounded={false}>
        <img styleName='logo' src={logoPath} alt="NOAA Logo"/>
        <div styleName='searchFacet'>
          <SearchContainer/>
        </div>
      </Paper>
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
);

export default Root
