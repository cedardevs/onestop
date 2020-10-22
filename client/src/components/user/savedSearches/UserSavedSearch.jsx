import React, {useState, useEffect} from 'react'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexRow from '../../common/ui/FlexRow'
import FlexColumn from '../../common/ui/FlexColumn'
import {decodePathAndQueryString} from '../../../utils/queryUtils'
import UserSavedSearchAppliedFilters from './UserSavedSearchAppliedFilters'
import Button from '../../common/input/Button'
import linkIcon from 'fa/arrow-right.svg'
import trashIcon from 'fa/trash.svg'

const styleTitle = {
  fontFamily: fontFamilySerif(),
  // fontSize: '2em',
  fontWeight: 'bold',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
  margin: '0 1.236em 0 0',
}

const styleIcon = {
  width: '1.309em',
  height: '1.309em',
}

const styleButton = {
  padding: '0.309em',
  margin: '0.105em',
  borderRadius: '0.309em',
  fontSize: '1em',
}

const styleButtonFocus = {
  outline: '2px dashed black',
  outlineOffset: '2px',
}

const styleHeading = {
  padding: 0,
  justifyContent: 'space-between',
}

const styleSavedSearch = {
  // background: 'green',
  display: 'flex',
  marginBottom: '0.309em',
}

const styleButtonFunc = color => {
  return {
    fontSize: '1em',
    padding: '0.309em',
    margin: '0.105em',
    borderRadius: '0.309em',
    background: color,
  }
}

const styleButtonHover = color => {
  return {
    background: `linear-gradient(black, ${color})`,
  }
}

const styleButtonFocusFunc = color => {
  return {
    background: `linear-gradient(black, ${color})`,
    outline: '2px dashed black',
    outlineOffset: '2px',
  }
}

const UserSavedSearch = props => {
  const {itemId, item, expanded, setExpanded} = useListViewItem(props)
  const {navigateToSearch, deleteSearch} = props
  const url = item.attributes.value
  const name = item.attributes.name
    ? item.attributes.name
    : item.attributes.value
  const [ decodedSavedSearch, setDecodedSavedSearch ] = useState({
    id: '',
    filters: {},
  })
  useEffect(
    () => {
      setDecodedSavedSearch(decodePathAndQueryString('', queryString)) // TODO the use of an empty string for the first param only works for collection searches - it will definitely break for granules
    },
    [ queryString ]
  )

  // TODO this isn't the best way to construct URL, for consistency - eg in Chrome it opens a new tab, etc
  const title = (
    <h3 key={'UserSavedSearch::title'} style={styleTitle}>
      {/*{name}*/}
      <a href={url}>{name}</a>
    </h3>
  )

  const navigateToButton = (
    <Button
      key="navigateTo"
      title={`navigate to ${item.attributes.name} search`}
      icon={linkIcon}
      style={styleButton}
      styleIcon={styleIcon}
      styleFocus={styleButtonFocus}
      iconPadding={'0.309em'}
      onClick={() => {
        navigateToSearch(decodedSavedSearch.filters) // TODO this also will only work for collections, as written
      }}
    />
  )

  const styleDeleteButton = styleButtonFunc('#851A11')
  const styleDeleteButtonHover = styleButtonHover('#851A11')
  const styleDeleteButtonFocus = styleButtonFocusFunc('#851A11')
  const deleteSearchButton = (
    <Button
      key="delete"
      title={`delete ${item.attributes.name} search`}
      icon={trashIcon}
      styleIcon={styleIcon}
      style={styleDeleteButton}
      styleHover={styleDeleteButtonHover}
      styleFocus={styleDeleteButtonFocus}
      iconPadding={'0.309em'}
      onClick={() => {
        deleteSearch(itemId)
      }}
    />
  )

  const actionButtons = (
    <div>
      <FlexColumn items={[ navigateToButton, deleteSearchButton ]} />
    </div>
  )

  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title ]} />
    </div>
  )

  const queryStringIndex = url.indexOf('?')
  const queryString = url.slice(queryStringIndex)

  const content = (
    <div style={styleSavedSearch}>
      {/* <ul>
        <li>Saved Search ID: {itemId}</li>
        <li>Name: {item.name}</li>
        <li>URL: {item.url}</li>

      </ul> */}
      {/*{navigateToButton}*/}
      <UserSavedSearchAppliedFilters
        collectionFilter={decodedSavedSearch.filters}
      />
    </div>
  )

  // TODO compare this item structure to the cart one - bet it's missing something, hence the expand thing being wrong
  return (
    <ListViewItem
      itemId={itemId}
      item={{title: item.attributes.name}}
      heading={heading}
      content={content}
      expanded={expanded}
      setExpanded={setExpanded}
      actions={actionButtons}
    />
  )
}

export default UserSavedSearch
