import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import Checkbox from '../common/input/Checkbox'

const styleListView = {}

const styleListInfo = {
  fontSize: '1.2em',
  margin: 0,
  padding: '0em 2em 1.618em 2em',
}

const styleListControl = {
  margin: 0,
  padding: '0em 2em 1.618em 2em',
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

const styleList = {
  display: 'flex',
  flexDirection: 'column',
  flexWrap: 'nowrap',
  margin: '0 0 0 2em',
}

const styleFallbackItem = {
  display: 'block',
}

export default class ListView extends Component {
  constructor(props) {
    super(props)
    this.state = {showAsGrid: true}
  }

  toggleShowAsGrid = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showAsGrid: event.checked,
      }
    })
  }

  render() {
    const {
      items,
      loading,
      shown,
      total,
      enableGridToggle,
      onItemSelect,
      ListItemComponent,
      GridItemComponent,
      propsForItem
    } = this.props

    const listInfo = (
      <h1 style={styleListInfo}>
        {loading ? (
          'Loading...'
        ) : (
          `Search Results (showing ${shown} of ${total})`
        )}
      </h1>
    )

    let toggleElement = null
    if (enableGridToggle) {
      toggleElement = (
        <div style={styleListControl}>
          <Checkbox
            label="Show as grid."
            checked={this.state.showAsGrid}
            onChange={this.toggleShowAsGrid}
          />
        </div>
      )
    }

    let itemElements = []
    _.forOwn(items, (item, key) => {
      let itemElement = (
        <div key={key} style={styleFallbackItem}>
          {key}
        </div>
      )

      const itemProps = propsForItem ? propsForItem(item) : null

      if (this.state.showAsGrid && GridItemComponent) {
        itemElement = (
          <GridItemComponent
            item={item}
            key={key}
            onClick={() => onItemSelect(key)}
            {...itemProps}
          />
        )
      }
      else if (!this.state.showAsGrid && ListItemComponent) {
        itemElement = (
          <ListItemComponent
            item={item}
            key={key}
            onClick={() => onItemSelect(key)}
            {...itemProps}
          />
        )
      }
      itemElements.push(itemElement)
    })

    return (
      <div style={styleListView}>
        {listInfo}
        {toggleElement}
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
  propsForItem: PropTypes.func
}
