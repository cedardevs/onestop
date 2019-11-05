import React from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {fontFamilySerif} from '../../../utils/styleUtils'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import GranuleItemContainer from './GranuleItemContainer'

const styleTitle = focusing => {
  const styleTitleFocusing = {
    textDecoration: 'underline',
    outline: focusing ? '2px dashed black' : 'none',
    outlineOffset: focusing ? '0.309em' : 'initial',
  }

  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    fontWeight: 'bold',
    overflowWrap: 'break-word',
    wordWrap: 'break-word',
    margin: '0 1.236em 0 0',
    ...(focusing ? styleTitleFocusing : {}),
  }
}

const styleHeading = {
  padding: 0,
}

export default function GranuleListItem(props){
  const {
    itemId,
    item,
    focusRef,
    focusing,
    handleFocus,
    handleBlur,
    expanded,
    setExpanded,
  } = useListViewItem(props)

  // TODO: the show more focus is not working here like it does with the collections and cart, why?
  const title = (
    <h3
      key={'GranuleListItem::title'}
      style={styleTitle(focusing)}
      tabIndex={-1}
      ref={focusRef}
      onFocus={handleFocus}
      onBlur={handleBlur}
    >
      {item.title}
    </h3>
  )

  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title ]} />
    </div>
  )

  const content = (
    <GranuleItemContainer
      itemId={itemId}
      item={item}
      checkGranule={props.checkGranule}
      handleCheckboxChange={props.handleCheckboxChange}
      showAccessLinks={true}
      showVideos={true}
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
    />
  )
}
