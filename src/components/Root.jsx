import React from 'react'
import AppBar from 'material-ui/lib/app-bar';
import ResultsContainer from '../containers/ResultsContainer';
import DetailContainer from '../containers/DetailContainer';
import SearchFacetContainer from '../containers/SearchFacetContainer';
//import FooterContainer from '../containers/FooterContainer';
import Footer from './Footer.jsx';
import Header from './Header.jsx';
import Paper from 'material-ui/lib/paper';

const styles = {
  header: {
      height: 24
  },
  appbar: {
      //border: '3px solid #01568D',
      //right: 500,
      //left: 258,
      top:24,
      height: 84,
      alignVertical:"top"
      //verticalAlign:"top"
      //alignItems: 'center'
  },
  results: {
    position: 'fixed',
    top: 64 + 40,
    bottom: 0,
    width: 400
  },
  details: {
    position: 'fixed',
    top: 64 + 40,
    left: 400,
    bottom: 0
  },
  footer: {
    position: 'absolute',
    right: 0,
    bottom: 0,
    left: 0
  },
    paper: {
        height: 64,
//        width: 100,
//        margin: 20,
//        textAlign: 'center',
//        display: 'inline-block'
    }
};

const Root = () => (
    <div>
      <div style={styles.header}>
          <Header/>
      </div>
      <div style={styles.appbar}>
          <Paper style={styles.paper} zDepth={3}>
          <img src="./BS_noaalogo1.jpg" width="258" height="57" alt="NOAA Logo"  />
          </Paper>
      </div>
      <div>
        <SearchFacetContainer />
      </div>
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
