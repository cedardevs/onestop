import React, {useState, useCallback, useEffect, useRef} from 'react'
import PropTypes from 'prop-types'

const FIRST_PAGE = 'FIRST_PAGE'
const SKIP_PREVIOUS_PAGE = 'SKIP_PREVIOUS_PAGE'
const PAGES = 'PAGES'
const SKIP_NEXT_PAGE = 'SKIP_NEXT_PAGE'
const LAST_PAGE = 'LAST_PAGE'

const range = (from, to, step = 1) => {
  let i = from
  const pool = []

  while (i <= to) {
    pool.push(i)
    i += step
  }

  return pool
}

const stylePageListDefault = {
  alignItems: 'center',
  display: 'flex',
  justifyContent: 'center',
  listStyle: 'none',
  margin: '0 0.618em 1em 0.618em',
}

const stylePageItemDefault = {
  margin: 0,
}

const styleActivePageItemDefault = {}

export const usePaging = props => {
  const init = () => {
    let {totalRecords = null, pageLimit = 20, pageNeighbours = 0} = props
    pageLimit = typeof pageLimit === 'number' ? pageLimit : 20
    totalRecords = typeof totalRecords === 'number' ? totalRecords : 0
    pageNeighbours =
      typeof pageNeighbours === 'number'
        ? Math.max(0, Math.min(pageNeighbours, 2))
        : 0

    const totalPages = Math.ceil(totalRecords / pageLimit)

    return {
      pageLimit,
      totalRecords,
      pageNeighbours,
      totalPages,
    }
  }

  const [ state, setState ] = useState(() => init())
  const firstRun = useRef(true)

  const gotoPage = useCallback(
    page => {
      const currentPage = Math.max(1, Math.min(page, state.totalPages))
      props.setCurrentPage(currentPage)
      var mainBlock = document.getElementById('mainBlock')
      if (mainBlock) {
        mainBlock.focus()
      }
    },
    [ state.totalPages, props.pageLimit ]
  )

  useEffect(
    () => {
      gotoPage(1)
    },
    [ gotoPage ]
  )

  useEffect(
    () => {
      props.setOffset((props.currentPage - 1) * props.pageLimit)
    },
    [ props.currentPage ]
  )

  useEffect(
    () => {
      if (firstRun.current) {
        firstRun.current = false
        return
      }
      const totalRecords = props.totalRecords
      const totalPages = Math.ceil(totalRecords / state.pageLimit)
      setState({...state, totalRecords: props.totalRecords, totalPages})
    },
    [ props.totalRecords ]
  )

  const handleClick = (page, evt) => {
    evt.preventDefault()
    gotoPage(page)
  }

  const numSkipPrevious = () => {
    return state.pageNeighbours * 2 + 1
  }

  const numSkipNext = () => {
    return Math.min(
      state.totalPages - props.currentPage,
      state.pageNeighbours * 2 + 1
    )
  }

  const handleSkipPrevious = useCallback(
    evt => {
      evt.preventDefault()
      gotoPage(props.currentPage - numSkipPrevious())
    },
    [ props.currentPage ]
  )

  const handleSkipNext = useCallback(
    evt => {
      evt.preventDefault()
      gotoPage(props.currentPage + numSkipNext())
    },
    [ state.totalPages, props.currentPage, state.pageNeighbours ]
  )

  const fetchPageNumbers = useCallback(
    () => {
      const totalPages = state.totalPages
      const currentPage = props.currentPage
      const pageNeighbours = state.pageNeighbours //Pages between first and middle block

      const totalNumbers = state.pageNeighbours * 2 + 3 //Neigbours on both sides including first, middle and last
      const totalBlocks = totalNumbers + 2 //including left and right buttons

      if (totalPages > totalBlocks) {
        let pages = []
        let template = []

        const leftBound = currentPage - pageNeighbours
        const rightBound = currentPage + pageNeighbours
        const beforeLastPage = totalPages - 1

        const startPage = leftBound > 2 ? leftBound : 2
        const endPage =
          rightBound < beforeLastPage ? rightBound : beforeLastPage

        pages = range(startPage, endPage)

        const pagesCount = pages.length
        const singleSpillOffset = totalNumbers - pagesCount - 1

        const canSkipPrevious = startPage > 2
        const canSkipNext = endPage < beforeLastPage

        if (canSkipPrevious && !canSkipNext) {
          const extraPages = range(startPage - singleSpillOffset, startPage - 1)
          pages = [ ...extraPages, ...pages ]
          template = [ SKIP_PREVIOUS_PAGE, PAGES ]
        }
        else if (!canSkipPrevious && canSkipNext) {
          const extraPages = range(endPage + 1, endPage + singleSpillOffset)
          pages = [ ...pages, ...extraPages ]
          template = [ PAGES, SKIP_NEXT_PAGE ]
        }
        else if (canSkipPrevious && canSkipNext) {
          pages = [ ...pages ]
          template = [ SKIP_PREVIOUS_PAGE, PAGES, SKIP_NEXT_PAGE ]
        }

        return {
          firstPage: 1,
          pages: pages,
          lastPage: totalPages,
          template: [ FIRST_PAGE, ...template, LAST_PAGE ],
        }
      }

      return {pages: range(1, totalPages), template: [ PAGES ]}
    },
    [ state.totalPages, props.currentPage, state.pageNeighbours ]
  )

  return {
    totalRecords: state.totalRecords,
    totalPages: state.totalPages,
    currentPage: props.currentPage,
    handleClick,
    handleSkipPrevious,
    handleSkipNext,

    fetchPageNumbers,

    numSkipPrevious: numSkipPrevious(),
    numSkipNext: numSkipNext(),
  }
}

const stylePageButton = config => {
  const {
    isActive,
    isFirst,
    isPrevious,
    isAfterFirst,
    isBeforeLast,
    isNext,
    isLast,
  } = config
  return {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    color: 'white',
    background: isActive ? 'rgb(24, 71, 143)' : 'rgb(39, 124, 178)',
    borderRadius:
      isFirst || isPrevious || isNext || isLast
        ? '0.309em'
        : isAfterFirst
          ? '0.309em 0 0 0.309em'
          : isBeforeLast ? '0 0.309em 0.309em 0' : '0',
    border: 'transparent',
    borderRight: '1px groove rgb(24, 71, 143)',
    borderStyle:
      isFirst || isPrevious || isNext || isLast
        ? 'none'
        : `none ${isBeforeLast ? 'none' : 'groove'} none none`,
    textAlign: 'center',
    fontSize: '1.25em',
    margin:
      isFirst || isPrevious
        ? '0 0.309em 0 0'
        : isNext || isLast ? '0 0 0 0.309em' : 0,
    padding: '0.309em 0.618em',
    textDecoration: isActive ? 'underline' : 'none',
  }
}

const stylePageButtonHover = config => {
  const {isActive} = config
  return {
    background: isActive
      ? 'rgb(24, 71, 143)'
      : 'linear-gradient(rgb(39, 124, 178), rgb(24, 71, 143))',
  }
}

const stylePageButtonPress = config => {
  return {}
}

const stylePageButtonFocus = config => {
  return {
    outline: '2px dashed white',
    outlineOffset: '-0.309em',
  }
}

const stylePageButtonDisable = config => {
  return {
    background: '#355d39',
  }
}

function PageController({
  paging,
  ButtonComponent,
  stylePageList,
  stylePageItem,
}){
  if (paging === null) return null

  const {
    totalRecords,
    totalPages,
    currentPage,
    handleClick,
    handleSkipPrevious,
    handleSkipNext,
    fetchPageNumbers,
    numSkipPrevious,
    numSkipNext,
  } = paging

  if (!totalRecords || totalRecords <= 0) return null
  if (totalPages === 1) return null

  const {firstPage, pages, lastPage, template} = fetchPageNumbers()
  const isFirstPageActive = currentPage === firstPage
  const isLastPageActive = currentPage === lastPage

  let config = {
    isActive: false,
    isFirst: false,
    isPrevious: false,
    isAfterFirst: false,
    isBeforeLast: false,
    isNext: false,
    isLast: false,
  }

  const styleProps = config => {
    return {
      style: stylePageButton(config),
      styleHover: stylePageButtonHover(config),
      stylePress: stylePageButtonPress(config),
      styleFocus: stylePageButtonFocus(config),
      styleDisable: stylePageButtonDisable(config),
    }
  }

  const pageListItems = template.map(t => {
    if (t === FIRST_PAGE) {
      const firstPageConfig = {
        ...config,
        isActive: isFirstPageActive,
        isFirst: true,
      }
      return (
        <li style={stylePageItem} key={'paginator-first-page'}>
          <ButtonComponent
            page={firstPage}
            onClick={e => handleClick(firstPage, e)}
            config={firstPageConfig}
            {...styleProps(firstPageConfig)}
          />
        </li>
      )
    }
    if (t === SKIP_PREVIOUS_PAGE) {
      const skipPreviousConfig = {...config, isPrevious: true}
      const skipPreviousPage = currentPage - numSkipPrevious
      return (
        <li style={stylePageItem} key={'paginator-skip-previous-page'}>
          <ButtonComponent
            page={skipPreviousPage}
            numSkip={numSkipPrevious}
            onClick={handleSkipPrevious}
            config={skipPreviousConfig}
            {...styleProps(skipPreviousConfig)}
          />
        </li>
      )
    }
    if (t === PAGES) {
      return pages.map((page, index) => {
        const isPageActive = page === currentPage
        const isAfterFirst = index === 0
        const isBeforeLast = index === pages.length - 1
        let pageConfig = {
          ...config,
          isActive: isPageActive,
          isAfterFirst,
          isBeforeLast,
        }

        return (
          <li style={stylePageItem} key={`paginator-page-${page}`}>
            <ButtonComponent
              page={page}
              onClick={e => handleClick(page, e)}
              config={pageConfig}
              {...styleProps(pageConfig)}
            />
          </li>
        )
      })
    }
    if (t === SKIP_NEXT_PAGE) {
      const skipNextConfig = {...config, isNext: true}
      const skipNextPage = currentPage + numSkipNext
      return (
        <li style={stylePageItem} key={'paginator-skip-next-page'}>
          <ButtonComponent
            page={skipNextPage}
            numSkip={numSkipNext}
            onClick={handleSkipNext}
            config={skipNextConfig}
            {...styleProps(skipNextConfig)}
          />
        </li>
      )
    }
    if (t === LAST_PAGE) {
      const lastPageConfig = {
        ...config,
        isActive: isLastPageActive,
        isLast: true,
      }
      return (
        <li style={stylePageItem} key={'paginator-last-page'}>
          <ButtonComponent
            page={lastPage}
            onClick={e => handleClick(lastPage, e)}
            config={lastPageConfig}
            {...styleProps(lastPageConfig)}
          />
        </li>
      )
    }
  })

  return <ul style={stylePageList}>{pageListItems}</ul>
}

PageController.defaultProps = {
  stylePageList: stylePageListDefault,
  stylePageItem: stylePageItemDefault,
  styleActivePageItem: styleActivePageItemDefault,

  ButtonComponent: ({
    page,
    numSkip,
    onClick,
    style,
    styleHover,
    stylePress,
    styleFocus,
    styleDisable,
    config,
  }) => {
    const {isPrevious, isNext} = config
    const previousText = `«`
    const nextText = `»`
    return (
      <button onClick={onClick} style={style}>
        {isPrevious ? previousText : isNext ? nextText : page}
      </button>
    )
  },
}

PageController.propTypes = {
  stylePageList: PropTypes.object,
  stylePageItem: PropTypes.object,
  styleActivePageItem: PropTypes.object,

  ButtonComponent: PropTypes.func,

  currentPage: PropTypes.number,
  pageLimit: PropTypes.number,
  pageNeighbours: PropTypes.number,
  setCurrentPage: PropTypes.func,
  setOffset: PropTypes.func,
  totalRecords: PropTypes.number,
}

export default PageController
