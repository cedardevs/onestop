import React, {useState} from 'react'
import PropTypes from 'prop-types'
import {isFTP, processUrl} from '../../../utils/urlUtils'
import MapThumbnail from '../../common/MapThumbnail'
import {boxShadow} from '../../../style/defaultStyles'
import {consolidateStyles} from '../../../utils/styleUtils'
import {useListViewItem} from '../../common/ui/ListViewItem'

const styleCard = {
  width: '25em',
  height: '15.5em',
  margin: '0 2em 2em 0',
  textAlign: 'center',
}

const styleContent = thumbnailUrl => {
  let styleBackground = {}
  if (thumbnailUrl) {
    styleBackground = {
      background: `url('${thumbnailUrl}')`,
      backgroundColor: 'black',
      backgroundRepeat: 'no-repeat',
      backgroundSize: 'cover',
      backgroundPosition: 'center center',
    }
  }
  return {
    boxSizing: 'border-box',
    width: '100%',
    height: '100%',
    color: 'white',
    overflow: 'hidden',
    position: 'relative',
    boxShadow: boxShadow,
    ...styleBackground,
  }
}

const styleOverlay = {
  position: 'absolute',
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  display: 'inline-flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  background: 'none',
  width: '100%',
  height: '100%',
  boxSizing: 'content-box',
  border: 0,
  color: 'inherit',
  font: 'inherit',
  lineHeight: 'normal',
  overflow: 'visible',
  borderRadius: 0,
  padding: 0,
  margin: 0,
}

const styleOverlayHover = {
  color: 'white',
}

const styleOverlayFocus = {
  color: 'white',
}

const styleOverlayBlur = {
  color: 'inherit',
}

const styleArch = {
  position: 'absolute',
  boxSizing: 'border-box',
  width: '100%',
  bottom: 0,
  left: 0,
  right: 0,
  height: '1.618em',
  overflow: 'hidden',
  whiteSpace: 'nowrap',
  textOverflow: 'ellipsis',
  fontWeight: 'normal',
  padding: '1.618em',
  margin: 0,
  color: '#222',
  backgroundColor: '#FBFBFB',
  transition: 'background-color 0.3s ease, color 0.3s ease, height 0.3s ease',
  borderTop: '1px solid #AAA',
  borderRadius: '12.5em 12.5em 0em 0em / 2.236em',
  boxShadow: boxShadow,
}

const styleArchHover = {
  fontWeight: 'bold',
  backgroundColor: '#307AAA',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchFocus = {
  fontWeight: 'bold',
  backgroundColor: '#307AAA',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchBlur = {
  fontWeight: 'normal',
  backgroundColor: '#FBFBFB',
  color: '#222',
  height: '1.618em',
}

const styleMapContainer = {
  position: 'absolute',
  top: 0,
  zIndex: 0,
  width: '100%',
  maxWidth: '100%',
  height: '100%',
}

const styleTitle = {
  letterSpacing: '0.05em',
  fontSize: '1em',
  fontWeight: 600, // semi-bold
  textAlign: 'center',
  lineHeight: '1.618em', // use this value to count block height
  maxHeight: '4.854em', // maxHeight = lineHeight (1.618) * max lines (3)
  margin: 0,
  padding: 0,
  textOverflow: 'ellipsis',
  whiteSpace: 'normal',
  overflow: 'hidden',
}

const CollectionGridItem = props => {
  const {
    item,
    focusing,
    handleFocus,
    handleBlur,
    handleSelect,
    handleKeyDown,
  } = useListViewItem(props)
  const [ hovering, setHovering ] = useState(false)

  const thumbnailUrl = processUrl(item.thumbnail)

  const fallbackMap = () => {
    const geometry = item.spatialBounding
    // show map for FTP links as most modern block loading FTP subresources for security reasons
    // or if no thumbnail was provided at all
    return isFTP(thumbnailUrl) || !thumbnailUrl ? (
      <div style={styleMapContainer}>
        <MapThumbnail geometry={geometry} interactive={false} />
      </div>
    ) : null
  }

  const styleOverlayMerged = consolidateStyles(
    styleOverlay,
    focusing ? styleOverlayFocus : styleOverlayBlur,
    hovering ? styleOverlayHover : null
  )

  const styleArchMerged = consolidateStyles(
    styleArch,
    focusing ? styleArchFocus : styleArchBlur,
    hovering ? styleArchHover : null
  )

  return (
    <div style={styleCard} onKeyDown={handleKeyDown}>
      <div style={styleContent(thumbnailUrl)}>
        <button
          role="link"
          style={styleOverlayMerged}
          onFocus={handleFocus}
          onBlur={handleBlur}
          onClick={handleSelect}
          onKeyDown={handleKeyDown}
          onMouseOver={() => setHovering(true)}
          onMouseOut={() => setHovering(false)}
        >
          {fallbackMap()}
          <div style={styleArchMerged}>
            <h3 style={styleTitle}>{item.title}</h3>
          </div>
        </button>
      </div>
    </div>
  )
}

CollectionGridItem.propTypes = {
  item: PropTypes.object.isRequired,
  onSelect: PropTypes.func.isRequired,
}

export default CollectionGridItem
