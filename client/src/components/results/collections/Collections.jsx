import React, {useState, useEffect, useCallback} from 'react'
import PropTypes from 'prop-types'
import CollectionGridItem from './CollectionGridItem'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import CollectionListItem from './CollectionListItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {asterisk, SvgIcon} from '../../common/SvgIcon'
import {encodeQueryString, PAGE_SIZE} from '../../../utils/queryUtils'
import defaultStyles, {FilterColors} from '../../../style/defaultStyles'
import saveIcon from 'fa/bookmark-o.svg'
import alreadySavedIcon from 'fa/bookmark.svg'
import ModalFormUncontrolled from '../../common/dialog/ModalFormUncontrolled'
import {useDisclosure} from '@chakra-ui/react'

const styleCollections = {
  color: '#222',
}

const styleListHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
}

/*const styleShowMore = {
  margin: '1em auto 1.618em auto',
}

const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}*/

const styleTextInput = {
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
  width: '10em',
}

export default function Collections(props){
  const {
    searchTerms,
    results,
    totalHits,
    fetchResultPage,
    selectCollection,
    collectionDetailFilter,
    loading,
    saveSearch, //action
    deleteSearch, //action
    savedSearchUrl,
    isAuthenticatedUser,
    savedSearches,
    collectionFilter,
  } = props

  const queryText = props.collectionDetailFilter.queryText
  const [ savedId, setSavedId ] = useState(null)
  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)
  const [ headingMessage, setHeadingMessage ] = useState(null)
  //used to toggle bookmark button highlight
  const [ , setSearchSaved ] = useState(false)
  //the element containing the bookmark button
  const [ bookmarkButton, setBookmark ] = useState(null)
  const {isOpen, onOpen, onClose} = useDisclosure()

  useEffect(
    () => {
      let value = findSavedId()
      setSavedId(value)
      setSearchSaved(!!value)
    },
    [ savedSearches, collectionFilter ]
  )
  function handleSave(name){
    const urlToSave = window.location.pathname + window.location.search
    // const queryStringIndex = urlToSave.indexOf('?')
    // const queryString = urlToSave.slice(queryStringIndex)
    // const decodedSavedSearch = decodePathAndQueryString('', queryString)
    saveSearch(savedSearchUrl, urlToSave, name, collectionFilter)
  }

  function handleDelete(id){
    deleteSearch(savedSearchUrl, id)
  }

  function setBookmarkButton(){
    const savedId = findSavedId()

    //if we found a matching id, the search was saved previously
    const saveSearchAction = savedId
      ? [
          {
            text: 'Delete',
            title: 'Delete search',
            icon: alreadySavedIcon,
            showText: false,
            handler: () => {
              onOpen()
            },
            notification: 'Delete',
          },
        ]
      : [
          {
            text: 'Save',
            title: 'Save search',
            icon: saveIcon,
            showText: false,
            handler: () => {
              onOpen()
            },
            notification: 'Save',
          },
        ]

    setBookmark(saveSearchAction)
  }

  const findSavedId = useCallback(
    () => {
      for (const [ key, value ] of Object.entries(savedSearches)) {
        // TODO could probably just grab the url directly instead of encoding
        // TODO shouldn't need to split, once we update what we save in the DB
        if (
          encodeQueryString(collectionFilter) ===
          value.attributes.value.split('?')[1]
        ) {
          return key
        }
      }
      return null
    },
    [ savedSearches, collectionFilter ]
  )

  useEffect(
    () => {
      if (isAuthenticatedUser) {
        setBookmarkButton()
      }

      if (loading) {
        setHeadingMessage(
          <span>
            <SvgIcon
              style={{fill: 'white', animation: 'rotation 2s infinite linear'}}
              path={asterisk}
              size=".9em"
              verticalAlign="unset"
            />&nbsp;Loading collections
          </span>
        )
      }
      else if (totalHits > 0) {
        const size = Object.keys(results).length
        const thru = (
          <span>
            <span aria-hidden="true">-</span>
            <span style={defaultStyles.hideOffscreen}>to</span>
          </span>
        )
        setHeadingMessage(
          <span>
            <span>Showing {offset + 1} </span>
            {thru}{' '}
            <span>
              {offset + size} of {totalHits.toLocaleString()} collection results
              matching '{searchTerms}'
            </span>
          </span>
        )
      }
      else {
        setHeadingMessage(`No collection results matched '${searchTerms}'`)
      }
    },
    [ loading, savedSearches, collectionFilter ]
  )
  const listHeading = (
    <h2 key="Collections::listHeading" style={styleListHeading}>
      <span role="alert" aria-live="polite">
        {headingMessage}
      </span>
    </h2>
  )

  const propsForItem = (item, itemId) => {
    return {
      onSelect: key => {
        selectCollection(key, collectionDetailFilter)
      },
    }
  }

  const onSubmit = useCallback(
    formData => {
      const savedId = findSavedId()
      if (savedId) {
        handleDelete(savedId)
      }
      else {
        handleSave(formData.get('searchName'))
      }
      setSearchSaved(prevSaved => !prevSaved)
    },
    [ findSavedId ]
  )

  const formInputs = savedId
    ? []
    : [
        {
          label: 'Search name:',
          id: 'searchName',
          name: 'searchName',
          type: 'text',
          style: styleTextInput,
        },
      ]

  const modalTitle = `Do you want to ${savedId
    ? 'delete'
    : 'save'} this search?`

  return (
    <div style={styleCollections}>
      <Meta
        title={'Collection Search Results for ' + queryText}
        formatTitle={true}
        robots="noindex"
      />
      <ModalFormUncontrolled
        isOpen={isOpen}
        onClose={onClose}
        title={modalTitle}
        submitText={savedId ? 'Delete' : 'Save'}
        cancelText={'Cancel'}
        inputs={formInputs}
        onSubmit={onSubmit}
      />
      <ListView
        totalRecords={totalHits}
        items={results}
        ListItemComponent={CollectionListItem}
        GridItemComponent={CollectionGridItem}
        propsForItem={propsForItem}
        heading={listHeading}
        showAsGrid={true}
        setOffset={offset => {
          setOffset(offset)
          fetchResultPage(offset, PAGE_SIZE)
        }}
        currentPage={currentPage}
        setCurrentPage={page => setCurrentPage(page)}
        customActions={bookmarkButton}
      />
      {/*{showMoreButton}*/}
    </div>
  )
}

Collections.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
