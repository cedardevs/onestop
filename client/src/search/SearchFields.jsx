import React from 'react'
import TextSearchField from './TextSearchField'
import _ from 'lodash'

import FlexRow from '../common/FlexRow'
import Button from '../common/input/Button'
import search from 'fa/search.svg'

class SearchFields extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.clearSearch = props.clearSearch
    this.updateQuery = props.updateQuery
    this.clearQueryString = this.clearQueryString.bind(this)
    this.clearSearchParams = this.clearSearchParams.bind(this)
    this.warningStyle = this.warningStyle.bind(this)
    this.validateAndSubmit = this.validateAndSubmit.bind(this)
    this.state = {
      warning: '',
      hoveringWarningClose: false,
    }
  }

  handleMouseOverWarningClose = event => {
    this.setState({
      hoveringWarningClose: true,
    })
  }

  handleMouseOutWarningClose = event => {
    this.setState({
      hoveringWarningClose: false,
    })
  }

  clearQueryString() {
    this.setState({warning: ''})
    this.updateQuery('')
  }

  clearSearchParams() {
    this.setState({warning: ''})
    this.clearSearch()
  }

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return {
        display: 'none',
      }
    }
    else {
      return {
        position: 'absolute',
        top: 'calc(100% + 0.309em)',
        lineHeight: '1.618em',
        fontSize: '1em',
        color: 'white',
        backgroundColor: '#900303',
        borderRadius: '1em',
        padding: '0.618em',
      }
    }
  }

  warningCloseStyle() {
    if (this.state.hoveringWarningClose) {
      return {
        cursor: 'pointer',
        fontWeight: 'bold',
      }
    }
    else {
      return {
        cursor: 'pointer',
      }
    }
  }

  validateAndSubmit() {
    let trimmedQuery = _.trim(this.props.queryString)
    if (!trimmedQuery) {
      this.setState({warning: 'You must enter a search term.'})
    }
    else if (
      trimmedQuery &&
      (_.startsWith(trimmedQuery, '*') || _.startsWith(trimmedQuery, '?'))
    ) {
      this.setState({
        warning: 'Search query cannot start with asterisk or question mark.',
      })
    }
    else {
      this.setState({warning: ''})
      this.submit()
    }
  }

  render() {
    const searchButton = (
      <Button
        key="searchButton"
        icon={search}
        onClick={this.validateAndSubmit}
        title={'Search'}
        style={{fontSize: '1em', display: 'inline'}}
        styleIcon={{
          width: '1.3em',
          height: '1.3em',
          paddingTop: '0.309em',
          paddingBottom: '0.309em',
        }}
      />
    )

    let searchFieldStyle = null
    if (this.props.home) {
      searchFieldStyle = {
        position: 'relative',
        marginRight: '1em',
        alignSelf: 'center',
      }
    }
    else {
      searchFieldStyle = {
        position: 'relative',
        marginRight: '1em',
        alignSelf: 'center',
      }
    }

    return (
      <section style={searchFieldStyle}>
        <div style={this.warningStyle()} role="alert">
          {this.state.warning}{' '}
          <span
            style={this.warningCloseStyle()}
            onClick={this.clearQueryString}
            onMouseOver={this.handleMouseOverWarningClose}
            onMouseOut={this.handleMouseOutWarningClose}
          >
            x
          </span>
        </div>

        <div
          style={{display: 'flex', height: '2.618em', justifyContent: 'center'}}
        >
          <TextSearchField
            onEnterKeyDown={this.validateAndSubmit}
            onChange={this.updateQuery}
            onClear={this.clearQueryString}
            value={this.props.queryString}
          />
          {searchButton}
        </div>
      </section>
    )
  }
}

export default SearchFields
