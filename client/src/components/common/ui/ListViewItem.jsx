import React, {useState, useEffect} from 'react'
import Expandable from './Expandable'
import FlexRow from './FlexRow'
import {boxShadow} from '../../../style/defaultStyles'
// import ExpandableFxnal from "../common/ui/Expandable";

const styleWrapper = focusing => {
  return {
    margin: '0 1.618em 0.618em 0',
    boxShadow: boxShadow,
    borderRadius: '0 0.309em 0.309em 0',
    backgroundColor: focusing ? 'rgb(207, 235, 253)' : 'white',
    transition: '0.3s background-color ease',
  }
}

const styleExpandableWrapper = {
  width: '100%',
}

// const styleExpandableHeadingFocused = {
//   textDecoration: 'underline',
//   outline: '2px dashed black',
// }

const styleExpandableHeading = open => {
  return {
    borderBottom: open ? '1px solid #EEE' : 'initial',
    justifyContent: 'space-between',
    padding: '0.618em',
    color: 'black',
  }
}

const styleExpandableArrowText = {fontSize: '0.8em', whiteSpace: 'nowrap'}

const styleExpandableArrowFocus = {
  outline: '2px dashed black',
  outlineOffset: '0.309em',
}

const styleActionPane = {
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
  boxShadow: boxShadow,
  padding: '0.309em',
  borderRadius: '0 0.309em 0.309em 0',
}

export function useListViewItem(props){
  // give the custom list view item component control over its expanded state
  const [ expanded, setExpanded ] = useState(false)

  // TODO: supply this ref from this effect
  // const focusRef = useRef(null)
  //
  // useEffect(() => {
  //   if (shouldFocus) {
  //     focusRef.current.focus()
  //   }
  // }, [])

  // if the ListView wants to control the items props directly
  // then we should set our items state according to that (if it has changed)
  useEffect(
    () => {
      if (props.expanded !== null && props.expanded !== expanded) {
        setExpanded(props.expanded)
      }
    },
    [ props.expanded ]
  )
  return [ expanded, setExpanded ]
}

export default function ListViewItem(props){
  const [ focusing, setFocusing ] = useState(false)

  const {itemId, item, heading, content, actions, expanded, setExpanded} = props

  const expandable = (
    <Expandable
      key={'ListItem::expandable'}
      styleWrapper={styleExpandableWrapper}
      showArrow={true}
      arrowTextClosed={'show details'}
      arrowTextOpened={'hide details'}
      styleArrowText={styleExpandableArrowText}
      styleArrowFocus={styleExpandableArrowFocus}
      alignArrow={true}
      heading={heading}
      headingTitle={item.title}
      styleHeading={styleExpandableHeading(expanded)}
      content={content}
      value={itemId}
      onToggle={event => {
        setExpanded(!expanded)
      }}
      open={expanded}
    />
  )

  const actionPane = actions ? (
    <div key={'ListItem::actionPane'} style={styleActionPane}>
      {actions}
    </div>
  ) : null

  return (
    <div
      style={styleWrapper(focusing)}
      onFocus={() => setFocusing(true)}
      onBlur={() => setFocusing(false)}
    >
      <FlexRow items={[ expandable, actionPane ]} />
    </div>
  )
}
