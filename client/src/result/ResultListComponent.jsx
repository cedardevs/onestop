import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import Detail from '../detail/DetailComponent'
import styles from './result.css'

const ResultsList = ({results, loading, onCardClick}) => {
  console.log("Results: " + results)
  return <div className={`${styles['pure-g']} ${styles.gridContainer}`} zDepth={3} rounded={false}>
    {results.map(r => <div className={`${styles['pure-u-1']} ${styles['pure-u-md-1-2']} ${styles['pure-u-lg-1-3']} ${styles['pure-u-xl-1-4']} ${styles.grid}`}>
      <Detail key={r.id}
        onClick={() => onCardClick(r.id)}
        title={r.title}
        description={r.description}
        thumbnail={r.thumbnail_s}
      />
    </div>)}
  </div>
}

ResultsList.propTypes = {
  results: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired
      })).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []}

export default ResultsList
