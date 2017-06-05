import React, { PropTypes } from 'react'
import styles from './resultLayout.css'
import FacetContainer from '../search/facet/FacetContainer'
import _ from 'lodash'

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)

    this.toggleFacetMenu = this.toggleFacetMenu.bind(this)
    this.renderFacetMenu = this.renderFacetMenu.bind(this)
    this.renderFacetButton = this.renderFacetButton.bind(this)
    this.facetButtonImage = this.facetButtonImage.bind(this)
    this.renderResultsContainer = this.renderResultsContainer.bind(this)
    this.renderSelectedFilters = this.renderSelectedFilters.bind(this)
    this.clearFacetAndSubmitSearch = this.clearFacetAndSubmitSearch.bind(this)

    this.location = props.location.pathname
    this.selectedFacets = props.selectedFacets
    this.toggleFacet = props.toggleFacet
    this.submit = props.submit
    this.collapseFacetMenu = false
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
    this.selectedFacets = nextProps.selectedFacets
  }

  clearFacetAndSubmitSearch(category, term) {
    this.toggleFacet(category, term, false)
    this.submit()
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

  renderSelectedFilters() {
    if (!this.location.includes("files") && !_.isEmpty(this.selectedFacets)) {
      let appliedFilters = []

      _.forEach(this.selectedFacets, (terms, category) => {
        _.forEach(terms, (value) => {
          let name = value.split('>').pop().trim()
          let filter = (
            <span className={`${styles.filter}`} key={`${value}`}>{name} <span className={`${styles.close}`}
                                                                               onClick={() => this.clearFacetAndSubmitSearch(category, value)}>x</span></span>
          )
          appliedFilters.push(filter)
        })
      })



      return (
          <div className={`pure-u-1`}>
            <div className={`${styles.filters}`}>{appliedFilters}</div>
          </div>
      )
    }
  }

  render() {
    return <div id="layout" className={`pure-g ${styles.mainWindow}`}>
      {this.renderFacetMenu()}
      {this.renderFacetButton()}
      <div className={`${this.renderResultsContainer()} ${styles.resultsContainer}`}>
        {this.renderSelectedFilters()}
        {this.props.children}
      </div>
    </div>
  }
}

export default ResultLayout
