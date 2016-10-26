import React, { PropTypes } from 'react'
import Breadcrumbs from 'react-breadcrumbs'
import Result from './ResultComponent'
import { CardStatus } from '../detail/DetailActions'
import styles from './result.css'
import FacetContainer from '../search/facet/FacetContainer'

const ResultsList = ({results, onCardClick, location, routes, params}) => {
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

  let breadcrumbs
  if (location.pathname !== "/") {
    breadcrumbs = <Breadcrumbs routes={routes} params={params}/>
  }

  return <div id="layout" className={styles.mainWindow}>
            <div className={styles.facetSideBar}>
              <FacetContainer/>
            </div>
            <div className={styles.gridContainer}>
              <div className={styles.breadCrumbs}>
                {breadcrumbs}
              </div>
              {cards}
            </div>
        </div>
}

ResultsList.propTypes = {
  results: PropTypes.instanceOf(Object).isRequired
}

ResultsList.defaultProps = {loading: false, results: new Map()}

export default ResultsList
