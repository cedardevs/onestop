import React, { Component } from 'react'
import ReactDOM from 'react-dom'
import _ from 'lodash'
import CollectionCard from './CollectionCard'
import Button from '../../common/input/Button'

const styleResultCountContainer = {
  display: "block",
  padding: "0em 2em 1.618em 2em"
}

const styleResultCount = {
  display: "inline",
  fontSize: "1.2em",
  margin: 0,
  padding: "0.309em"
}

const styleResultCountFocus = {
  backgroundColor: "#3E97D1"
}

const styleResultCountFocusBlur = {
  backgroundColor: "transparent"
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
  paddingLeft: "2em",
  paddingRight: "2em"
}

export default class CollectionGrid extends Component {
  constructor(props) {
    super(props)
    this.renderShowMoreButton = this.renderShowMoreButton.bind(this)
  }

  componentWillMount() {
    this.setState(prevState => {
      return {
        focusingResultsCount: false,
        focusedCardKey: null
      }
    })
  }

  renderShowMoreButton() {
    if (this.props.returnedHits < this.props.totalHits) {
      return (
          <div style={styleShowMore}>
            <Button text="Show More Results" onClick={() => this.props.fetchMoreResults()}/>
          </div>
      )
    }
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
        focusingResultsCount: true
      }
    })
  }

  handleBlurResultsCount = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingResultsCount: false
      }
    })
  }

  handleFocusCard = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusedCardKey: event.cardKey
      }
    })
  }

  render() {
    const {results, returnedHits, totalHits, pageSize} = this.props

    const styleResultCountMerged = {
      ...styleResultCount,
      ...(this.state.focusingResultsCount ? styleResultCountFocus : styleResultCountFocusBlur )
    }

    let cards = []
    _.forOwn(results, (result, key) => {
      let tileProps = {
        key: key,
        cardKey: key,
        title: result.title,
        thumbnail: result.thumbnail,
        description: result.description,
        geometry: result.spatialBounding,
        onClick: () => this.props.onCardClick(key),
        onFocus: this.handleFocusCard
      }

      if (this.state.focusedCardKey === key) {
        tileProps.ref = focusCard => this.focusCard = focusCard
      }

      cards.push(<CollectionCard {...tileProps} />)
    })

    return (
        <div>
          <div style={styleResultCountContainer}>
            <h1 style={styleResultCountMerged} tabIndex={0} ref={resultCount => (this.resultCount = resultCount)}
                onFocus={this.handleFocusResultsCount} onBlur={this.handleBlurResultsCount}>
              Search Results (showing {returnedHits} of {totalHits})
            </h1>
          </div>
          <div style={styleGrid}>{cards}</div>
          {this.renderShowMoreButton()}
        </div>
    )
  }
}
