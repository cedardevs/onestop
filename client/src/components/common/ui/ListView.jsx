import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import _ from 'lodash'
import Button from '../input/Button'
import gridIcon from 'fa/th.svg'
import listIcon from 'fa/th-list.svg'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexRow from './FlexRow'

const styleListView = {
  marginLeft: '1.618em',
}

const styleTopRow = {
  justifyContent: 'space-between',
  alignItems: 'center',
  margin: '0 1.618em 0 0',
}

const styleListInfo = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  padding: 0,
  margin: '0 0 0.618em',
}

const styleListControl = {
  display: 'flex',
  justifyContent: 'space-around',
  padding: '0.618em',
  backgroundColor: 'rgba(0,0,0, 0.2)',
  borderRadius: '0.309em',
  margin: '0 1.618em 1em 0',
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

export default class ListView extends React.Component {
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
      resultType,
      resultsMessage,
      resultsMessageEmpty,
      searchTerms,
      shown,
      total,
      onItemSelect,
      ListItemComponent,
      GridItemComponent,
      propsForItem,
      customControl,
      customButtons,
      customMessage,
    } = this.props

    let countMessage = `Showing ${shown.toLocaleString()} of ${total.toLocaleString()} ${resultType
      ? resultType
      : 'results'} ${searchTerms ? 'for "' + searchTerms + '"' : ''}`

    let message = `${resultsMessage ? resultsMessage : countMessage}`

    if (total === 0) {
      message = resultsMessageEmpty ? resultsMessageEmpty : 'No Results'
    }

    const listInfo = (
      <div key="list-view-info">
        <h2 style={styleListInfo}>{message}</h2>
      </div>
    )

    // initialize vars for control elements
    let controlElement = null
    let buttons = []

    // if both list and grid components are provided,
    // we can show a toggle between views
    const toggleAvailable = ListItemComponent && GridItemComponent
    if (toggleAvailable) {
      buttons.push(
        <Button
          text={this.state.showAsGrid ? 'Show List' : 'Show Grid'}
          icon={this.state.showAsGrid ? listIcon : gridIcon}
          styleIcon={styleControlButtonIcon}
          onClick={this.toggleShowAsGrid}
        />
      )
    }

    // add any provided custom buttons to the control element
    const customButtonsAvailable = customButtons && customButtons.length > 0
    if (customButtonsAvailable) {
      buttons = buttons.concat(customButtons)
    }

    // if any control buttons are available to show, show them
    if (buttons.length > 0) {
      controlElement = <div style={styleListControl}>{buttons}</div>
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
        <FlexRow style={styleTopRow} items={[ listInfo, customControl ]} />
        {controlElement}
        {customMessage}
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
  shown: PropTypes.number,
  total: PropTypes.number,
  showAsGrid: PropTypes.bool,
  onItemsSelect: PropTypes.func,
  ListComponent: PropTypes.func,
  GridComponent: PropTypes.func,
  propsForItem: PropTypes.func,
}
