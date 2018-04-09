import React, {Component} from 'react'
import FlexRow from '../common/FlexRow'
import {fontFamilySerif} from '../utils/styleUtils'

// <TabButton>

const styleTabButton = (active, first = false, last = false) => {
  return {
    display: 'flex',
    flexGrow: 1,
    marginRight: last ? 0 : '1px',
    fontSize: '0.8em',
    color: active ? '#000131' : 'white',
    backgroundColor: active ? 'white' : '#6e91b2',
    textAlign: 'center',
    border: `1px solid ${active ? 'gray' : '#F0F0F2'}`,
    borderRadius: '0.618em 0.618em 0 0',
    borderBottom: active ? 'none' : 'initial',
  }
}

const styleTabButtonInput = () => {
  return {
    display: 'none',
  }
}

const styleTabButtonLabel = active => {
  return {
    width: '100%',
    height: '100%',
    fontSize: '1.4em',
    padding: '0.618em',
    cursor: 'pointer',
    fontFamily: fontFamilySerif(),
  }
}

class TabButton extends Component {
  render() {
    const {first, last, title, value, active, onChange} = this.props
    const tabID = `${title} - ${value}`

    return (
      <div style={styleTabButton(active, first, last)}>
        <input
          style={styleTabButtonInput()}
          id={tabID}
          type="radio"
          name={title}
          value={value}
          checked={active}
          onChange={onChange}
        />
        <label style={styleTabButtonLabel(active)} htmlFor={tabID}>
          {title}
        </label>
      </div>
    )
  }
}

// <TabButton/>

// <Tabs>

const styleTabs = {
  marginTop: '1.618em',
}

const styleTabButtons = {
  flexWrap: 'nowrap',
  flexShrink: 0,
  position: 'sticky',
  top: '0',
  width: 'fit-content',
  justifyContent: 'space-between',
}

const styleContentDefault = {
  borderTop: '1px solid gray',
}

export default class Tabs extends Component {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  componentWillMount() {
    this.setState(prevState => {
      return {
        ...prevState,
        activeIndex: this.props.activeIndex ? this.props.activeIndex : 0,
      }
    })
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.activeIndex !== this.props.activeIndex) {
      this.setState(prevState => {
        return {
          ...prevState,
          activeIndex: this.props.activeIndex,
        }
      })
    }
  }

  handleChange(event) {
    const index = Number(event.currentTarget.value)
    if (this.props.data[index].action) {
      this.props.data[index].action()
    }
    this.setState(prevState => {
      return {
        ...prevState,
        activeIndex: index,
      }
    })
  }

  render() {
    const {data, styleContent} = this.props

    const styleContentMerged = {
      ...styleContentDefault,
      ...styleContent,
    }
    let tabButtons = []
    let tabContent = null
    if (data) {
      data.forEach((tab, index) => {
        let active = false
        if (index === this.state.activeIndex) {
          active = true
          tabContent = tab.content
        }
        tabButtons.push(
          <TabButton
            key={index}
            first={index === 0}
            last={index + 1 === data.length}
            title={tab.title}
            value={index}
            active={active}
            onChange={this.handleChange}
          />
        )
      })
    }
    return (
      <div style={styleTabs}>
        <FlexRow items={tabButtons} style={styleTabButtons} />
        <div style={styleContentMerged}>{tabContent}</div>
      </div>
    )
  }
}

// <Tabs/>
