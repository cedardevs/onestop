import React from 'react'
import ListViewItem, {useListViewItem} from '../../common/ui/ListViewItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexRow from '../../common/ui/FlexRow'
import {decodePathAndQueryString} from '../../../utils/queryUtils'
import UserSavedSearchAppliedFilters from './UserSavedSearchAppliedFilters'
import Button from "../../common/input/Button";
import linkIcon from 'fa/arrow-right.svg'
import trashIcon from 'fa/trash.svg'

const styleTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '2em',
  fontWeight: 'bold',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
  margin: '0 1.236em 0 0',
}

const styleButton = {
  padding: '0.309em',
  margin: '0.309em',
  borderRadius: '0.309em',
}

const styleHeading = {
  padding: 0,
  justifyContent: 'space-between'
}

const styleSavedSearch = {
  // background: 'green',
  display: 'flex',
  marginBottom: '0.309em',
}

const UserSavedSearch = props => {
  const {itemId, item, expanded, setExpanded} = useListViewItem(props)
  const {navigateToSearch, deleteSearch} = props
  const url = item.value
  const name = item.name ? item.name : item.value

  const title = (
    <h3 key={'UserSavedSearch::title'} style={styleTitle}>
      {/*{name}*/}
      <a href={url}>{name}</a>
    </h3>
  )

  const navigateToAction =  [
    {
      text: "navigateTo",
      title: "navigateTo",
      icon: linkIcon,
      showText: false,
      handler: () => {navigateToSearch(JSON.parse(item.filter))},
      notification: "notification",
    }
  ]
  const navigateToButton = <Button
      key="navigateTo"
      title="navigate to"
      icon={linkIcon}
      style={styleButton}
      // styleIcon={styleIcon}
      // iconPadding={'0.309em'}
      onClick={() => {navigateToSearch(JSON.parse(item.filter))}}
  />

  const deleteSearchButton = <Button
      key="delete"
      title="delete"
      icon={trashIcon}
      style={styleButton}
      // styleIcon={styleIcon}
      // iconPadding={'0.309em'}
      onClick={() => {deleteSearch(itemId)}}
  />

  const actionButtons = (<div>
    <FlexRow items={[ deleteSearchButton, navigateToButton ]} />
  </div>)

  const heading = (
    <div style={styleHeading}>
      <FlexRow items={[ title ]} />
    </div>
  )

  const queryStringIndex = url.indexOf('?')
  const queryString = url.slice(queryStringIndex)
  const decodedSavedSearch = decodePathAndQueryString('', queryString)
  // console.log('decodedSavedSearch', decodedSavedSearch)

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

  return (
    <ListViewItem
      itemId={itemId}
      item={item}
      heading={heading}
      content={content}
      expanded={expanded}
      setExpanded={setExpanded}
      actions={actionButtons}
    />
  )
}

export default UserSavedSearch
