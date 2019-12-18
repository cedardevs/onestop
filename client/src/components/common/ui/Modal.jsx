import React, {useEffect, useLayoutEffect, useRef, useState} from 'react'
import ResizeObserver from 'resize-observer-polyfill'

const ANIMATION_DURATION = 200
const RESTYLE_INTERVAL = 500

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

const styleAbsolute = (left, top, width) => {
  return {
    position: 'absolute',
    left: `${left}px`,
    top: `${top}px`,
    width: `${width}px`,
    height: `auto`,
    transition: `opacity ${ANIMATION_DURATION}ms, width ${ANIMATION_DURATION}ms`,
  }
}

export function useModal(open){
  const targetRef = useRef(null)
  const relativeRef = useRef(null)
  const contentRef = useRef(null)

  // tracks the fully open height of the content, given it spans the same '100%' width of the target
  const [ contentHeight, setContentHeight ] = useState(null)

  const [ styleContent, setStyleContent ] = useState(() => {
    styleAbsolute(0, 0, 0)
  })

  const calculateRelativePosition = () => {
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
    return {left: xTarget - xRelative, top: yTarget - yRelative}
  }

  const calculateTargetWidth = () => {
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
    return targetRect.width - space
  }

  const calculateContentHeight = () => {
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
    return contentRect.height - space
  }

  const updateStyleContent = () => {
    if (contentRef.current && targetRef.current) {
      const {left, top} = calculateRelativePosition()
      const targetWidth = calculateTargetWidth()
      const contentHeight = calculateContentHeight()
      setContentHeight(contentHeight)
      const newStyle = styleAbsolute(left, top, targetWidth)
      setStyleContent(newStyle)
    }
  }

  useEffect(() => {
    // due to position changes happening without triggering renders,
    // this interval triggers and update to the content style to account for this
    const styleInterval = setInterval(() => {
      if (contentRef.current && targetRef.current) {
        updateStyleContent()
      }
    }, RESTYLE_INTERVAL)
    return () => {
      if (styleInterval) {
        clearInterval(styleInterval)
      }
    }
  }, [])

  useEffect(
    () => {
      // when the contentRef or targetRef change, re-evaluate the content style
      updateStyleContent()
    },
    [ contentRef.current, targetRef.current ]
  )

  const [ roc ] = useState(() => {
    return new ResizeObserver((entries, observer) => {
      // when the content has resized, re-evaluate the content style
      updateStyleContent()
    })
  })

  const [ rot ] = useState(
    () =>
      new ResizeObserver((entries, observer) => {
        // when the target has resized, re-evaluate the content style
        updateStyleContent()
      })
  )

  // observe resize changes to the content
  useEffect(() => {
    if (contentRef.current) roc.observe(contentRef.current)
    return () => roc.disconnect()
  }, [])

  // observe resize changes to the target
  useEffect(() => {
    if (targetRef.current) rot.observe(targetRef.current)
    return () => rot.disconnect()
  }, [])

  return {
    // open is a passthrough param to `useModal` which triggers the overall state of the modal
    // the internal animation state is handled automatically
    open,

    // modal refs
    targetRef,
    relativeRef,
    contentRef,

    // modal content height
    contentHeight,
    setContentHeight,

    // modal style content
    styleContent,
    setStyleContent,
    updateStyleContent,
  }
}

const ModalComponent = ({targetRef, open, contentHeight}) => {
  const styleTarget = {
    width: '100%',
    height: open ? `${contentHeight}px` : 0,
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
  open,
  zIndex,
  children,
  relativeRef,
  contentRef,
  styleContent,
  updateStyleContent,
}) => {
  useEffect(() => {
    // update the content style when content is mounted initially
    updateStyleContent()
  }, [])

  const handleTransitionStart = event => {
    // update the content style when the content style transitions start
    updateStyleContent()
  }

  useEffect(() => {
    if (contentRef.current) {
      contentRef.current.addEventListener(
        'transitionstart',
        handleTransitionStart
      )
    }
    return () => {
      if (contentRef.current) {
        contentRef.current.removeEventListener(
          'transitionstart',
          handleTransitionStart
        )
      }
    }
  })

  return (
    <div style={styleRelative(zIndex)} ref={relativeRef}>
      <div style={{...styleContent, opacity: open ? 1 : 0}} ref={contentRef}>
        {open ? children : null}
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
