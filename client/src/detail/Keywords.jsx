import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'

const styleKeyword = backgroundColor => {
  return {
    backgroundColor: backgroundColor ? backgroundColor : 'initial',
    color: 'white',
    padding: '0.35em',
    borderRadius: '0.1em 0.4em',
    margin: '0.25em 0.2em',
    flex: '0 1 auto',
  }
}

const styleKeywords = {
  display: 'flex',
  flexFlow: 'row wrap',
  justifyContent: 'left',
  alignContent: 'center',
}

const styleShowMoreButton = {
  float: 'right',
  textDecoration: 'underline',
  border: 'none',
  background: 'none',
}

const styleShoweMoreButtonHover = {
  cursor: 'pointer',
}

class Keywords extends Component {
  constructor(props) {
    super(props)
    this.state = {
      showAllThemes: false,
      showAllInstruments: false,
      showAllPlatforms: false,
      hoveringShowMore: false,
    }
  }

  handleShowGCMD = type => {
    if (type === 'gcmdScience') {
      this.setState({
        showAllThemes: !this.state.showAllThemes,
      })
    }
    else if (type === 'gcmdInstruments') {
      this.setState({
        showAllInstruments: !this.state.showAllInstruments,
      })
    }
    else if (type === 'gcmdPlatforms') {
      this.setState({
        showAllPlatforms: !this.state.showAllPlatforms,
      })
    }
  }

  handleMouseOverShowMore = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hoveringShowMore: true,
      }
    })
  }

  handleMouseOutShowMore = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hoveringShowMore: false,
      }
    })
  }

  renderGCMDKeywords(type, bgColor, showAll) {
    const {item} = this.props
    let keywords = (item && item[type]) || []

    if (!_.isEmpty(keywords)) {
      if (type === 'gcmdScience') {
        keywords = keywords
          .map(k => k.split('>')) // split GCMD keywords apart
          .reduce((list, keys) => list.concat(keys), []) // flatten
          .map(k => k.trim()) // you can figure this one out
          .filter((k, i, a) => a.indexOf(k) === i) // dedupe
      }
      else {
        keywords = keywords.map(term => term.split('>').pop().trim())
      }
      keywords = keywords.map(
        (k, index) =>
          index > 2 && !showAll ? null : (
            <div style={styleKeyword(bgColor)} key={k}>
              {k}
            </div>
          )
      )

      if (keywords.length > 3) {
        const styleShowMoreButtonMerged = {
          ...styleShowMoreButton,
          ...(this.state.hoveringShowMore ? styleShoweMoreButtonHover : {}),
        }

        return (
          <div>
            <div style={styleKeywords}>{keywords}</div>
            <button
              type="button"
              style={styleShowMoreButtonMerged}
              onMouseOver={this.handleMouseOverShowMore}
              onMouseOut={this.handleMouseOutShowMore}
              onClick={() => {
                this.handleShowGCMD(type)
              }}
            >
              {!showAll ? 'Show All' : 'Collapse'}
            </button>
          </div>
        )
      }
      else {
        return <div style={styleKeywords}>{keywords}</div>
      }
    }
    else {
      return (
        <div style={{fontStyle: 'italic', color: bgColor}}>None Provided</div>
      )
    }
  }

  render() {
    const {styleHeading} = this.props
    return (
      <div>
        <h3 style={styleHeading}>Themes:</h3>
        {this.renderGCMDKeywords(
          'gcmdScience',
          '#008445',
          this.state.showAllThemes
        )}
        <h3 style={styleHeading}>Instruments:</h3>
        {this.renderGCMDKeywords(
          'gcmdInstruments',
          '#0965a1',
          this.state.showAllInstruments
        )}
        <h3 style={styleHeading}>Platforms:</h3>
        {this.renderGCMDKeywords(
          'gcmdPlatforms',
          '#008445',
          this.state.showAllPlatforms
        )}
      </div>
    )
  }
}

Keywords.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default Keywords
