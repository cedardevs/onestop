import React, {useEffect, useLayoutEffect, useRef, useState} from 'react'
import ResizeObserver from 'resize-observer-polyfill'

const ANIMATION_DURATION = 200

export const ModalContext = () => {
  return React.createContext({})
}

const styleRelative = zIndex => {
  return {
    position: 'relative',
    top: 0,
    zIndex: zIndex ? zIndex : 0,
  }
}

const styleAbsolute = (left, top, width, visible) => {
  return {
    position: 'absolute',
    left: `${left}px`,
    top: `${top}px`,
    width: visible ? `${width}px` : 0,
    height: `auto`,

    visibility: visible ? 'visible' : 'hidden',
    opacity: visible ? 1 : 0,
    transition: `opacity ${ANIMATION_DURATION}ms, width ${ANIMATION_DURATION}ms`,

    // Animating width has visual side-effects with overflow and wrapping, so we must
    // prevent these effects while the modal is animating
    //overflow: animating ? 'hidden' : 'initial',
    //whiteSpace: animating ? 'nowrap' : 'initial',
  }
}

export function useModal(open){
  const targetRef = useRef(null)
  const relativeRef = useRef(null)
  const contentRef = useRef(null)

  const [ targetInitialized, setTargetInitialized ] = useState(false)
  const [ contentInitialized, setContentInitialized ] = useState(false)

  const [ relativePosition, setRelativePosition ] = useState(null)

  // tracks the '100%' width (in `px`) of the target container (desired visual location of modal)
  // so that the absolutely positioned modal content knows the `px` width to render and appear as if it were actually there
  const [ targetWidth, setTargetWidth ] = useState(null)

  // tracks the fully open height of the content, given it spans the same '100%' width of the target
  const [ contentHeight, setContentHeight ] = useState(null)

  const [ targetOpen, setTargetOpen ] = useState(false)
  const [ contentOpen, setContentOpen ] = useState(false)

  const [ styleContent, setStyleContent ] = useState(
    styleAbsolute(0, 0, 0, false)
  )

  const updateRelativePosition = () => {
    const targetElement = targetRef.current
    const relativeElement = relativeRef.current

    // fallback rects (left: 0, top: 0)
    let targetRect = new DOMRect()
    let relativeRect = new DOMRect()
    if (targetElement) {
      targetRect = targetElement.getBoundingClientRect()
    }
    if (relativeElement) {
      relativeRect = relativeElement.getBoundingClientRect()
    }

    // viewport coordinates of the target and proxy elements
    const {left: xTarget, top: yTarget} = targetRect
    const {left: xRelative, top: yRelative} = relativeRect

    // relative coordinates of target element
    setRelativePosition({left: xTarget - xRelative, top: yTarget - yRelative})
  }

  const updateTargetWidth = () => {
    const contentElement = contentRef.current
    const targetElement = targetRef.current
    // allow the target room for the content's margin, border width, and padding
    // `.getBoundingClientRect()` values are relative to outer-most boundary
    let space = 0
    if (contentElement) {
      const contentCSS = window.getComputedStyle(contentElement)
      const radix = 10

      // x-axis considerations
      const borderLeftWidth = parseInt(contentCSS.borderLeftWidth, radix)
      const borderRightWidth = parseInt(contentCSS.borderRightWidth, radix)
      const marginLeft = parseInt(contentCSS.marginLeft, radix)
      const marginRight = parseInt(contentCSS.marginRight, radix)
      const paddingLeft = parseInt(contentCSS.paddingLeft, radix)
      const paddingRight = parseInt(contentCSS.paddingRight, radix)

      // subtract space to get effective dimensions of content that won't overflow target region
      const leftSpace = marginLeft + borderLeftWidth + paddingLeft
      const rightSpace = paddingRight + borderRightWidth + marginRight
      space = leftSpace + rightSpace
    }

    let targetRect = new DOMRect()
    if (targetElement) {
      targetRect = targetElement.getBoundingClientRect()
    }
    setTargetWidth(targetRect.width - space)
  }

  const updateContentHeight = () => {
    const contentElement = contentRef.current
    // allow the target room for the content's margin, border width, and padding
    // `.getBoundingClientRect()` values are relative to outer-most boundary
    let space = 0
    let contentRect = new DOMRect()
    if (contentElement) {
      const contentCSS = window.getComputedStyle(contentElement)
      const radix = 10

      // y-axis considerations
      const borderTopWidth = parseInt(contentCSS.borderTopWidth, radix)
      const borderBottomWidth = parseInt(contentCSS.borderBottomWidth, radix)
      const marginTop = parseInt(contentCSS.marginTop, radix)
      const marginBottom = parseInt(contentCSS.marginBottom, radix)
      const paddingTop = parseInt(contentCSS.paddingTop, radix)
      const paddingBottom = parseInt(contentCSS.paddingBottom, radix)

      // subtract space to get effective dimensions of content that won't overflow target region
      const topSpace = marginTop + borderTopWidth + paddingTop
      const bottomSpace = marginBottom + borderBottomWidth + paddingBottom
      space = topSpace + bottomSpace
      contentRect = contentElement.getBoundingClientRect()
    }
    console.log('contentRect ->', contentRect)
    setContentHeight(contentRect.height - space)
  }

  const updateStyleContent = visible => {
    if (relativePosition && targetWidth) {
      const {left, top} = relativePosition
      setStyleContent(styleAbsolute(left, top, targetWidth, visible))
    }
  }

  const [ ro ] = useState(
    () =>
      new ResizeObserver(([ entry ]) => {
        updateRelativePosition()
        updateTargetWidth()
        updateContentHeight()
        updateStyleContent(contentOpen)
      })
  )

  // observe resize changes to the modal region
  useEffect(() => {
    if (targetRef.current) ro.observe(targetRef.current)
    return () => ro.disconnect()
  }, [])

  return {
    // open is a passthrough param to `useModal` which triggers the overall state of the modal
    // the internal animation state is handled automatically
    open,

    // modal refs
    targetRef,
    relativeRef,
    contentRef,

    // has the target component been mounted?
    targetInitialized,
    setTargetInitialized,

    // has the content component been mounted?
    contentInitialized,
    setContentInitialized,

    // modal relative position
    relativePosition,
    setRelativePosition,
    updateRelativePosition,

    // modal target width
    targetWidth,
    setTargetWidth,
    updateTargetWidth,

    // modal content height
    contentHeight,
    setContentHeight,
    updateContentHeight,

    // modal style content
    styleContent,
    setStyleContent,
    updateStyleContent,

    // ???
    targetOpen,
    setTargetOpen,

    contentOpen,
    setContentOpen,
  }
}

const ModalComponent = ({
  targetRef,
  targetInitialized,
  setTargetInitialized,
  setTargetWidth,
  updateTargetWidth,
  setTargetOpen,
  contentHeight,
  setContentOpen,
}) => {
  // since our modal context is borrowed between content and target,
  // this mount/unmount operation allows us to track when both content and target are available
  useLayoutEffect(() => {
    console.log('Modal initialized')
    setTargetInitialized(true)
    return () => {
      setTargetInitialized(false)
    }
  }, [])

  // get the 100% width and corresponding height of the modal region intially (before render)
  // and update the style for the content
  useEffect(
    () => {
      // if the target has been mounted
      if (targetInitialized) {
        console.log('updating target width')
        // then it's reliable to evaluate the '100%' target width
        updateTargetWidth()
      }
      return () => {
        setTargetWidth(null)
      }
    },
    [ targetInitialized ]
  )

  const handleTransitionStart = event => {
    const property = event.propertyName
    if (property === 'height') {
      const targetCSS = window.getComputedStyle(targetRef.current)
      if (targetCSS.height === '0px') {
        console.log('target starting to open')
      }
      else {
        console.log('target starting to close')
        setContentOpen(false)
      }
    }
  }

  const handleTransitionEnd = event => {
    // this ensures the map tiles get loaded properly around the animation
    const property = event.propertyName
    if (property === 'height') {
      const targetCSS = window.getComputedStyle(targetRef.current)
      if (targetCSS.height === '0px') {
        console.log('target finished closing')
        setTargetOpen(false)
      }
      else {
        console.log('target finished opening')
        setTargetOpen(true)
      }
    }
  }

  useEffect(() => {
    // on mount
    if (targetRef.current) {
      targetRef.current.addEventListener(
        'transitionstart',
        handleTransitionStart
      )
      targetRef.current.addEventListener('transitionend', handleTransitionEnd)
    }
    return () => {
      if (targetRef.current) {
        targetRef.current.removeEventListener(
          'transitionend',
          handleTransitionEnd
        )
        targetRef.current.removeEventListener(
          'transitionstart',
          handleTransitionStart
        )
      }
    }
  }, [])

  const styleTarget = {
    width: '100%',
    height: contentHeight !== null && open ? `${contentHeight}px` : 0,
    background: 'yellow',
    transition: `height ${ANIMATION_DURATION}ms`,
  }
  return <div style={styleTarget} ref={targetRef} />
}

const Modal = props => {
  const {context} = props
  if (context) {
    return (
      <context.Consumer>
        {modal => <ModalComponent {...modal} />}
      </context.Consumer>
    )
  }
}
export default Modal

const ModalContentComponent = ({
  zIndex,
  children,
  relativeRef,
  contentRef,
  relativePosition,
  targetWidth,
  contentHeight,
  setContentHeight,
  updateContentHeight,
  contentInitialized,
  setContentInitialized,
  styleContent,
  updateStyleContent,
  contentOpen,
}) => {
  useLayoutEffect(() => {
    // since our modal context is borrowed between content and target,
    // this mount/unmount operation allows us to track when both content and target are available
    console.log('ModalContent initialized')
    setContentInitialized(true)
    return () => {
      setContentInitialized(false)
    }
  }, [])

  useEffect(
    () => {
      // if the content & target have been mounted, and the target width has been evaluated
      if (contentInitialized && targetWidth !== null) {
        console.log('updating content height')
        // then this `useEffect` allows to measure the effective content height at that width from a resize after render
        updateContentHeight()
      }
      return () => {
        setContentHeight(null)
      }
    },
    [ targetWidth ]
  )

  useLayoutEffect(
    () => {
      console.log(
        `updating style content because (relative position=${JSON.stringify(
          relativePosition
        )}, targetWidth=${targetWidth}, or contentHeight=${contentHeight} changed)`
      )
      updateContentHeight()
      updateStyleContent(contentOpen)
    },
    [ relativePosition, targetWidth, contentHeight ]
  )

  //console.log("styleContent", styleContent)
  return (
    <div style={styleRelative(zIndex)} ref={relativeRef}>
      <div style={styleContent} ref={contentRef}>
        {children}
      </div>
    </div>
  )
}

export const ModalContent = props => {
  const {context, zIndex, children} = props
  if (context) {
    return (
      <context.Consumer>
        {modal => (
          <ModalContentComponent
            zIndex={zIndex}
            children={children}
            {...modal}
          />
        )}
      </context.Consumer>
    )
  }
}
