import React, { PropTypes } from 'react'

export const UPDATE_TIME = 200
export const MAX_PROGRESS = 90
export const PROGRESS_INCREASE = 5
export const ANIMATION_TIME = UPDATE_TIME * 2

export class Loading extends React.Component {
  constructor(props) {
    super(props)

    this.state = this.getNewState()

    this.boundSimulateProgress = this.simulateProgress.bind(this)
    this.boundReset = this.reset.bind(this)
  }

  getNewState() {
    return {
      percent: 0,
      progressInterval: null,
      animationTimeout: null
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.loading > this.props.loading) {
      this.launch()
    }
  }

  componentWillUnmount() {
    clearInterval(this.state.progressInterval)
    clearTimeout(this.state.animationTimeout)
  }

  launch() {
    let { progressInterval, percent } = this.state
    const { animationTimeout } = this.state

    if (!progressInterval) {
      progressInterval = setInterval(
          this.boundSimulateProgress,
          this.props.updateTime
      )
      clearTimeout(animationTimeout)
      percent = 5
    }

    this.setState(Object.assign(this.state, {progressInterval, percent}))
  }

  simulateProgress() {
    let { progressInterval, percent, animationTimeout } = this.state

    if (percent === 100) {
      clearInterval(progressInterval)
      animationTimeout = setTimeout(this.boundReset, ANIMATION_TIME)
      progressInterval = null
    } else if (this.props.loading === 0) {
      percent = 100
    } else if (percent < this.props.maxProgress) {
      percent = percent + this.props.progressIncrease
    }

    this.setState({ percent, progressInterval, animationTimeout })
  }

  reset() {
    let { progressInterval, animationTimeout } = this.state
    if (progressInterval) {
      clearInterval(progressInterval)
    }
    if (animationTimeout) {
      clearTimeout(animationTimeout)
    }
    this.setState(this.getNewState())
  }


  buildStyle() {
    const shouldBeVisible = this.state.percent > 0 && this.state.percent < 100
    const style = {
      height: '2px',
      width: `${this.state.percent}%`,
      backgroundColor: 'white',
      transition: `width ${ANIMATION_TIME}ms ease-out,
                   height ${ANIMATION_TIME}ms linear,
                   opacity ${ANIMATION_TIME}ms ease-out`,
      position: 'absolute',
      opacity: shouldBeVisible ? '1' : '0',
    }

    return Object.assign(style, this.props.style)
  }

  render() {
    const style = this.buildStyle()

    return (
        <div>
          <div style={style} className={this.props.className} />
          <div style={{ display: 'table', clear: 'both' }} />
        </div>
    )
  }
}

Loading.propTypes = {
  style: PropTypes.object,
  className: PropTypes.string,
  actions: PropTypes.object,
  loading: PropTypes.number,
  updateTime: PropTypes.number,
  maxProgress: PropTypes.number,
  progressIncrease: PropTypes.number,
}

Loading.defaultProps = {
  style: {},
  className: undefined,
  loading: 0,
  updateTime: UPDATE_TIME,
  maxProgress: MAX_PROGRESS,
  progressIncrease: PROGRESS_INCREASE,
}

export default Loading
