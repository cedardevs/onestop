import React, { PropTypes } from 'react'
import CircularProgress from 'material-ui/lib/circular-progress';
import List from 'material-ui/lib/lists/list';
import Result from './Result';

const ResultsList = ({results, loading}) => {
  const styles = {
    spinner: {
      display: loading ? 'flex' : 'none',
      flexWrap: 'wrap',
      justifyContent: 'space-around',
      paddingTop: 24
    },
    list: {
      position: 'fixed',
      top: 64,
      bottom: 0,
      width: 400,
      overflowY: 'auto'
    }
  };

  return <List style={styles.list} zDepth={3}>
    <div style={styles.spinner}><CircularProgress/></div>
    {results.map(r => <Result key={r.id} record={r}/>)}
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
