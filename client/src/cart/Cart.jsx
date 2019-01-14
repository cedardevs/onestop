import React from 'react'
import Meta from 'react-helmet'
import ListView from '../common/ListView'
import CartItem from './CartItem'
import Button from '../common/input/Button'
import {boxShadow} from '../common/defaultStyles'
import {identifyProtocol} from '../utils/resultUtils'
import cancel from 'fa/ban.svg'
import {fontFamilySerif} from '../utils/styleUtils'
import FlexColumn from '../common/FlexColumn'
import ScriptDownloader from './ScriptDownloader'
import FlexRow from '../common/FlexRow'

const SHOW_MORE_INCREMENT = 10

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleCartListWrapper = {
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  marginRight: '3px',
  marginLeft: '1px',
  paddingTop: '1.618em',
  paddingBottom: '1.618em',
  backgroundColor: 'white',
  color: '#222',
}

const styleCartActions = {
  margin: '0 0 1.618em 1.618em',
}

const styleCartActionsTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  margin: '0 1.618em 0.618em 0',
  padding: 0,
}

const styleActionButtons = {
  justifyContent: 'space-between',
  marginRight: '1.618em',
}

const styleClearCartButton = {
  fontSize: '1em',
  display: 'inline-flex',
  padding: '0.309em 0.618em 0.309em 0.309em',
}

const styleClearCartIcon = {
  width: '1.618em',
  height: '1.618em',
  marginRight: '0.309em',
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}
const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

export default class Cart extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      numShownItems:
        props.numberOfGranulesSelected < SHOW_MORE_INCREMENT
          ? props.numberOfGranulesSelected
          : SHOW_MORE_INCREMENT,
    }
    this.props = props
  }

  // handleExpandableToggle = event => {
  //   // prevent focus-change state from disrupting if each expandable is open
  //   let toggledElement = event.value
  //   this.setState({
  //     [toggledElement]: event.open,
  //   })
  // }

  propsForResult = (item, itemId) => {
    const {deselectGranule} = this.props
    let resultProps = {}
    return {deselectGranule: deselectGranule}
  }

  handleSelectItem = e => {}

  handleShowMore = () => {
    const {numberOfGranulesSelected} = this.props
    const {numShownItems} = this.state
    if (numShownItems < numberOfGranulesSelected) {
      const nextNumShownItems =
        numShownItems + SHOW_MORE_INCREMENT > numberOfGranulesSelected
          ? numberOfGranulesSelected
          : numShownItems + SHOW_MORE_INCREMENT

      this.setState(prevState => {
        return {
          ...prevState,
          numShownItems: nextNumShownItems,
        }
      })
    }
  }

  render() {
    const {
      loading,
      selectedGranules,
      numberOfGranulesSelected,
      deselectAllGranules,
    } = this.props
    const {numShownItems} = this.state
    const selectedGranulesCount = Object.keys(selectedGranules).length
    const shownGranules =
      selectedGranulesCount < numShownItems
        ? selectedGranulesCount
        : numShownItems
    // keep track of used protocols in results to avoid unnecessary legend keys
    const usedProtocols = new Set()

    for (let key in selectedGranules) {
      if (selectedGranules.hasOwnProperty(key)) {
        const value = selectedGranules[key]
        _.forEach(value.links, link => {
          // if(link.linkFunction.toLowerCase() === 'download' || link.linkFunction.toLowerCase() === 'fileaccess') {
          return usedProtocols.add(identifyProtocol(link))
          // }
        })
      }
    }

    const showMoreButton =
      numShownItems < numberOfGranulesSelected ? (
        <Button
          text="Show More"
          onClick={this.handleShowMore}
          style={styleShowMore}
          styleFocus={styleShowMoreFocus}
        />
      ) : null

    const cartActions = [
      <ScriptDownloader
        key="scriptDownloaderButton"
        selectedGranules={selectedGranules}
      />,
      <FlexRow
        key={'cartActionButtons'}
        style={{marginTop: '1em'}}
        items={[
          <Button
            key={'clearCartButton'}
            style={styleClearCartButton}
            title={'Clear cart'}
            text={'Clear cart'}
            icon={cancel}
            styleIcon={styleClearCartIcon}
            onClick={deselectAllGranules}
          />,
        ]}
      />,
    ]

    const cartActionsWrapper =
      selectedGranulesCount === 0 ? null : (
        <div style={styleCartActions}>
          <h1 style={styleCartActionsTitle}>Cart Actions</h1>
          <FlexColumn style={styleActionButtons} items={cartActions} />
        </div>
      )

    return (
      <div style={styleCenterContent}>
        <Meta title="File Access Cart" robots="noindex" />

        <div style={styleCartListWrapper}>
          {cartActionsWrapper}
          <ListView
            items={selectedGranules}
            loading={!!loading}
            resultsMessage={'Files for download'}
            shown={shownGranules}
            total={selectedGranulesCount}
            // total={numberOfGranulesSelected}
            onItemSelect={this.handleSelectItem}
            ListItemComponent={CartItem}
            GridItemComponent={null}
            propsForItem={this.propsForResult}
          />
          {showMoreButton}
        </div>
      </div>
    )
  }
}
