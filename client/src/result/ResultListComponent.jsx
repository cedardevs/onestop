import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import { CardStatus } from '../detail/DetailActions'
import styles from './result.css'
import 'font-awesome/css/font-awesome.css'

const ResultsList = ({results, loading, onCardClick}) => {
  const cards = []
  results.forEach((value, key) => {
    cards.push(<div key={key} className={`pure-u-1
                pure-u-md-1-2 pure-u-lg-1-3
                pure-u-xl-1-4 ${styles.grid}`}>
      <Result
          recordId={key}
          title={value.get('title')}
          thumbnail={value.get('thumbnail')}
          description={value.get('description')}
          flipped={value.get('cardStatus') != CardStatus.SHOW_FRONT}
          onCardClick={onCardClick}
      />
    </div>)
  })

  let display = null
  if(loading) {
    display =
        <div className={`pure-u-1`}>
          <div className={`${styles.spinner}`}>
            <i className="fa fa-anchor fa-spin fa-5x" aria-hidden="true"/>
          </div>
        </div>
  } else {
    display = <div className={`pure-g ${styles.gridContainer}`}>{cards}</div>
  }

  return display
}

ResultsList.propTypes = {
  results: PropTypes.instanceOf(Object).isRequired
}

ResultsList.defaultProps = {loading: false, results: new Map()}

export default ResultsList
