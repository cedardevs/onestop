import React from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {Key} from '../../../utils/keyboardUtils'
import {fontFamilySerif} from '../../../utils/styleUtils'

const styleTabButton = (active, first = false, last = false) => {
  return {
    display: 'flex',
    flexGrow: 1,
    marginRight: last ? 0 : '1px',
    fontSize: '0.8em',
    color: active ? 'white' : '#000131',
    backgroundColor: active ? '#3c6280' : 'white',
    textAlign: 'center',
    border: `1px solid ${active ? '#F0F0F2' : 'gray'}`,
    borderRadius: '0.618em 0.618em 0 0',
    borderBottom: active ? 'none' : 'initial',
    outline: 'none',
  }
}

const styleTabButtonInput = () => {
  return {
    display: 'none',
  }
}

const styleTabButtonLabel = {
  width: '100%',
  height: '100%',
  fontSize: '1.4em',
  padding: '0.618em',
  cursor: 'pointer',
  fontFamily: fontFamilySerif(),
}

const styleFocusDefault = (focused, active) => {
  return {
    padding: '0.105em 0.309em',
    outline: focused
      ? active ? '2px dashed white' : '2px dashed #6e91b2'
      : 'none',
  }
}

class TabButton extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      focusing: false,
    }
  }

  handleClick = event => {
    event.preventDefault()
    this.handleChange()
  }

  handleChange = event => {
    this.props.onChange({currentTarget: {value: this.props.value}})
  }

  handleKeyUp = e => {
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      this.handleChange()
    }
    if (e.keyCode === Key.ENTER) {
      this.handleChange()
    }
  }
  handleKeyDown = e => {
    const tabControlKeys = [ Key.SPACE, Key.ENTER ]
    if (
      !e.metaKey &&
      !e.shiftKey &&
      !e.ctrlKey &&
      !e.altKey &&
      tabControlKeys.includes(e.keyCode)
    ) {
      e.preventDefault()
    }
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      first,
      last,
      title,
      value,
      active,
      onChange,
      tabWrapperId,
      tabContentId,
      tabId,
    } = this.props
    const tabIndex = 0
    const styleFocused = styleFocusDefault(this.state.focusing, active)

    return (
      <div
        role="tab"
        style={styleTabButton(active, first, last)}
        aria-selected={active}
        aria-controls={tabContentId}
        tabIndex={tabIndex}
        onKeyUp={this.handleKeyUp}
        onKeyDown={this.handleKeyDown}
        onClick={this.handleClick}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        id={tabWrapperId}
      >
        <input
          style={styleTabButtonInput()}
          id={tabId}
          type="radio"
          name={title}
          value={value}
          checked={active}
          onChange={onChange}
        />
        <label style={styleTabButtonLabel} htmlFor={tabId}>
          <span style={styleFocused}>{title}</span>
        </label>
      </div>
    )
  }
}

const styleTabs = {
  margin: '1.618em 0 0 0',
  fontWeight: 'normal',
  fontSize: '1em',
}

const styleTabButtons = {
  flexWrap: 'nowrap',
  flexShrink: 0,
  position: 'sticky',
  top: '0',
  // width: 'fit-content',  // if enabled: tabs only take up the space they need (instead of full page width)
  justifyContent: 'space-between',
}

const styleContentDefault = {
  borderTop: '1px solid gray',
}

export default class Tabs extends React.Component {
  constructor(props) {
    super(props)
  }

  UNSAFE_componentWillMount() {
    this.setState(prevState => {
      return {
        ...prevState,
        activeIndex: this.props.activeIndex ? this.props.activeIndex : 0,
      }
    })
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.activeIndex !== this.props.activeIndex) {
      this.setState(prevState => {
        return {
          ...prevState,
          activeIndex: this.props.activeIndex,
        }
      })
    }
  }

  updateCurrentTab = index => {
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

  handleChange = event => {
    const index = Number(event.currentTarget.value)
    this.updateCurrentTab(index)
  }

  render() {
    const {data, styleContent} = this.props

    const styleContentMerged = {
      ...styleContentDefault,
      ...styleContent,
    }
    let tabButtons = []
    let tabContent = null
    let tabContentLabelledBy = null
    // let tabId = null
    let tabContentId = 'no-tab-content'
    if (data) {
      let activeTab = data[this.state.activeIndex]
      let tabId = `${activeTab.title}-${this.state.activeIndex}`
      tabContent = activeTab.content
      tabContentId = `${tabId}-content`
      tabContentLabelledBy = `${tabId}-wrapper`

      data.forEach((tab, index) => {
        let active = false
        let tabId = `${tab.title}-${index}`
        let tabPanelId
        let tabWrapperId
        // let tabWrapperId = `${tabId}-wrapper`
        if (index === this.state.activeIndex) {
          active = true
          tabWrapperId = tabContentLabelledBy
          tabPanelId = tabContentId
        }
        tabButtons.push(
          <TabButton
            key={index}
            first={index === 0}
            last={index + 1 === data.length}
            title={tab.title}
            tabId={tabId}
            tabWrapperId={tabWrapperId}
            tabContentId={tabPanelId}
            value={index}
            active={active}
            onChange={this.handleChange}
          />
        )
      })
    }
    return (
      <div>
        <h2 style={styleTabs}>
          <FlexRow
            rowId="details-tablist"
            tabIndex="-1"
            role="tablist"
            items={tabButtons}
            style={styleTabButtons}
          />
        </h2>

        <div
          role="tabpanel"
          id={tabContentId}
          aria-labelledby={tabContentLabelledBy}
          style={styleContentMerged}
        >
          {tabContent}
        </div>
      </div>
    )
  }
}
