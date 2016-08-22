import React, { PropTypes } from 'react'
import Result from './ResultComponent'
import { CardStatus } from '../detail/DetailActions'
import styles from './result.css'
import 'font-awesome/css/font-awesome.css'
import facetList from '../facet/FacetContainer'

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
    display =
        <div id= "layout" className={` pure-g `}>
          <div  className={`${styles.facetContainer}`}>
            <div className={`pure-menu `}>
             {facetList}
            </div>
            </div>
          <div className={`${styles.gridContent}`}>
            <div className={`${styles.gridContainer}`}>
              {cards}
            </div>
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
