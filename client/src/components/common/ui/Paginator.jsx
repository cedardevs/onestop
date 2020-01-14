import React, {useState, useCallback, useEffect, useRef} from 'react'
import PropTypes from 'prop-types'

const LEFT_PAGE = 'LEFT'
const RIGHT_PAGE = 'RIGHT'

const range = (from, to, step = 1) => {
  let i = from
  const pool = []

  while (i <= to) {
    pool.push(i)
    i += step
  }

  return pool
}

const paginatorStyle = {
  alignItems: 'center',
  display: 'flex',
  justifyContent: 'center',
  listStyle: 'none',
}

const pageActiveStyle = {
  backgroundColor: '#3895d3',
  borderColor: '#3895d3',
  color: '#fff',
}

function Paginator(props){
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

  const handleMoveLeft = useCallback(
    evt => {
      evt.preventDefault()
      gotoPage(props.currentPage - state.pageNeighbours * 2 - 1)
    },
    [ props.currentPage ]
  )

  const handleMoveRight = useCallback(
    evt => {
      evt.preventDefault()
      gotoPage(props.currentPage + state.pageNeighbours * 2 + 1)
    },
    [ props.currentPage, state.pageNeighbours ]
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

        const leftBound = currentPage - pageNeighbours
        const rightBound = currentPage + pageNeighbours
        const beforeLastPage = totalPages - 1

        const startPage = leftBound > 2 ? leftBound : 2
        const endPage =
          rightBound < beforeLastPage ? rightBound : beforeLastPage

        pages = range(startPage, endPage)

        const pagesCount = pages.length
        const singleSpillOffset = totalNumbers - pagesCount - 1

        const leftSpill = startPage > 2
        const rightSpill = endPage < beforeLastPage

        const leftSpillPage = LEFT_PAGE
        const rightSpillPage = RIGHT_PAGE

        if (leftSpill && !rightSpill) {
          const extraPages = range(startPage - singleSpillOffset, startPage - 1)
          pages = [ leftSpillPage, ...extraPages, ...pages ]
        }
        else if (!leftSpill && rightSpill) {
          const extraPages = range(endPage + 1, endPage + singleSpillOffset)
          pages = [ ...pages, ...extraPages, rightSpillPage ]
        }
        else if (leftSpill && rightSpill) {
          pages = [ leftSpillPage, ...pages, rightSpillPage ]
        }

        return [ 1, ...pages, totalPages ]
      }

      return range(1, totalPages)
    },
    [ state.totalPages, props.currentPage, state.pageNeighbours ]
  )

  if (!state.totalRecords) return null

  if (state.totalPages === 1) return null

  const {currentPage} = props
  const pages = fetchPageNumbers()

  return (
    <ul style={paginatorStyle}>
      {pages.map((page, index) => {
        if (page === LEFT_PAGE)
          return (
            <li key={index}>
              <button onClick={handleMoveLeft}>{props.pagePrevText}</button>
            </li>
          )

        if (page === RIGHT_PAGE)
          return (
            <li key={index}>
              <button onClick={handleMoveRight}>{props.pageNextText}</button>
            </li>
          )

        return (
          <li key={index}>
            <button
              style={currentPage === page ? pageActiveStyle : null}
              onClick={e => handleClick(page, e)}
            >
              {page}
            </button>
          </li>
        )
      })}
    </ul>
  )
}

Paginator.defaultProps = {
  pageContainerClass: 'react-hooks-paginator',
  pageActiveClass: 'active',
  pageItemClass: 'page-item',
  pageLinkClass: 'page-link',
  pageNextText: 'Next »',
  pagePrevText: '« Prev',
  pagePrevClass: 'page-link',
  pageNextClass: 'page-link',
}

Paginator.propTypes = {
  currentPage: PropTypes.number,
  pageActiveClass: PropTypes.string,
  pageNextText: PropTypes.oneOfType([ PropTypes.string, PropTypes.node ]),
  pagePrevText: PropTypes.oneOfType([ PropTypes.string, PropTypes.node ]),
  pagePrevClass: PropTypes.string,
  pageNextClass: PropTypes.string,
  pageContainerClass: PropTypes.string,
  pageItemClass: PropTypes.string,
  pageLimit: PropTypes.number,
  pageLinkClass: PropTypes.string,
  pageNeighbours: PropTypes.number,
  setCurrentPage: PropTypes.func,
  setOffset: PropTypes.func,
  totalRecords: PropTypes.number.isRequired,
}

export default Paginator
