import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import _ from 'lodash'
import Button from './input/Button'
import gridIcon from 'fa/th.svg'
import listIcon from 'fa/th-list.svg'
import {fontFamilySerif} from '../utils/styleUtils'

const styleListView = {
  marginLeft: '1.618em',
}

const styleListInfo = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  margin: '0 1.618em 0.618em 0',
  padding: 0,
}

const styleListControl = {
  display: 'flex',
  padding: '0.618em',
  backgroundColor: 'rgba(0,0,0, 0.2)',
  borderRadius: '0.309em',
  margin: '0 1.618em 1.618em 0',
}

const styleControlButtonIcon = {
  width: '1em',
  height: '1em',
  marginRight: '0.309em',
}

const styleGrid = {
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  alignItems: 'flex-start',
  alignContent: 'flex-start',
}

const styleList = {
  display: 'flex',
  flexDirection: 'column',
  flexWrap: 'nowrap',
}

const styleFallbackItem = {
  display: 'block',
  margin: '0 1.618em 0 0',
}

const styleFocusDefault = {
  outline: 'none',
  border: '.1em dashed white', // ems so it can be calculated into the total size easily - border + padding + margin of this style must total the same as padding in styleOverallHeading, or it will resize the element when focus changes
  padding: '.259em',
  margin: '.259em',
}

export default class ListView extends Component {
  constructor(props) {
    super(props)
    this.state = {
      showAsGrid: !!props.GridItemComponent,
      previousResultsLength: null,
    }
  }

  toggleShowAsGrid = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showAsGrid: !prevState.showAsGrid,
      }
    })
  }

  componentDidMount() {
    if (this.focusItem) {
      ReactDOM.findDOMNode(this.focusItem).focus()
    }
  }

  componentWillReceiveProps(nextProps) {
    const currResultsLength = Object.keys(this.props.items).length
    const nextResultsLength = Object.keys(nextProps.items).length
    this.setState({
      previousResultsLength: currResultsLength,
    })
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      items,
      loading,
      loadingMessage,
      resultsMessage,
      shown,
      total,
      onItemSelect,
      ListItemComponent,
      GridItemComponent,
      propsForItem,
    } = this.props

    const listInfo = (
      <h1 style={styleListInfo}>
        {loading ? loadingMessage ? (
          loadingMessage
        ) : (
          'Loading...'
        ) : (
          `${resultsMessage
            ? resultsMessage
            : 'Results'} (showing ${shown} of ${total})`
        )}
      </h1>
    )

    const toggleAvailable = ListItemComponent && GridItemComponent

    let controlElement = null
    if (toggleAvailable) {
      controlElement = (
        <div style={styleListControl}>
          <Button
            text={this.state.showAsGrid ? 'Show List' : 'Show Grid'}
            icon={this.state.showAsGrid ? listIcon : gridIcon}
            styleIcon={styleControlButtonIcon}
            onClick={this.toggleShowAsGrid}
          />
        </div>
      )
    }

    let itemElements = []
    this.focusItem = null
    _.forOwn(items, (item, key) => {
      const isNextFocus =
        this.state.previousResultsLength > 0 &&
        this.state.previousResultsLength == itemElements.length

      const styleFocused = {
        ...(this.state.focusing ? styleFocusDefault : {}),
      }

      const styleOverallItemApplied = {
        ...styleFallbackItem,
        ...styleFocused,
      }
      let itemElement = (
        <div
          key={key}
          tabIndex={-1}
          ref={item => {
            if (isNextFocus) {
              this.focusItem = item
            }
          }}
          style={styleOverallItemApplied}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        >
          {key}
        </div>
      )

      const itemProps = propsForItem ? propsForItem(item, key) : null

      if (this.state.showAsGrid && GridItemComponent) {
        itemElement = (
          <GridItemComponent
            item={item}
            key={key}
            onClick={() => onItemSelect(key)}
            shouldFocus={isNextFocus}
            {...itemProps}
          />
        )
      }
      else if (!this.state.showAsGrid && ListItemComponent) {
        itemElement = (
          <ListItemComponent
            itemId={key}
            item={item}
            key={key}
            onClick={() => onItemSelect(key)}
            shouldFocus={isNextFocus}
            {...itemProps}
          />
        )
      }
      itemElements.push(itemElement)
    })

    return (
      <div style={styleListView}>
        {listInfo}
        {controlElement}
        <div style={this.state.showAsGrid ? styleGrid : styleList}>
          {itemElements}
        </div>
      </div>
    )

    return <div>{itemElements}</div>
  }
}

ListView.propTypes = {
  items: PropTypes.object,
  loading: PropTypes.bool,
  shown: PropTypes.number,
  total: PropTypes.number,
  showAsGrid: PropTypes.bool,
  onItemsSelect: PropTypes.func,
  ListComponent: PropTypes.func,
  GridComponent: PropTypes.func,
  propsForItem: PropTypes.func,
}
