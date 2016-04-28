import React, { PropTypes } from 'react'
import CircularProgress from 'material-ui/lib/circular-progress';
import List from 'material-ui/lib/lists/list';
import Result from './Result';

const ResultsList = ({results, loading, onResultClick}) => {
  const styles = {
    spinner: {
      display: loading ? 'flex' : 'none',
      flexWrap: 'wrap',
      justifyContent: 'space-around',
      paddingTop: 24
    },
    list: {
      width: '100%',
      height: '100%',
      overflowY: 'auto'
    }
  };

  return <List style={styles.list} zDepth={3} rounded={false}>
    <div style={styles.spinner}><CircularProgress/></div>
    {results.map(r => <Result key={r.id} record={r} onClick={onResultClick}/>)}
  </List>
};

ResultsList.propTypes = {
  results: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired
      })).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
