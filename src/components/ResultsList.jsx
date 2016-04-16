import React, { PropTypes } from 'react'
import GridList from 'material-ui/lib/grid-list/grid-list';
import GridTile from 'material-ui/lib/grid-list/grid-tile';
import CircularProgress from 'material-ui/lib/circular-progress';

const styles = {
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    paddingTop: 24
  },
  gridList: {
    width: 800,
    overflowY: 'auto',
    marginBottom: 24
  }
};

const ResultsList = ({results, loading}) => {
  if (loading) {
    return <div style={styles.root}><CircularProgress/></div>
  }

  const tiles = results.map(result => (
      <GridTile key={result.name} title={result.name}>
        <img src={result.thumbnail}/>
      </GridTile>
  ));

  return (
      <div style={styles.root}>
        <GridList
            cellHeight={200}
            style={styles.gridList}
            cols={3}
            padding={10 }
        >
          {tiles}
        </GridList>
      </div>
  )
};

ResultsList.propTypes = {
  results: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string.isRequired,
    thumbnail: PropTypes.string.isRequired
  }).isRequired).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
