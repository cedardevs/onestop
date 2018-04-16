import React from 'react'
import FlexRow from '../common/FlexRow'
import {processUrl} from '../utils/urlUtils'
import {fontFamilySerif} from '../utils/styleUtils'

const styleFeaturedDatasetsWrapper = {
  marginTop: '2.618em',
}

const styleFeaturedDatasetsLabel = {
  textAlign: 'center',
  fontFamily: fontFamilySerif(),
  margin: '0 0 0.618em 0',
}

const styleFeaturedDatasets = {
  color: '#F9F9F9',
}

const styleTitle = (active, first, last, collapseImage) => {
  const topRightRadius = collapseImage ? '1em' : '0'
  const bottomRightRadius = collapseImage ? '1em' : '0'
  return {
    background: active ? '#263f78' : '#007ec6',
    textAlign: collapseImage ? 'center' : 'right',
    fontFamily: fontFamilySerif(),
    fontSize: '1.25em',
    padding: '1em',
    borderRadius: first
      ? `1em ${topRightRadius} 0 0`
      : last ? `0 0 ${bottomRightRadius} 1em` : 'none',
  }
}

const styleTitleList = collapseImage => {
  return {
    listStyleType: 'none',
    cursor: 'pointer',
    margin: 0,
    padding: 0,
    width: collapseImage ? '100%' : '33%',
  }
}

const styleImageContainer = {
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  flex: 1,
  borderRadius: '0 1em 1em 1em',
  background: '#263f78',
  cursor: 'pointer',
  width: '66%',
  height: '35em',
  padding: '1em',
}

const styleImage = {
  display: 'flex',
  width: '100%',
  height: '100%',
  justifyContent: 'center',
}

const styleFeaturedImage = backgroundURL => {
  return {
    flex: 1,
    alignSelf: 'stretch',
    background: `url(${backgroundURL})`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'contain',
    backgroundPosition: 'center',
  }
}

class FeaturedDatasets extends React.Component {
  search = query => {
    const {submit, updateQuery} = this.props
    updateQuery(query)
    submit(query)
  }

  onClick(i) {
    const {featured} = this.props
    const {current} = this.state
    if (current === i) {
      this.search(featured[i].searchTerm)
    }
  }

  onEnter(i) {
    this.state.timer.pause()
    this.setState({current: i})
  }

  onLeave() {
    this.state.timer.resume()
  }

  setupTimer(items) {
    const {carouselLength, timer} = this.state

    if (items && (items.length || carouselLength !== items.length)) {
      this.setState({carouselLength: items.length})

      function Timer(callback, delay){
        var timerId,
          start,
          remaining = delay

        this.pause = () => {
          window.clearTimeout(timerId)
          remaining -= new Date() - start
        }

        this.resume = () => {
          start = new Date()
          window.clearTimeout(timerId)
          timerId = window.setTimeout(callback, remaining)
        }

        this.resume()
      }

      const self = this
      if (!timer)
        (function setTimerState(){
          self.setState({
            timer: new Timer(() => {
              const {carouselLength, current} = self.state
              const newCurrent = (current + 1) % carouselLength
              self.setState({current: newCurrent})
              setTimerState()
            }, 5000),
          })
        })()
    }
  }

  constructor(props) {
    super(props)

    this.state = {
      current: 0,
      carouselLength: 0,
      timer: undefined,
      collapseImage: false,
    }
  }

  componentWillReceiveProps({featured}) {
    this.setupTimer(featured)
  }

  componentDidMount() {
    const {featured} = this.props
    this.setupTimer(featured)

    // need to set dimensions intially before resize events
    this.debounceResize()
    // subsequent resize event will retrigger our calculation
    window.addEventListener('resize', this.debounceResize)
  }

  componentWillUnmount() {
    if (this.state.timer) {
      this.state.timer.pause()
    }

    // remember to remove custom listeners before unmounting
    window.removeEventListener('resize', this.debounceResize)
  }

  debounceResize = () => {
    // prevent resize work unless threshold is reached
    // this helps ensure the new width doesn't stick on a transient value
    const resizeThreshold = 250 // ms
    clearTimeout(this.resizeId)
    this.resizeId = setTimeout(this.resize, resizeThreshold)
  }

  resize = () => {
    // do work only if iframe exists
    if (this.featuredRef) {
      // get new width dynamically
      const featuredRect = this.featuredRef.getBoundingClientRect()
      const newWidth = parseFloat(featuredRect.width)
      // if the image's new width goes below our threshold, collapse
      this.setState(prevState => {
        return {
          ...prevState,
          collapseImage: newWidth < 700,
        }
      })
    }
  }

  render() {
    const {featured} = this.props
    const {current, collapseImage} = this.state

    if (featured !== null && featured.length > 0) {
      const titleList = (
        <ul key="title-list" style={styleTitleList(collapseImage)}>
          {featured.map((f, i, arr) => {
            const active = current === i
            const first = i === 0
            const last = i === arr.length - 1
            return (
              <li
                style={styleTitle(active, first, last, collapseImage)}
                key={i}
                onClick={() => this.onClick(i)}
                onMouseEnter={() => this.onEnter(i)}
                onMouseLeave={() => this.onLeave()}
              >
                <a onClick={() => this.onClick(i)}>{f.title}</a>
              </li>
            )
          })}
        </ul>
      )

      const imageContainer = (
        <div key="image-container" style={styleImageContainer}>
          {featured.map((f, i) => {
            const active = current === i
            if (active) {
              const backgroundURL = processUrl(f.imageUrl)
              return (
                <div key={i} style={styleImage}>
                  <div
                    title={f.title}
                    style={styleFeaturedImage(backgroundURL)}
                    onClick={() => this.onClick(i)}
                    onMouseEnter={() => this.onEnter(i)}
                    onMouseLeave={() => this.onLeave()}
                  />
                </div>
              )
            }
          })}
        </div>
      )

      const flexItems = collapseImage
        ? [ titleList ]
        : [ titleList, imageContainer ]

      return (
        <div
          aria-labelledby="featuredDatasets"
          style={styleFeaturedDatasetsWrapper}
        >
          <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
            Featured Data Sets
          </h2>
          <div
            ref={featuredRef => {
              this.featuredRef = featuredRef
            }}
            style={styleFeaturedDatasets}
          >
            <FlexRow items={flexItems} />
          </div>
          <br />
          <br />
        </div>
      )
    }
    else {
      return null
    }
  }
}

export default FeaturedDatasets
