import React from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexColumn from '../../common/ui/FlexColumn'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import TimeSummary from '../../collections/detail/TimeSummary'
import SpatialSummary from '../../collections/detail/SpatialSummary'
import ResultGraphic from '../ResultGraphic'
import {SiteColors} from '../../../style/defaultStyles'

const pattern = require('../../../../img/topography.png')

const styleTitle = focusing => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    fontWeight: 'bold',
    overflowWrap: 'break-word',
    wordWrap: 'break-word',
    margin: '0 1.236em 0 0',

    outline: focusing ? '2px dashed black' : 'none',
    outlineOffset: focusing ? '0.309em' : 'initial',
  }
}

const styleLink = {
  color: SiteColors.LINK,
  textDecoration: 'underline',
  outline: 'none',
}

const styleHeading = {
  padding: 0,
}

const styleLeft = {
  flex: '1 1 auto',
  width: '38.2%',
  background: `url(${pattern}) repeat`,
  backgroundSize: '30em',
  justifyContent: 'center',
}

const styleRight = {
  flex: '1 1 auto',
  width: '61.8%',
  marginLeft: '1.618em',
}

const styleLeftRightFlexRow = {
  flexDirection: 'row-reverse',
}

const styleContent = {
  padding: '0.618em',
}

const styleContentHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  marginTop: '1em',
  marginBottom: '0.25em',
  fontWeight: 'bold',
}

const styleContentHeadingTop = {
  ...styleContentHeading,
  marginTop: '0em',
}

const CollectionListItem = props => {
  const {
    itemId,
    item,
    focusing,
    handleFocus,
    handleBlur,
    handleSelect,
    handleKeyDown,
    expanded,
    setExpanded,
  } = useListViewItem(props)

  const title = (
    <h3 key={'CollectionListItem::title'} style={styleTitle(focusing)}>
      <a
        role="link"
        style={styleLink}
        onFocus={handleFocus}
        onBlur={handleBlur}
        onClick={handleSelect}
        onKeyDown={handleKeyDown}
      >
        {item.title}
      </a>
    </h3>
  )
  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title ]} />
    </div>
  )

  const timePeriod = (
    <div key={'CollectionListItem::timePeriod'}>
      <h4 style={styleContentHeadingTop}>Time Period:</h4>
      <TimeSummary item={item} />
    </div>
  )

  const boundingCoordinates = (
    <div key={'CollectionListItem::boundingCoordinates'}>
      <h4 style={styleContentHeading}>Bounding Coordinates:</h4>
      <SpatialSummary item={item} />
    </div>
  )
  const left = (
    <FlexColumn
      key={'CollectionListItem::left'}
      style={styleLeft}
      items={[
        <ResultGraphic
          key={'ResultGraphic'}
          thumbnail={item.thumbnail}
          geometry={item.spatialBounding}
        />,
      ]}
    />
  )

  const right = (
    <FlexColumn
      key={'CollectionListItem::right'}
      style={styleRight}
      items={[ timePeriod, boundingCoordinates ]}
    />
  )

  const content = (
    <div style={styleContent}>
      <FlexRow items={[ right, left ]} style={styleLeftRightFlexRow} />
    </div>
  )

  return (
    <ListViewItem
      itemId={itemId}
      item={item}
      heading={heading}
      content={content}
      expanded={expanded}
      setExpanded={setExpanded}
    />
  )
}

export default CollectionListItem
