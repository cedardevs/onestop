import React, { PropTypes } from 'react'
import Breadcrumbs from 'react-breadcrumbs'
import styles from './collections/collectionGrid.css'
import FacetContainer from '../search/facet/FacetContainer'

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return <div id="layout" className={styles.mainWindow}>
      <div className={styles.facetSideBar}>
        <FacetContainer/>
      </div>
      <div className={styles.gridContainer}>
        <div className={styles.breadCrumbs}>
          <Breadcrumbs routes={this.props.routes} params={this.props.params}/>
        </div>
        {this.props.children}
      </div>
    </div>
  }
}

export default ResultLayout