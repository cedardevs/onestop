import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import Detail from '../detail/DetailComponent'
import {CardStatus} from '../detail/DetailActions'
import styles from './result.css'

const ResultsList = ({results, loading, details, onCardClick}) => {
  var cards = []
  results.forEach(function(val, key){
    cards.push(<div key={key} className={`${styles['pure-u-1']} ${styles['pure-u-md-1-2']} ${styles['pure-u-lg-1-3']} ${styles['pure-u-xl-1-4']} ${styles.grid}`}>
      <Detail key={key}
        flipped = {details[key].cardStatus != CardStatus.SHOW_FRONT}
        onClick={() => onCardClick(key)}
        title={val.title}
        description={val.description}
        thumbnail={val.thumbnail_s}
      />
    </div>)
  })
  return <div className={`${styles['pure-g']} ${styles.gridContainer}`} zDepth={3} rounded={false}>
    {cards}
  </div>
}

ResultsList.propTypes = {
  results: PropTypes.instanceOf(Object).isRequired,
  loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: new Map()}

export default ResultsList
