import React from 'react'
import _ from 'lodash'
import search from 'fa/search.svg'

import Button from '../../common/input/Button'
import FlexRow from '../../common/ui/FlexRow'
import TextSearchField from '../text/TextSearchField'
import {SiteColors, boxShadow2} from '../../../style/defaultStyles'
import {times_circle, SvgIcon} from '../../common/SvgIcon'

const styleSearchWrapper = {
  display: 'flex',
  height: '2.618em',
  justifyContent: 'center',
}

const styleWarningCloseIcon = focusingWarningClose => {
  return {outline: focusingWarningClose ? '2px dashed white' : 'none'}
}

const styleWarningClose = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  padding: '0.618em',
}

const warningStyle = warning => {
  if (_.isEmpty(warning)) {
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
      fill: 'white',
      backgroundColor: SiteColors.WARNING,
      borderRadius: '0.309em',
      padding: '0.618em 0 0.618em 0.618em',
      boxShadow: boxShadow2,
      alignItems: 'center',
      justifyContent: 'space-between',
      zIndex: 6,
    }
  }
}

const searchFieldStyle = {
  position: 'relative',
  marginRight: '1em',
  alignSelf: 'center',
}

const styleSearchButtonIcon = {
  width: '1.3em',
  height: '1.3em',
  paddingTop: '0.309em',
  paddingBottom: '0.309em',
}

const styleSearchButtonFocus = home => {
  return home
    ? {
        outline: '2px dashed #5C87AC',
        outlineOffset: '.118em',
        zIndex: '1',
      }
    : {}
}

const styleSearchButton = {fontSize: '1em', display: 'inline'}

class CollectionSearch extends React.Component {
  constructor(props) {
    super(props)
    const {queryString} = this.props
    this.state = {
      queryString: queryString,
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

  handleInputChange = value => {
    this.setState({
      queryString: value,
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

  clearQueryString = () => {
    this.setState({warning: '', queryString: ''})
  }

  validateAndSubmit = () => {
    let trimmedQuery = _.trim(this.state.queryString)
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
      this.props.submit(trimmedQuery)
    }
  }

  render() {
    const {home} = this.props
    const {warning, focusingWarningClose, queryString} = this.state

    const instructionalCopy = home ? 'Search NOAA Data' : 'New NOAA Data Search'

    const searchButton = (
      <Button
        id="searchButton"
        key="searchButton"
        icon={search}
        onClick={this.validateAndSubmit}
        title={`Submit: ${instructionalCopy}`}
        style={styleSearchButton}
        styleFocus={styleSearchButtonFocus(home)}
        styleIcon={styleSearchButtonIcon}
      />
    )

    const warningText = <div key="warning-text">{warning}</div>

    const warningClose = (
      <button
        key="warning-close-button"
        style={styleWarningClose}
        onClick={this.clearQueryString}
        onMouseOver={this.handleMouseOverWarningClose}
        onMouseOut={this.handleMouseOutWarningClose}
        onFocus={this.handleFocusWarningClose}
        onBlur={this.handleBlurWarningClose}
        aria-label="close validation message"
      >
        <SvgIcon
          size="2em"
          style={styleWarningCloseIcon(focusingWarningClose)}
          path={times_circle}
        />
      </button>
    )

    const warningPopup = (
      <FlexRow
        style={warningStyle(warning)}
        items={[ warningText, warningClose ]}
        role="alert"
      />
    )

    return (
      <section style={searchFieldStyle}>
        <div role="search" style={styleSearchWrapper}>
          <TextSearchField
            id="collectionSearch"
            onEnterKeyDown={this.validateAndSubmit}
            onChange={this.handleInputChange}
            onClear={this.clearQueryString}
            value={queryString}
            warningPopup={warningPopup}
            instructionalCopy={instructionalCopy}
          />
          {searchButton}
        </div>
      </section>
    )
  }
}

export default CollectionSearch // TODO move this out of filters directory!
