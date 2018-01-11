import React, {Component} from 'react'

const styleContainer = (even, hovering) => {
  return {
    backgroundColor: hovering ? '#18478F' : even ? '#222' : '#111',
    color: 'white'
  }
}

export default class GranuleViewTableRow extends Component {
  componentWillMount() {
    this.setState({
      hovering: false,
    })
  }

  handleRowMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleRowMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
      }
    })
  }

  handleToggleFocus = event => {
    const { id, toggleFocus } = this.props
    console.log("handleRowToggleFocus:::toggleFocus:", toggleFocus)
    console.log("handleRowToggleFocus:::id:", id)
    toggleFocus(id, true)
  }

  handleToggleUnfocus = event => {
    const { id, toggleFocus } = this.props
    console.log("handleRowToggleUnfocus:::toggleFocus:", toggleFocus)
    console.log("handleRowToggleUnfocus:::id:", id)
    toggleFocus(id, false)
  }

  render() {
    const { style, styleTitle, styleBadges, title, badges, even } = this.props
    const { hovering } = this.state
    const styles = Object.assign({}, styleContainer(even, hovering), style)
    return (
      <tr
        style={styles}
        onMouseOver={this.handleRowMouseOver}
        onMouseOut={this.handleRowMouseOut}
        onClick={() => console.log("onClick")}
        onMouseEnter={this.handleToggleFocus}
        onMouseLeave={this.handleToggleUnfocus}
      >
        <td style={styleTitle}>{title}</td>
        <td style={styleBadges}>{badges}</td>
      </tr>
    )
  }
}
