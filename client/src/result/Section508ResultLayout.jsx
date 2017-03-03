import React, { PropTypes } from 'react'
import styles from './resultLayout.css'
import FacetContainer from '../search/facet/Section508FacetContainer'

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)

    this.toggleFacetMenu = this.toggleFacetMenu.bind(this)
    this.renderFacetMenu = this.renderFacetMenu.bind(this)
    this.renderFacetButton = this.renderFacetButton.bind(this)
    this.facetButtonImage = this.facetButtonImage.bind(this)
    this.renderResultsContainer = this.renderResultsContainer.bind(this)

    this.location = props.location.pathname
    this.collapseFacetMenu = false
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
  }

  toggleFacetMenu() {
    this.collapseFacetMenu = !this.collapseFacetMenu
    this.forceUpdate()
  }

  renderFacetMenu() {
    if(this.location.includes("files") || this.collapseFacetMenu) {
      return <div display="none"></div>
    }
    else {
      return <div className={`pure-u-10-24 pure-u-md-7-24 pure-u-lg-5-24 ${styles.facetSideBar}`}>
        <FacetContainer/>
      </div>
    }
  }

  renderFacetButton() {
    if(this.location.includes("files")) {
      return <div className="pure-u-1-24"></div>
    }
    else {
      let buttonStyling
      let buttonColumnStyling
      if(this.collapseFacetMenu) {
        buttonStyling = styles.facetButtonCollapsed
        buttonColumnStyling = styles.buttonColumn
      }
      else {
        buttonStyling = styles.facetButtonExpanded
        buttonColumnStyling = null
      }

      return <div className={`pure-u-1-24 ${buttonColumnStyling}`}>
        <span className={buttonStyling} onClick={this.toggleFacetMenu}>
          <i className={`${this.facetButtonImage()}`}></i>
        </span>
      </div>
    }
  }

  facetButtonImage() {
    if(this.collapseFacetMenu) {
      return "fa fa-angle-double-right"
    }
    else {
      return "fa fa-angle-double-left"
    }
  }

  renderResultsContainer() {
    if(this.location.includes("files") || this.collapseFacetMenu) {
      return "pure-u-22-24"
    }
    else {
      return "pure-u-12-24 pure-u-md-15-24 pure-u-lg-17-24"
    }
  }

  render() {
    return <div id="layout" className={`pure-g ${styles.mainWindow}`}>
      <div className={`pure-u-1 ${styles.resultsContainer}`}>
        {this.props.children}
      </div>
    </div>
  }
}

export default ResultLayout
