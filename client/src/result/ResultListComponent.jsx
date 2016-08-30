import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import { CardStatus } from '../detail/DetailActions'
import styles from './result.css'
import FacetContainer from '../search/facet/FacetContainer'

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

  return <div id= "layout" className={` ${styles.resultContainer}`}>
            <div className={`${styles.facetContainer}`}>
              <FacetContainer/>
            </div>
            <div className={`  ${styles.gridContainer}`}>
              {cards}
            </div>
        </div>
}

ResultsList.propTypes = {
  results: PropTypes.instanceOf(Object).isRequired
}

ResultsList.defaultProps = {loading: false, results: new Map()}

export default ResultsList
