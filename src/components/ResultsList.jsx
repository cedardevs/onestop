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
    height: 600,
    overflowY: 'auto',
    marginBottom: 24
  }
};

const ResultsList = ({results, loading}) => {
  if (loading) {
    return <div style={styles.root}><CircularProgress/></div>
  }

  const tiles = results.map(result => {
    const defaultThumbnailHref =
        '//upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/300px-No_image_available.svg.png'
    const thumbnailLink = result.links.find(link => link.type === 'thumbnail');
    const thumbnailHref = thumbnailLink && thumbnailLink.href || defaultThumbnailHref;

    return (
        <GridTile key={result.id} title={result.title}>
          <img src={thumbnailHref}/>
        </GridTile>
    )
  });

  return (
      <div style={styles.root}>
        <GridList
            cellHeight={200}
            style={styles.gridList}
            cols={3}
            padding={10}
        >
          {tiles}
        </GridList>
      </div>
  )
};

ResultsList.propTypes = {
  results: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    links: PropTypes.arrayOf(PropTypes.shape({
      href: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired
    })).isRequired
  }).isRequired).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
