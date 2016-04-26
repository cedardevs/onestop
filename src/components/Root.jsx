import React from 'react'
import AppBar from 'material-ui/lib/app-bar';
import ResultsContainer from '../containers/ResultsContainer';
import DetailContainer from '../containers/DetailContainer';
import SearchFacetContainer from '../containers/SearchFacetContainer';

const styles = {
  header: {
    height: 64,
  },
  results: {
    position: 'fixed',
    top: 64,
    bottom: 0,
    width: 400
  },
  details: {
    position: 'fixed',
    top: 64,
    left: 400,
    bottom: 0
  }
};

const Root = () => (
    <div>
      <AppBar
          title="Evan's Fancy OneStop Sandbox"
          showMenuIconButton={false}
          style={styles.header}
          zDepth={3}>
        <SearchFacetContainer/>
      </AppBar>
      <div style={styles.results}>
        <ResultsContainer/>
      </div>
      <div style={styles.details}>
        <DetailContainer/>
      </div>
    </div>
);

export default Root
