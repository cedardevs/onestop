import React, {useState} from 'react'
import FlexRow from '../common/ui/FlexRow'
import {fontFamilySerif} from '../../utils/styleUtils'
import FlexColumn from '../common/ui/FlexColumn'
import {SiteColors} from '../../style/defaultStyles'
import ListViewItem from '../common/ui/ListViewItem'
import TimeSummary from '../collections/detail/TimeSummary'
import SpatialSummary from '../collections/detail/SpatialSummary'
import ResultGraphic from '../results/ResultGraphic'
import ResultAccessLinks from '../results/ResultAccessLinks'
import {useListViewItem} from '../common/ui/ListViewItem'
import CartListItemActions from './CartListItemActions'
const pattern = require('../../../img/topography.png')

const styleTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  fontWeight: 'bold',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
  margin: '0 1.236em 0 0',
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

export default function CartListItem(props){
  const {itemId, item, expanded, setExpanded} = useListViewItem(props)

  const title = (
    <h3 key={'CartListItem::title'} style={styleTitle}>
      {item.title}
    </h3>
  )

  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title ]} />
    </div>
  )

  const accessLinks = (
    <div key={'CartListItem::accessLinks'}>
      <h4 style={styleContentHeadingTop}>Data Access Links:</h4>
      <ResultAccessLinks itemId={itemId} item={item} />
    </div>
  )

  const timePeriod = (
    <div key={'CartListItem::timePeriod'}>
      <h4 style={styleContentHeading}>Time Period:</h4>
      <TimeSummary item={item} />
    </div>
  )

  const boundingCoordinates = (
    <div key={'CartListItem::boundingCoordinates'}>
      <h4 style={styleContentHeading}>Bounding Coordinates:</h4>
      <SpatialSummary item={item} />
    </div>
  )

  const left = (
    <FlexColumn
      key={'CartListItem::left'}
      style={styleLeft}
      items={[
        <ResultGraphic
          key={'ResultGraphic'}
          thumbnail={item.thumbnail}
          geometry={item.spatialBounding}
          height={'16em'}
        />,
      ]}
    />
  )

  const right = (
    <FlexColumn
      key={'CartListItem::right'}
      style={styleRight}
      items={[ accessLinks, timePeriod, boundingCoordinates ]}
    />
  )

  const content = (
    <div style={styleContent}>
      <FlexRow items={[ right, left ]} style={styleLeftRightFlexRow} />
    </div>
  )

  const {deselectGranule} = props
  const actions = (
    <CartListItemActions
      key={'CartListItem::actions'}
      expanded={expanded}
      item={item}
      itemId={itemId}
      deselectGranule={deselectGranule}
    />
  )

  return (
    <ListViewItem
      itemId={itemId}
      item={item}
      heading={heading}
      content={content}
      expanded={expanded}
      setExpanded={setExpanded}
      actions={actions}
    />
  )
}
