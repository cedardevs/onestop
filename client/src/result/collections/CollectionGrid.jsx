import React, {Component} from 'react'
import PropTypes from 'prop-types'
import ReactDOM from 'react-dom'
import _ from 'lodash'
import CollectionCard from './CollectionCard'
import Button from '../../common/input/Button'

const styleResultCountContainer = {
  display: 'block',
  padding: '0em 2em 1.618em 2em',
}

const styleResultCount = {
  display: 'inline',
  fontSize: '1.2em',
  margin: 0,
  padding: '0.309em',
}

const styleResultCountFocus = {
  backgroundColor: '#3E97D1',
}

const styleResultCountFocusBlur = {
  backgroundColor: 'transparent',
}

const styleGrid = {
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  alignItems: 'flex-start',
  alignContent: 'flex-start',
  margin: '0 0 0 2em',
}

const styleShowMore = {
  display: 'flex',
  justifyContent: 'center',
  paddingLeft: '2em',
  paddingRight: '2em',
}

export default class CollectionGrid extends Component {
  constructor(props) {
    super(props)
  }

  componentWillMount() {
    this.setState(prevState => {
      return {
        focusingResultsCount: false,
        focusedCardKey: null,
      }
    })
  }

  componentDidUpdate(prevProps, prevState) {
    const prevResultsLength = Object.keys(prevProps.results).length
    const currResultsLength = Object.keys(this.props.results).length

    if (prevResultsLength !== currResultsLength) {
      // TODO: Add feature if we can determine how to not get here when pressing the "Show More Results" button.
      // ----- Annoyingly, this causes the page to jump focus to the top again when requesting more results.
      // ----- Ideally, we would focus on the last card before expansion occurs when requesting more results.
      // ----- For now we are succesfully relying on the page layout to focus, and a tab will get from the filter
      // ----- menu to the search results.
      // ReactDOM.findDOMNode(this.resultCount).focus()
    }
    else if (prevState.focusedCardKey !== this.state.focusedCardKey) {
      if (this.focusCard) {
        ReactDOM.findDOMNode(this.focusCard).focus()
      }
    }
  }

  handleFocusResultsCount = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingResultsCount: true,
      }
    })
  }

  handleBlurResultsCount = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingResultsCount: false,
      }
    })
  }

  handleFocusCard = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusedCardKey: event.cardKey,
      }
    })
  }

  render() {
    const {
      loading,
      results,
      returnedHits,
      totalHits,
      selectCollection,
    } = this.props

    const headingText = loading
      ? `Loading...`
      : `Search Results (showing ${returnedHits} of ${totalHits})`

    const styleResultCountMerged = {
      ...styleResultCount,
      ...(this.state.focusingResultsCount
        ? styleResultCountFocus
        : styleResultCountFocusBlur),
    }

    let cards = []
    _.forOwn(results, (result, key) => {
      let tileProps = {
        key: key,
        cardKey: key,
        doi: result.doi,
        title: result.title,
        thumbnail: result.thumbnail,
        description: result.description,
        geometry: result.spatialBounding,
        onClick: () => selectCollection(key),
        onFocus: this.handleFocusCard,
      }

      cards.push(<CollectionCard {...tileProps} />)
    })

    return (
      <div>
        <div style={styleResultCountContainer}>
          <h1
            style={styleResultCountMerged}
            tabIndex={-1}
            ref={resultCount => (this.resultCount = resultCount)}
            onFocus={this.handleFocusResultsCount}
            onBlur={this.handleBlurResultsCount}
          >
            {headingText}
          </h1>
        </div>
        <div style={styleGrid}>{cards}</div>
        {this.renderShowMoreButton()}
      </div>
    )
  }

  renderShowMoreButton = () => {
    const {returnedHits, totalHits, fetchMoreResults} = this.props

    if (returnedHits < totalHits) {
      return (
        <div style={styleShowMore}>
          <Button text="Show More Results" onClick={() => fetchMoreResults()} />
        </div>
      )
    }
  }
}

CollectionGrid.propTypes = {
  loading: PropTypes.number.isRequired,
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
