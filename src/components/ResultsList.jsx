import React, { PropTypes } from 'react'
import CircularProgress from 'material-ui/lib/circular-progress';
import Result from './Result';

const styles = {
  spinner: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    paddingTop: 24
  }
};

const ResultsList = ({results, loading}) => {
  if (loading) {
    return <div style={styles.spinner}><CircularProgress/></div>
  }
  else {
    return <div>{results.map(r => <Result key={r.id} record={r}/>)}</div>
  }
};

ResultsList.propTypes = {
  results: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
