import React from 'react'

const styleTopicButton = {
  margin: '1em',
  background: 'none',
  display: 'block',
  fontWeight: 'bold',
  fontSize: '1.17em',
  border: 'none',
  textAlign: 'center',
  verticalAlign: 'middle',
  textDecoration: 'none',
}

const styleTopicButtonHover = {}

const styleTopicButtonFocus = {
  outline: '2px dashed white',
}

const styleTopicImage = {
  width: '5em',
  height: '5em',
  maxWidth: '100%',
  transition: 'transform 200ms',
}

const styleTopicImageHover = {
  transform: 'scale(1.25)',
  transformOrigin: 'center bottom',
}

const styleTopicImageFocus = {}

class TopicsMenuButton extends React.Component {
  componentWillMount() {
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

    const styleTopicButtonMerged = {
      ...styleTopicButton,
      ...(this.state.hovering ? styleTopicButtonHover : {}),
      ...(this.state.focusing ? styleTopicButtonFocus : {}),
    }

    const styleTopicImageMerged = {
      ...styleTopicImage,
      ...(this.state.hovering ? styleTopicImageHover : {}),
      ...(this.state.focusing ? styleTopicImageFocus : {}),
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
      >
        <img
          style={styleTopicImageMerged}
          src={topic.icon}
          alt={topic.title}
          aria-hidden="true"
        />
        <div>{topic.title}</div>
      </button>
    )
  }
}

export default TopicsMenuButton
