import React from 'react'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexRow from '../../common/ui/FlexRow'
import {decodePathAndQueryString} from '../../../utils/queryUtils'
import UserSavedSearchAppliedFilters from './UserSavedSearchAppliedFilters'

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

const styleSavedSearch = {
  // background: 'green',
  display: 'flex',
  marginBottom: '0.309em',
}

const UserSavedSearch = props => {
  const {itemId, item, expanded, setExpanded} = useListViewItem(props)

  const url = item.value
  const name = item.name ? item.name : item.value

  const title = (
    <h3 key={'UserSavedSearch::title'} style={styleTitle}>
      <a href={url}>{name}</a>
    </h3>
  )

  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title, ': Collection' ]} />
    </div>
  )

  const queryStringIndex = url.indexOf('?')
  const queryString = url.slice(queryStringIndex)
  const decodedSavedSearch = decodePathAndQueryString('', queryString)
  console.log('decodedSavedSearch', decodedSavedSearch)

  const content = (
    <div style={styleSavedSearch}>
      {/* <ul>
        <li>Saved Search ID: {itemId}</li>
        <li>Name: {item.name}</li>
        <li>URL: {item.url}</li>

      </ul> */}
      <UserSavedSearchAppliedFilters
        collectionFilter={decodedSavedSearch.filters}
      />
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

export default UserSavedSearch
