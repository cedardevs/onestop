import React from 'react'
import {fontFamilySerif} from '../../utils/styleUtils'

const styleTopicButton = {
  padding: '1em',
  background: 'none',
  display: 'block',
  fontWeight: 'bold',
  fontSize: '1.17em',
  border: 'none',
  textAlign: 'center',
  verticalAlign: 'middle',
  textDecoration: 'none',
}

const styleTopicButtonFocus = {
  outline: '2px dashed #5C87AC',
}

const styleTopicImage = {
  width: '5em',
  height: '5em',
  maxWidth: '100%',
  transition: 'transform 200ms',
}

const styleTopicImageHover = {
  transform: 'scale(1.25)',
}

const styleTopicButtonTitle = {
  fontFamily: fontFamilySerif(),
  color: 'black',
  marginTop: '0.309em',
  transition: 'transform 200ms',
}

const styleTopicButtonTitleHover = {
  transform: 'translate(0, 0.618em)',
}

class TopicsMenuButton extends React.Component {
  UNSAFE_componentWillMount() {
    this.setState({
      hovering: false,
      focusing: false,
    })
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
      }
    })
  }

  handleFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {topic, onClick} = this.props
    const ariaLabel = `${topic.title} Data Search`
    const styleTopicButtonMerged = {
      ...styleTopicButton,
      ...(this.state.focusing ? styleTopicButtonFocus : {}),
    }

    const styleTopicImageMerged = {
      ...styleTopicImage,
      ...(this.state.hovering ? styleTopicImageHover : {}),
    }

    const styleTopicButtonTitleMerged = {
      ...styleTopicButtonTitle,
      ...(this.state.hovering ? styleTopicButtonTitleHover : {}),
    }

    return (
      <button
        style={styleTopicButtonMerged}
        onClick={() => onClick(topic.term)}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onMouseDown={this.handleMouseDown}
        onMouseUp={this.handleMouseUp}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        aria-label={ariaLabel}
      >
        <img
          style={styleTopicImageMerged}
          src={topic.icon}
          alt=""
          aria-hidden="true"
          width="5em"
          height="5em"
        />
        <div style={styleTopicButtonTitleMerged}>{topic.title}</div>
      </button>
    )
  }
}

export default TopicsMenuButton
