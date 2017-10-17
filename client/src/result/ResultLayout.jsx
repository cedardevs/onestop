import React, { PropTypes } from 'react'
import angleDoubleRight from 'fa/angle-double-right.svg'
import angleDoubleLeft from 'fa/angle-double-left.svg'
import styles from './resultLayout.css'
import Filters from '../filter/Filters'
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
    this.renderTemporalFilters = this.renderTemporalFilters.bind(this)
    this.clearFacetAndSubmitSearch = this.clearFacetAndSubmitSearch.bind(this)
    this.clearDateAndSubmitSearch = this.clearDateAndSubmitSearch.bind(this)

    this.location = props.location.pathname
    this.selectedFacets = props.selectedFacets
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.toggleFacet = props.toggleFacet
    this.updateDateRange = props.updateDateRange
    this.submit = props.submit
    this.collapseFacetMenu = false
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
    this.selectedFacets = nextProps.selectedFacets
    this.startDateTime = nextProps.startDateTime
    this.endDateTime = nextProps.endDateTime
  }

  clearFacetAndSubmitSearch(category, term) {
    this.toggleFacet(category, term, false)
    this.submit()
  }

  clearDateAndSubmitSearch(start, end) {
    this.updateDateRange(start, end)
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
        <Filters/>
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
          {this.facetButtonImage()}
        </span>
      </div>
    }
  }

  facetButtonImage() {
    if(this.collapseFacetMenu) {
      return <img src={angleDoubleRight} className={styles.facetButtonImage}></img>
    } else {
      return <img src={angleDoubleLeft} className={styles.facetButtonImage}></img>
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
    if (!this.location.includes("files")) {
      let appliedFilters = []
      _.forEach(this.selectedFacets, (terms, category) => {
        _.forEach(terms, (value) => {
          let name = value.split('>').pop().trim()
          let filter = (
            <span className={styles.filter} key={`${value}`}>
              {name}
              <span className={`${styles.close}`}
                onClick={() => this.clearFacetAndSubmitSearch(category, value)}>x
              </span>
            </span>
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

  renderTemporalFilters() {
    if (!this.location.includes("files") && (this.startDateTime || this.endDateTime)) {
      let appliedFilters = []
      if (this.startDateTime) {
        appliedFilters.push(
          <span className={`${styles.filter} ${styles.temporal}`} key="start">After: {this.startDateTime} <span
            className={`${styles.close}`}
            onClick={() => this.clearDateAndSubmitSearch(null, this.endDateTime)}>x</span></span>
        )
      }
      if (this.endDateTime) {
        appliedFilters.push(
          <span className={`${styles.filter} ${styles.temporal}`} key="end">Before: {this.endDateTime} <span
            className={`${styles.close}`}
            onClick={() => this.clearDateAndSubmitSearch(this.startDateTime, null)}>x</span></span>
        )
      }

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
        {/*
         TODO: Rendering temporal or spatial filters will require drill-down behavior on results view instead of currently
         present new-search behavior (otherwise applied filters update store but don't modify the search until a new search
         is sent -- i.e., time filter appears but doesn't apply)
         */}
        {/*{this.renderTemporalFilters()}*/}
        {this.props.children}
      </div>
    </div>
  }
}

export default ResultLayout
