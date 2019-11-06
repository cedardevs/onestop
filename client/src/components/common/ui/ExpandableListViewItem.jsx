import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import AnimateHeight from 'react-animate-height/lib/index'
import {Key} from '../../../utils/keyboardUtils'
import defaultStyles from '../../../style/defaultStyles'
import arrowIconClosed from '../../../../img/font-awesome/white/svg/caret-right.svg'
import arrowIconOpen from '../../../../img/font-awesome/white/svg/caret-down.svg'
import {caret_right, caret_down, SvgIcon} from '../SvgIcon'

const ANIMATION_DURATION = 200

const styleHeadingDefault = (open, borderRadius) => {
  const borderRadiusEffective = open
    ? `${borderRadius} ${borderRadius} 0 0`
    : `${borderRadius}`

  return {
    display: 'flex',
    textAlign: 'left',
    alignItems: 'center',
    color: '#FFFFFF',
    borderRadius: borderRadius ? borderRadiusEffective : 'none',
    borderBottom: 0,
    outline: 'none', // focus is shown on an interior element instead
    transition: `border-radius ${ANIMATION_DURATION}ms ease`,
  }
}

const styleArrowDefault = {
  userSelect: 'none',
  cursor: 'pointer',
}

const styleArrowFocusDefault = {
  outline: '2px dashed white',
  outlineOffset: '.118em',
}

const styleContentDefault = (open, display, borderRadius) => {
  const borderRadiusContentOpen = `0 0 ${borderRadius} ${borderRadius}`

  return {
    textAlign: 'left',
    borderRadius: borderRadius ? borderRadiusContentOpen : 'none',
  }
}

const styleFocusDefault = (open, borderRadius, showArrow) => {
  const borderRadiusEffective = open
    ? `${borderRadius} 0 0 0`
    : `${borderRadius} 0 0 ${borderRadius}`
  return {
    outline: showArrow ? 'none' : '2px dashed white',
    borderRadius: borderRadius ? borderRadiusEffective : 'none',
  }
}

const styleHeadingFocusDefault = () => {
  return {}
}

export default function Expandable(props){
  const [ open, setOpen ] = useState(false)
  const [ focusing, setFocusing ] = useState(false)
  const [ display, setDisplay ] = useState(props.open ? 'block' : 'none')

  useEffect(
    () => {
      if (props.open !== open) {
        setOpen(!!props.open)
      }
    },
    [ props.open ]
  )

  const {
    showArrow,
    arrowTextClosed,
    arrowTextOpened,
    styleArrowText,
    styleFocus,
    styleHeadingFocus,
    styleWrapper,
    styleHeading,
    heading,
    headingTitle,
    styleContent,
    styleContentOpen,
    content,
    borderRadius,
    styleArrow,
    styleArrowFocus,
    onToggle,
    value,
    disabled,
  } = props

  const toggle = () => {
    if (disabled) {
      return
    }
    const newOpen = !open
    if (onToggle) {
      onToggle({open: newOpen, value: value})
    }
    setOpen(newOpen)
  }

  const handleClick = event => {
    event.preventDefault()
    toggle()
  }

  const handleKeyDown = event => {
    if (event.keyCode === Key.SPACE) {
      event.preventDefault() // prevent scrolling down on space press
      toggle()
    }
    if (event.keyCode === Key.ENTER) {
      toggle()
    }
  }

  const arrowText = (
    <span>{open ? arrowTextOpened : arrowTextClosed}&nbsp;</span>
  )
  const arrow = showArrow ? open ? (
    <span style={styleArrowText}>
      &nbsp;{arrowText}
      <SvgIcon size=".9em" verticalAlign="initial" path={caret_down} />
      <span style={defaultStyles.hideOffscreen}> for {headingTitle}</span>
    </span>
  ) : (
    <span style={styleArrowText}>
      &nbsp;{arrowText}
      <SvgIcon size=".9em" verticalAlign="initial" path={caret_right} />
      <span style={defaultStyles.hideOffscreen}> for {headingTitle}</span>
    </span>
  ) : null

  const ariaHidden = display === 'none'
  const tabbable = !(props.tabbable === false)
  const tabIndex = tabbable ? '0' : '-1'
  const role = tabbable ? 'button' : undefined
  const ariaExpanded = tabbable ? open : undefined

  const stylesHeadingMerged = {
    ...styleHeadingDefault(open, borderRadius),
    ...styleHeading,
    ...(focusing ? {...styleHeadingFocusDefault(), ...styleHeadingFocus} : {}),
  }

  const styleFocused = {
    ...(focusing
      ? {...styleFocusDefault(open, borderRadius, showArrow), ...styleFocus}
      : {}),
  }

  const styleContentMerged = {
    ...styleContentDefault(open, display, borderRadius),
    ...styleContent,
    ...(open ? styleContentOpen : {}),
  }

  const styleArrowMerged = {
    ...styleArrowDefault,
    ...styleArrow,
    ...(focusing && showArrow
      ? {...styleArrowFocusDefault, ...styleArrowFocus}
      : {}),
  }

  // dynamic aria-hidden={_.isEmpty(arrowText)} may not work for all uses of Expandable
  const headingEffective = heading ? (
    <div style={stylesHeadingMerged} title={headingTitle}>
      <div style={styleFocused}>{heading}</div>
      <div
        aria-hidden={_.isEmpty(arrowText)}
        style={styleArrowMerged}
        onClick={handleClick}
        onKeyDown={handleKeyDown}
        tabIndex={tabIndex}
        aria-expanded={ariaExpanded}
        role={role}
        onFocus={() => setFocusing(true)}
        onBlur={() => setFocusing(false)}
      >
        {arrow}
      </div>
    </div>
  ) : null

  return (
    <div style={styleWrapper}>
      {headingEffective}
      <div style={styleContentMerged} aria-hidden={ariaHidden}>
        <AnimateHeight
          duration={ANIMATION_DURATION}
          height={open ? 'auto' : 0}
          onAnimationStart={() => {
            if (!open) {
              setDisplay('none')
            }
          }}
          onAnimationEnd={() => setDisplay('block')}
        >
          {content}
        </AnimateHeight>
      </div>
    </div>
  )
}
