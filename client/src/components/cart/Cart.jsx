import React, {useState} from 'react'
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'
import Meta from 'react-helmet'
import ListView from '../common/ui/ListView'
import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'
import {identifyProtocol} from '../../utils/resultUtils'
import clearIcon from 'fa/ban.svg'
import {times, SvgIcon} from '../common/SvgIcon'
import {fontFamilySerif} from '../../utils/styleUtils'
import ScriptDownloader from './ScriptDownloader'
import {FEATURE_CART} from '../../utils/featureUtils'
import CartListItem from './CartListItem'
import {PAGE_SIZE} from '../../utils/queryUtils'
// import {Confirmation, useConfirmation} from '../common/dialog/OneStopDialog'
import {
  useDisclosure,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
} from "@chakra-ui/core";

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleCartListWrapper = {
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  paddingTop: '1.618em',
  paddingBottom: '1.618em',
  backgroundColor: 'white',
  color: '#222',
}

const styleListHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
}

const styleCartActions = {
  margin: '0 1.618em 1.618em 1.618em',
}

const styleCartActionsTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  margin: '0 0 0.618em 0',
  padding: 0,
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
// export default class Cart extends React.Component {
export default function Cart({
  featuresEnabled,
  // loading, TODO does this need to wire up specific loading state somewhere else correctly now?
  selectedGranules,
  numberOfGranulesSelected,
  deselectGranule,
  deselectAllGranules,
}){
  if (!featuresEnabled.includes(FEATURE_CART)) {
    return null
  }

  // keep track of used protocols in results to avoid unnecessary legend keys
  const usedProtocols = new Set()

  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)
  const { isOpen, onOpen, onClose } = useDisclosure();

  // const {dialog: dialogEmptyCart, confirmation} = useConfirmation({
  //   title: `Clear all cart contents.`,
  //   question: `Are you sure you want remove all items (${Object.keys(
  //     selectedGranules
  //   ).length} total)?`,
  //   yesAction: dialog => {
  //     deselectAllGranules()
  //     dialog.hide()
  //   },
  //   yesText: 'Empty cart',
  //   noAction: dialog => {
  //     dialog.hide()
  //   },
  //   noText: 'Never mind',
  // })

  //only show granules for this page
  const allowed = Object.keys(selectedGranules).slice(
    offset,
    offset + PAGE_SIZE
  )

  const subset = Object.keys(selectedGranules)
    .filter(key => allowed.includes(key))
    .reduce((obj, key) => {
      obj[key] = selectedGranules[key]
      return obj
    }, {})

  const propsForItem = (item, itemId) => {
    const collectionId = item.internalParentIdentifier
    return {
      onSelect: key => {
        selectCollection(collectionId, collectionDetailFilter)
      },
      deselectGranule,
    }
  }

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

  const cartActionsWrapper =
    numberOfGranulesSelected === 0 ? null : (
      <div style={styleCartActions}>
        <h1 style={styleCartActionsTitle}>Cart Actions</h1>
        <ScriptDownloader
          key="scriptDownloaderButton"
          selectedGranules={selectedGranules}
        />
      </div>
    )

  let message = 'No files selected for download'
  if (numberOfGranulesSelected > 0) {
    message = `Showing ${offset + 1} - ${offset +
      Object.keys(subset)
        .length} of ${numberOfGranulesSelected.toLocaleString()} files for download`
  }
  /**
  NOTE: this uses LiveAnnouncer instead of the following span, because the message does not toggle to "loading" in between, causing it to read changes incorrectly.
  <span role="alert" aria-live="polite">
    {message}
  </span>
  */
  const listHeading = (
    <h2 key="Cart::listHeading" style={styleListHeading}>
      <LiveAnnouncer>
        <LiveMessage message={message} aria-live="polite" />
      </LiveAnnouncer>
      <span aria-hidden="true">{message}</span>
    </h2>
  )

  const clearAllAction = {
    text: 'Clear All',
    title: 'Clear All Files from Cart',
    icon: clearIcon,
    showText: false,
    handler: () => {
      // allow user to confirm action before commiting
      onOpen()
    },
    notification: 'Clearing all files from cart',
  }

  // only show this button when there's something to clear

  const cartListCustomActions = []

  if (Object.keys(selectedGranules).length > 0) {
    cartListCustomActions.push(clearAllAction)
  }

// <Modal isOpen={isOpen} onClose={onClose}>
//   <ModalOverlay />
//   <ModalContent>
//     <ModalHeader>Clear all cart contents.</ModalHeader>
//     <ModalCloseButton />
//     <ModalBody>
//       `Are you sure you want remove all items (${Object.keys(
//         selectedGranules
//       ).length} total)?`
//     </ModalBody>
//
//     <ModalFooter>
//       <Button onClick={() => {
//           deselectAllGranules()
//           onClose()
//         }}>
//         Yes
//       </Button>
//       <Button onClick={onClose}>Never mind</Button>
//     </ModalFooter>
//   </ModalContent>
// </Modal>
// Modal isOpen={isOpen} onClose={onClose} isCentered size="md">
//   <ModalOverlay backgroundColor='tomato' zIndex={1999}>
//   <ModalContent  isCentered size="md" >
//     <ModalHeader>Modal Title</ModalHeader>
//     <ModalCloseButton />
//     <ModalBody>
//       asdfasdf
//     </ModalBody>
//
//     <ModalFooter>
//       <Button colorScheme="blue" mr={3} onClick={onClose}>
//         Close
//       </Button>
//       <Button variant="ghost">Secondary Action</Button>
//     </ModalFooter>
//   </ModalContent>
//   </ModalOverlay>
// </Modal>

  return (
    <div style={styleCenterContent}>
      <Meta title="File Access Cart" robots="noindex" />

      <Modal isOpen={isOpen} onClose={onClose} isCentered >
        <ModalOverlay backgroundColor="#5d5d5d94">
        <ModalContent maxWidth="28em" borderRadius='0.309em' padding="0.309em">
          <ModalHeader><h1>Clear all cart contents.</h1></ModalHeader>
        <ModalCloseButton backgroundColor="#00000000" border="none" _focus={styleButtonFocus}><SvgIcon size="1em" path={times} /></ModalCloseButton>
          <ModalBody>
            Are you sure you want remove all items ({Object.keys(selectedGranules).length} total)?
          </ModalBody>

          <ModalFooter>

            <Button
            style={styleButton}
            styleFocus={styleButtonFocus}
            onClick={onClose}>Never mind</Button>
            <Button
            style={styleButton}
            styleFocus={styleButtonFocus}
            onClick={() => {
                      deselectAllGranules()
                      onClose()
                    }}>
                    Yes
            </Button>
          </ModalFooter>
        </ModalContent>
      </ModalOverlay>
      </Modal>

      <div style={styleCartListWrapper}>
        {cartActionsWrapper}
        <ListView
          totalRecords={numberOfGranulesSelected}
          items={subset}
          ListItemComponent={CartListItem}
          GridItemComponent={null}
          propsForItem={propsForItem}
          heading={listHeading}
          customActions={cartListCustomActions}
          setOffset={offset => {
            setOffset(offset)
          }}
          currentPage={currentPage}
          setCurrentPage={page => setCurrentPage(page)}
        />
      </div>
    </div>
  )
}
