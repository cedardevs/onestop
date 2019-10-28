import React, {useState, useEffect} from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexColumn from '../../common/ui/FlexColumn'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import TimeSummary from '../../collections/detail/TimeSummary'
import SpatialSummary from '../../collections/detail/SpatialSummary'
import ResultGraphic from '../ResultGraphic'
import {Key} from '../../../utils/keyboardUtils'
import {SiteColors} from '../../../style/defaultStyles'

const pattern = require('../../../../img/topography.png')

const styleTitle = expanded => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    fontWeight: expanded ? 'bold' : 'normal',
    overflowWrap: 'break-word',
    wordWrap: 'break-word',
    margin: '0 1.236em 0 0',
  }
}

const styleLink = focusing => {
  return {
    color: SiteColors.LINK,
    textDecoration: 'underline',
    outline: focusing ? '2px dashed black' : 'none',
    outlineOffset: focusing ? '0.309em' : 'initial',
  }
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

const useFocus = (ref, defaultState = false) => {
  const [ state, setState ] = useState(defaultState)

  if (!ref.current) {
    return false
  }

  useEffect(() => {
    const onFocus = () => setState(true)
    const onBlur = () => setState(false)
    ref.current.addEventListener('focus', onFocus)
    ref.current.addEventListener('blur', onBlur)

    return () => {
      ref.current.removeEventListener('focus', onFocus)
      ref.current.removeEventListener('blur', onBlur)
    }
  }, [])

  return state
}

const CollectionListItem = React.forwardRef((props, ref) => {
  const [ focusingLink, setFocusingLink ] = useState(false)
  const [ itemId, item, onSelect, expanded, setExpanded ] = useListViewItem(
    props
  )

  console.log(`itemId=${itemId}, ref=${ref}`)
  console.log(ref)

  const focusingItem = useFocus(ref)

  const handleKeyDown = event => {
    if (event.keyCode === Key.SPACE) {
      event.preventDefault() // prevent scrolling down on space press
      onSelect(itemId)
    }
    if (event.keyCode === Key.ENTER) {
      onSelect(itemId)
    }
  }

  const title = (
    <h3 key={'CollectionListItem::title'} style={styleTitle(expanded)}>
      <span>{focusingItem ? 'FOCUSING: ' : ''}</span>
      <a
        style={styleLink(focusingLink)}
        tabIndex={0}
        onClick={() => onSelect(itemId)}
        onKeyDown={handleKeyDown}
        onFocus={() => setFocusingLink(true)}
        onBlur={() => setFocusingLink(false)}
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
})

export default CollectionListItem
