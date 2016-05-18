const logoPath = require('../../img/BS_noaalogo1.jpg');

import React from 'react'
import Paper from 'material-ui/lib/paper';
import ResultsContainer from '../result/ResultContainer';
import DetailContainer from '../detail/DetailContainer';
import SearchContainer from '../search/SearchContainer';
import Footer from './Footer.jsx';
import Header from './Header.jsx';

const HEADER_HEIGHT = 24;
const APP_BAR_HEIGHT = 84;
const RESULTS_WIDTH = 400;
const FOOTER_HEIGHT = 24;

const styles = {
  header: {
    position: 'fixed',
    top: 0,
    left: 0,
    height: HEADER_HEIGHT,
    width: '100%'
  },
  appbar: {
    position: 'fixed',
    top: HEADER_HEIGHT,
    height: APP_BAR_HEIGHT,
    width: '100%'
  },
  logo: {
    height: APP_BAR_HEIGHT,
    float: 'left'
  },
  results: {
    position: 'fixed',
    top: HEADER_HEIGHT + APP_BAR_HEIGHT,
    bottom: FOOTER_HEIGHT,
    width: RESULTS_WIDTH
  },
  details: {
    position: 'fixed',
    top: HEADER_HEIGHT + APP_BAR_HEIGHT,
    left: RESULTS_WIDTH,
    bottom: FOOTER_HEIGHT
  },
  footer: {
    position: 'fixed',
    right: 0,
    bottom: 0,
    left: 0
  },
  searchFacet: {
    float: 'right'
  }
};

const Root = () => (
    <div>
      <div style={styles.header}>
        <Header/>
      </div>
      <Paper style={styles.appbar} rounded={false}>
        <img style={styles.logo} src={logoPath} alt="NOAA Logo"/>
        <div style={styles.searchFacet}>
          <SearchContainer/>
        </div>
      </Paper>
      <div style={styles.results}>
        <ResultsContainer/>
      </div>
      <div style={styles.details}>
        <DetailContainer/>
      </div>
      <div style={styles.footer}>
        <Footer/>
      </div>
    </div>
);

export default Root
