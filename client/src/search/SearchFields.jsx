import React from 'react'
import TextSearchField from './TextSearchField'
import _ from 'lodash'

import Button from '../common/input/Button'
import FlexRow from '../common/FlexRow'
import search from 'fa/search.svg'
import {SiteColors, boxShadow2} from '../common/defaultStyles'
import {times_circle, SvgIcon} from '../common/SvgIcon'

const styleWarningClose = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  padding: '0.618em',
}

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
      focusingWarningClose: false,
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

  handleFocusWarningClose = event => {
    this.setState({
      focusingWarningClose: true,
    })
  }

  handleBlurWarningClose = event => {
    this.setState({
      focusingWarningClose: false,
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
        left: 0,
        right: 0,
        lineHeight: '1.618em',
        fontSize: '1em',
        color: 'white',
        backgroundColor: SiteColors.WARNING,
        borderRadius: '0.309em',
        padding: '0.618em 0 0.618em 0.618em',
        boxShadow: boxShadow2,
        alignItems: 'center',
        justifyContent: 'space-between',
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
    const instructionalCopy = this.props.home
      ? 'Search NCEI Data'
      : 'New NCEI Data Search'

    const searchButton = (
      <Button
        key="searchButton"
        icon={search}
        onClick={this.validateAndSubmit}
        title={`Submit: ${instructionalCopy}`}
        style={{fontSize: '1em', display: 'inline'}}
        styleIcon={{
          width: '1.3em',
          height: '1.3em',
          paddingTop: '0.309em',
          paddingBottom: '0.309em',
        }}
      />
    )

    const searchFieldStyle = {
      position: 'relative',
      marginRight: '1em',
      alignSelf: 'center',
    }

    const warningText = <div key="warning-text">{this.state.warning}</div>

    const styleSvgIcon = {
      outline: this.state.focusingWarningClose ? '2px dashed white' : 'none',
    }
    const svgFillColor = 'white'

    const warningClose = (
      <button
        key="warning-close-button"
        style={styleWarningClose}
        onClick={this.clearQueryString}
        onMouseOver={this.handleMouseOverWarningClose}
        onMouseOut={this.handleMouseOutWarningClose}
        onFocus={this.handleFocusWarningClose}
        onBlur={this.handleBlurWarningClose}
        aria-label="close warning message"
      >
        <SvgIcon
          size="2em"
          style={styleSvgIcon}
          path={times_circle(svgFillColor)}
        />
      </button>
    )

    const warning = (
      <FlexRow
        style={this.warningStyle()}
        items={[ warningText, warningClose ]}
        role="alert"
      />
    )

    return (
      <section style={searchFieldStyle}>
        <div
          role="search"
          style={{display: 'flex', height: '2.618em', justifyContent: 'center'}}
        >
          <TextSearchField
            onEnterKeyDown={this.validateAndSubmit}
            onChange={this.updateQuery}
            onClear={this.clearQueryString}
            value={this.props.queryString}
            warning={warning}
            instructionalCopy={instructionalCopy}
          />
          {searchButton}
        </div>
      </section>
    )
  }
}

export default SearchFields
