import React, {useRef, useState, useEffect} from 'react'
import {SvgIcon, times_circle} from '../SvgIcon'
import {consolidateStyles} from '../../../utils/styleUtils'
import {SiteColors} from '../../../style/defaultStyles'

const styleButton = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  margin: '0.618em',
  padding: 0,
}

const styleButtonFocused = {
  outline: '2px dashed white',
  fill: SiteColors.LINK_LIGHT,
}

const styleSVG = {
  outline: 'none',
  fill: 'white',
}
const styleSVGHover = {
  fill: SiteColors.LINK_LIGHT,
}

const styleSVGPress = {}

const styleSVGFocus = {}

function usePrevious(value, defaultValue = undefined){
  const ref = useRef()
  useEffect(
    () => {
      ref.current = value
    },
    [ value ]
  )
  return ref.current ? ref.current : defaultValue
}

const CloseButton = ({title, onClose, size}) => {
  const buttonRef = useRef(null)

  const [ hovering, setHovering ] = useState(false)
  const [ pressing, setPressing ] = useState(false)
  const [ pressingGlobal, setPressingGlobal ] = useState(false)
  const [ focusing, setFocusing ] = useState(false)

  const previousPressingGlobal = usePrevious(pressingGlobal, false)

  const handleMouseOver = event => {
    setHovering(true)
    setPressing(previousPressingGlobal)
  }

  const handleMouseOut = event => {
    setHovering(false)
    setPressing(false)
  }

  const handleGlobalMouseUp = event => {
    setPressingGlobal(false)
  }

  const handleGlobalMouseDown = event => {
    setPressingGlobal(true)
  }

  const handleMouseDown = event => {
    setPressing(true)
  }

  const handleMouseUp = event => {
    setPressing(false)
  }

  const handleFocus = event => {
    setFocusing(true)
  }

  const handleBlur = event => {
    setFocusing(false)
  }

  useEffect(() => {
    if (buttonRef.current) {
      buttonRef.current.addEventListener('mouseup', handleGlobalMouseUp, false)
      buttonRef.current.addEventListener(
        'mousedown',
        handleGlobalMouseDown,
        false
      )
    }
    return () => {
      if (buttonRef.current) {
        buttonRef.current.removeEventListener(
          'mouseup',
          handleGlobalMouseUp,
          false
        )
        buttonRef.current.removeEventListener(
          'mousedown',
          handleGlobalMouseDown,
          false
        )
      }
    }
  }, [])

  const styleButtonMerged = consolidateStyles(
    styleButton,
    focusing ? styleButtonFocused : {}
  )

  const styleSVGMerged = consolidateStyles(
    styleSVG,
    hovering ? styleSVGHover : {},
    pressing ? styleSVGPress : {},
    focusing ? styleSVGFocus : {}
  )

  return (
    <button
      ref={buttonRef}
      title={title}
      aria-label={title}
      style={styleButtonMerged}
      onClick={onClose}
      onMouseOver={handleMouseOver}
      onMouseOut={handleMouseOut}
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
      onFocus={handleFocus}
      onBlur={handleBlur}
    >
      <SvgIcon
        size={size ? size : '2em'}
        style={styleSVGMerged}
        path={times_circle}
      />
    </button>
  )
}
export default CloseButton
