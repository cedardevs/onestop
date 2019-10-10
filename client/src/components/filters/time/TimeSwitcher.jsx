import React, {useState, useEffect} from 'react'

// import Button from '../../common/input/Button'
//
// import FlexRow from '../../common/ui/FlexRow'
import RadioButtonTabs from './RadioButtonTabs'

import {
  FilterColors,
  FilterStyles,
  SiteColors,
} from '../../../style/defaultStyles'

const VIEW_OPTIONS = [
  {
    value: 'standard',
    label: 'Datetime',
  },
  {
    value: 'geologic',
    label: 'Geologic',
  },
  // {
  //   value: 'periodic',
  //   label: 'Periodic',
  // },
]

// const DEFAULT_VIEW = VIEW_OPTIONS[0].value

const TimeSwitcher = props => {
  // expect props matching the values of VIEW_OPTIONS
  // const [ activeView, setActiveView ] = useState(DEFAULT_VIEW)
  const [ view, setView ] = useState(null)
  // const [ focus, setFocus ] = useState(null)

  // const switchView = () => {
  //   if (activeView == 'standard') setActiveView('geologic')
  //   else if (activeView == 'geologic') setActiveView('standard')
  // }

  // useEffect(
  //   () => {
  //     console.log('focus changed?', focus)
  //   },
  //   [focus]
  // )

  // useEffect(
  //   () => {
  //     setView(props[activeView]) // TODO needs some mild 508 alert about the active view changing
  //   },
  //   [ activeView ]
  // )

  //   const radioButtons = []
  //   _.each(VIEW_OPTIONS, (option, index) => {
  //     // TODO this is the same generator method as format, although with key names, state variable names, and styling it might not really be extractable when I'm done?
  //     // TODO check that display none vs accessibility stuff! - not even in tab order right now! can't do focus style!
  //     const id = `TimeFilterView${option.value}`
  //     const selected = activeView == option.value
  //     const focused = focus == option.value
  //     // const style = selected? {justifyContent: 'center',
  //     // alignItems: 'center',
  //     // color: 'white',
  //     // background: '#277CB2', // $color_primary
  //     // borderRadius: '0.309em',
  //     // border: 'transparent',
  //     // textAlign: 'center',
  //     // padding: '0.309em',
  //     // margin: '0 0.309em',
  //     // fontSize: '1.05em',} :{}
  //     const style = {
  //       ...{ // default
  //         display: 'inline-block',
  //         // borderRadius: '0.309em',
  //         backgroundColor: '#277CB2',
  //         padding: '0.309em 0.618em',//'10px 20px',
  //         borderWidth: '1px',
  //         borderStyle: 'solid',
  //         borderColor: FilterColors.DARK,
  //         color: FilterColors.INVERSE_TEXT,
  //         // border: '2px solid #444',
  //       },
  //       ...(selected
  //         ? focused ? {textDecoration: 'underline' , backgroundColor: FilterColors.DARK} : {backgroundColor: FilterColors.DARK}
  //         : {} ),//focused ? {color: 'green'} : {color: 'yellow'}),
  //         ...((index<(VIEW_OPTIONS.length-1))? {borderRight:'0'}:{}),
  //         ...((index==0)? { borderRadius: '0.309em 0 0 0.309em'} :{}),
  //         ...((index==VIEW_OPTIONS.length-1)? {borderRadius: '0 0.309em 0.309em 0'} : {}),
  //     } // note: green never shows - you change selection with arrow keys not tab!
  //     // TODO make sure onBlur junk works correctly for other browsers!
  //     console.log('lsadfasdf',FilterColors)
  //     console.log(VIEW_OPTIONS.length, index, (index<(VIEW_OPTIONS.length-1)), index>0)
  //     radioButtons.push(
  //       <div key={`TimeFilter::View::${option.value}`}>
  //         <label htmlFor={id} style={style}>
  //           {option.label}
  //         </label>
  //         <input
  //           type="radio"
  //           id={id}
  //           style={{
  //             opacity: 0,
  //             position: 'fixed',
  //             width: 0,
  //           }}
  //           name="timefilterview"
  //           value={option.value}
  //           checked={selected}
  //           onChange={e => setActiveView(e.target.value)}
  //           onFocus={e => setFocus(e.target.value)}
  //           onBlur={e => setFocus(null)}
  //         />
  //       </div>
  //     )
  //   })
  //
  //   const styleSwitcherRadioButtons = {
  //   margin: '0.309em auto',
  //   padding: '0.309em',
  //   justifyContent: 'center', //'space-around',
  // }
  return (
    <div>
      <RadioButtonTabs
        inputName="timefilterview"
        OPTIONS={VIEW_OPTIONS}
        callback={activeViewName => {
          setView(props[activeViewName])
        }}
      />
      {view}
    </div>
  )
}
export default TimeSwitcher
