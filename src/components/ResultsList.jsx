import React, { PropTypes } from 'react'
import GridList from 'material-ui/lib/grid-list/grid-list';
import CircularProgress from 'material-ui/lib/circular-progress';
import Result from './Result';

const styles = {
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    paddingTop: 24
  },
  gridList: {
    width: 800,
    height: 600,
    overflowY: 'auto',
    marginBottom: 24
  }
};

const ResultsList = ({results, loading}) => {
  if (loading) {
    return <div style={styles.root}><CircularProgress/></div>
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
