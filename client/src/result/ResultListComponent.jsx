import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import 'purecss'
import styles from './result.css'

const ResultsList = ({results, loading, onResultClick}) => {
  return <div className={`pure-menu-list ${styles.list}`} zDepth={3} rounded={false}>
    {results.map(r => <Result key={r.id} record={r} onClick={onResultClick}/>)}
  </div>
}

ResultsList.propTypes = {
  results: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired
      })).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
