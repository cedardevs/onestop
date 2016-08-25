import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import { CardStatus } from '../detail/DetailActions'
import styles from './result.css'
import FacetContainer from '../facet/FacetContainer'

const ResultsList = ({results, loading, onCardClick}) => {
  const cards = []
  results.forEach((value, key) => {
    cards.push(<div key={key} className={`${styles.grid}`}>
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
    display =
        <div id= "layout" className={` ${styles.resultContainer}`}>
            <div className={`${styles.facetContainer}`}>
              <FacetContainer/>
            </div>
            <div className={`  ${styles.gridContainer}`}>
              {cards}
            </div>
        </div>
  }

  return display
}

ResultsList.propTypes = {
  results: PropTypes.instanceOf(Object).isRequired
}

ResultsList.defaultProps = {loading: false, results: new Map()}

export default ResultsList
