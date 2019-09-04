import React from 'react'
import PropTypes from 'prop-types'
import Button from '../../common/input/Button'
import ListView from '../../common/ui/ListView'
import GranuleListResultContainer from './GranuleListResultContainer'
import {SiteColors} from '../../../style/defaultStyles'
import Meta from '../../helmet/Meta'
import _ from 'lodash'
import cartIcon from '../../../../img/font-awesome/white/svg/shopping-cart.svg'
import {FEATURE_CART} from '../../../utils/featureUtils'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleGranuleListWrapper = {
  maxWidth: '80em',
  width: '80em',
  color: '#222',
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}
const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

const styleAddFilteredGranulesToCartButton = {
  flexShrink: 0,
  height: 'fit-content',
}

const styleAddFilteredGranulesToCartButtonIcon = {
  width: '1em',
  height: '1em',
}

const styleAddFilteredGranulesToCartButtonText = {
  paddingRight: '0.309em',
}

const styleAddFilteredGranulesToCartButtonFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

const styleWarning = warning => {
  if (_.isEmpty(warning)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      backgroundColor: SiteColors.WARNING,
      color: 'white',
      fontWeight: 'bold',
      fontSize: '1.15em',
      margin: '0 1.618em 1em 0',
      padding: '0.618em',
      display: 'flex',
      justifyContent: 'center',
      borderRadius: '0.309em',
    }
  }
}

export default class GranuleList extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      hovering: false,
    }
  }

  isGranuleSelected = itemId => {
    const {selectedGranules} = this.props
    const checkIt = Object.keys(selectedGranules).includes(itemId)
    return checkIt
  }

  handleCheckboxChange = (itemId, item) => {
    const {selectGranule, deselectGranule} = this.props
    return checkbox => {
      if (checkbox.checked) {
        selectGranule(item, itemId)
      }
      else {
        deselectGranule(itemId)
      }
    }
  }

  propsForResult = (item, itemId) => {
    const {featuresEnabled} = this.props

    return {
      showLinks: true,
      showTimeAndSpace: true,
      handleCheckboxChange: this.handleCheckboxChange,
      checkGranule: this.isGranuleSelected(itemId),
      featuresEnabled: featuresEnabled,
    }
  }

  render() {
    const {
      results,
      returnedHits,
      totalHits,
      selectCollection,
      fetchMoreResults,
      addFilteredGranulesToCart,
      addFilteredGranulesToCartWarning,
      collectionTitle,
      granuleFilter,
      featuresEnabled,
    } = this.props

    const {hovering} = this.state

    const showMoreButton =
      returnedHits < totalHits ? (
        <Button
          text="Show More Results"
          onClick={() => fetchMoreResults()}
          style={styleShowMore}
          styleFocus={styleShowMoreFocus}
        />
      ) : null

    // custom control for list view
    let customButtons = []

    if (featuresEnabled.includes(FEATURE_CART)) {
      customButtons.push(
        <Button
          key={'addMatchingToCartButton'}
          icon={cartIcon}
          iconAfter={true}
          styleIcon={styleAddFilteredGranulesToCartButtonIcon}
          text="Add Matching to Cart"
          onClick={() => addFilteredGranulesToCart(granuleFilter)}
          style={styleAddFilteredGranulesToCartButton}
          styleFocus={styleAddFilteredGranulesToCartButtonFocus}
          styleText={styleAddFilteredGranulesToCartButtonText}
        />
      )
    }

    let customMessage = addFilteredGranulesToCartWarning ? (
      <div
        key="GranuleList::Warning"
        style={styleWarning(addFilteredGranulesToCartWarning)}
        role="alert"
      >
        {addFilteredGranulesToCartWarning}
      </div>
    ) : null

    return (
      <div style={styleCenterContent}>
        <Meta
          title={`Files in Collection ${collectionTitle}`}
          formatTitle={true}
          robots="noindex"
        />

        <div style={styleGranuleListWrapper}>
          <ListView
            items={results}
            resultType="matching files"
            shown={returnedHits}
            total={totalHits}
            onItemSelect={selectCollection}
            ListItemComponent={GranuleListResultContainer}
            GridItemComponent={null}
            propsForItem={this.propsForResult}
            customButtons={customButtons}
            customMessage={customMessage}
          />
          {showMoreButton}
        </div>
      </div>
    )
  }
}

GranuleList.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
}
