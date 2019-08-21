// import React from 'react'
// import _ from 'lodash'
// import search from 'fa/search.svg'
//
// import Button from '../../common/input/Button'
// // import FlexRow from '../../common/ui/FlexRow'
// import FlexColumn from '../../common/ui/FlexColumn'
// import TextSearchField from '../text/TextSearchField'
// import {SiteColors, FilterColors, FilterStyles, boxShadow2} from '../../../style/defaultStyles'
// import {times_circle, SvgIcon} from '../../common/SvgIcon'
//
// // import {
// //   FilterColors,
// //   FilterStyles,
// //   SiteColors,
// // } from '../../../style/defaultStyles'
//
// const styleButton = {
//   width: '35%',
// }
// const styleBreathingRoom = {
//   marginTop: '1em',
// }
//
// const styleButtonRow = {
//   display: 'flex',
//   flexDirection: 'row',
//   alignItems: 'center',
//   justifyContent: 'space-around',
//   marginBottom: '0.5em',
// }
// const styleForm = {
//   display: 'flex',
//   flexDirection: 'column',
// }
// const styleTextFilter = {
//   ...FilterStyles.MEDIUM,
//   ...{padding: '0.618em'},
// }
// const styleField = {
//   margin: '2px',
//   display: 'flex',
//   flexDirection: 'column',
//   alignItems: 'center',
//   justifyContent: 'space-around',
//   marginBottom: '0.25em',
// }
// const styleLabel = {
//   marginBottom: '0.25em',
// }
// const styleWrapper = {
//   height: '2em',
// }
//
// const styleInput = {
//   width: '15em',
//   color: FilterColors.TEXT,
//   height: '100%',
//   margin: 0,
//   padding: '0 0.309em',
//   border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
//   borderRadius: '0.309em',
// }
//
// // const styleSearchWrapper = {
// //   display: 'flex',
// //   height: '2.618em',
// //   justifyContent: 'center',
// // }
// //
// // const styleWarningCloseIcon = focusingWarningClose => {
// //   return {outline: focusingWarningClose ? '2px dashed white' : 'none'}
// // }
// //
// // const styleWarningClose = {
// //   alignSelf: 'center',
// //   background: 'none',
// //   border: 'none',
// //   outline: 'none',
// //   padding: '0.618em',
// // }
// //
// // const warningStyle = warning => {
// //   if (_.isEmpty(warning)) {
// //     return {
// //       display: 'none',
// //     }
// //   }
// //   else {
// //     return {
// //       position: 'absolute',
// //       top: 'calc(100% + 0.309em)',
// //       left: 0,
// //       right: 0,
// //       lineHeight: '1.618em',
// //       fontSize: '1em',
// //       color: 'white',
// //       fill: 'white',
// //       backgroundColor: SiteColors.WARNING,
// //       borderRadius: '0.309em',
// //       padding: '0.618em 0 0.618em 0.618em',
// //       boxShadow: boxShadow2,
// //       alignItems: 'center',
// //       justifyContent: 'space-between',
// //     }
// //   }
// // }
// //
// // const searchFieldStyle = {
// //   position: 'relative',
// //   margin: '.3em',
// //   alignSelf: 'center',
// // }
// //
// // const styleSearchButtonIcon = {
// //   width: '1.3em',
// //   height: '1.3em',
// //   paddingTop: '0.309em',
// //   paddingBottom: '0.309em',
// // }
// //
// // const styleSearchButton = {fontSize: '1em', display: 'inline'}
//
// class GranuleSearch extends React.Component {
//   constructor(props) {
//     super(props)
//     const {queryString} = this.props
//     this.state = {
//       queryString: queryString,
//       warning: '',
//       hoveringWarningClose: false,
//       focusingWarningClose: false,
//     }
//   }
//
//   handleMouseOverWarningClose = event => {
//     this.setState({
//       hoveringWarningClose: true,
//     })
//   }
//
//   handleInputChange = value => {
//     this.setState({
//       queryString: value,
//     })
//   }
//
//   handleMouseOutWarningClose = event => {
//     this.setState({
//       hoveringWarningClose: false,
//     })
//   }
//
//   handleFocusWarningClose = event => {
//     this.setState({
//       focusingWarningClose: true,
//     })
//   }
//
//   handleBlurWarningClose = event => {
//     this.setState({
//       focusingWarningClose: false,
//     })
//   }
//
//   clearQueryString = () => {
//     this.setState({warning: '', queryString: ''})
//     this.props.clear()
//   }
//
//     warningStyle() {
//       if (_.isEmpty(this.state.warning)) {
//         return {
//           display: 'none',
//         }
//       }
//       else {
//         return {
//           color: SiteColors.WARNING,
//           textAlign: 'center',
//           margin: '0.75em 0 0.5em',
//           fontWeight: 'bold',
//           fontSize: '1.15em',
//         }
//       }
//     }
//
//   validateAndSubmit = () => {
//     let trimmedQuery = _.trim(this.state.queryString)
//     if (!trimmedQuery) {
//       this.setState({warning: 'You must enter a search term.'})
//     }
//     else if (
//       trimmedQuery &&
//       (_.startsWith(trimmedQuery, '*') || _.startsWith(trimmedQuery, '?'))
//     ) {
//       this.setState({
//         warning: 'Search query cannot start with asterisk or question mark.',
//       })
//     }
//     else {
//       this.setState({warning: ''})
//       this.props.submit(trimmedQuery)
//     }
//   }
//
//   handleKeyDown = event => {
//     if (event.keyCode === Key.ENTER) {
//       event.preventDefault()
//       this.validateAndSubmit()
//     }
//   }
//
//     createApplyButton = () => {
//       return (
//         <Button
//           key="TextFilter::apply"
//           text="Apply"
//           title="Apply text filter"
//           onClick={this.validateAndSubmit}
//           style={styleButton}
//         />
//       )
//     }
//
//     createClearButton = () => {
//       return (
//         <Button
//           key="TextFilter::clear"
//           text="Clear"
//           title="Clear text filter"
//           onClick={this.clearQueryString}
//           style={styleButton}
//         />
//       )
//     }
//
//   render() {
//     const {warning, focusingWarningClose, queryString} = this.state
//
//     const instructionalCopy = 'Filter Granule Search'
//
//     // const searchButton = (
//     //   <Button
//     //     id="searchButton"
//     //     key="searchButton"
//     //     onClick={this.validateAndSubmit}
//     //     title={`Submit: ${instructionalCopy}`}
//     //     style={styleSearchButton}
//     //     styleIcon={styleSearchButtonIcon}
//     //   >
//     //     Apply
//     //   </Button>
//     // )
//
//
//     //
//     // const warningText = <div key="warning-text">{warning}</div>
//     //
//     // const warningClose = (
//     //   <button
//     //     key="warning-close-button"
//     //     style={styleWarningClose}
//     //     onClick={this.clearQueryString}
//     //     onMouseOver={this.handleMouseOverWarningClose}
//     //     onMouseOut={this.handleMouseOutWarningClose}
//     //     onFocus={this.handleFocusWarningClose}
//     //     onBlur={this.handleBlurWarningClose}
//     //     aria-label="close validation message"
//     //   >
//     //     <SvgIcon
//     //       size="2em"
//     //       style={styleWarningCloseIcon(focusingWarningClose)}
//     //       path={times_circle}
//     //     />
//     //   </button>
//     // )
//     //
//     // const warningPopup = (
//     //   <FlexRow
//     //     style={warningStyle(warning)}
//     //     items={[ warningText, warningClose ]}
//     //     role="alert"
//     //   />
//     // )
//
//     const styleTextBox = {minWidth: '6em', maxWidth: '100%'}
//     //
//     // <label>Filter by title:</label>
//     // <input
//     //   type="text"
//     //   id="granuleSearch"
//     //   name="granuleSearch"
//     //   placeholder={instructionalCopy}
//     //   aria-placeholder={instructionalCopy}
//     //   value={queryString}
//     //   style={styleTextBox}
//     //   onChange={this.handleInputChange}
//     // />
//     const id = 'granuleTextFilter'
//     const inputField = (
//       // <section style={searchFieldStyle}>
//       //   <div role="search" style={styleSearchWrapper}>
//       //     <TextSearchField
//       //       id="granuleSearch"
//       //       onEnterKeyDown={this.validateAndSubmit}
//       //       onChange={this.handleInputChange}
//       //       onClear={this.clearQueryString}
//       //       value={queryString}
//       //       warningPopup={warningPopup}
//       //       instructionalCopy={instructionalCopy}
//       //       textFieldStyle={styleTextBox}
//       //     />
//       //     {searchButton}
//       //   </div>
//       // </section>
//
//         <div style={styleField}>
//           <label style={styleLabel} htmlFor={id}>
//             Title Filter
//           </label>
//           <div style={styleWrapper}>
//             <input
//               type="text"
//               id={id}
//               name={id}
//               placeholder="Title Text Filter"
//               value={queryString}
//               onChange={event => {
//                 this.handleInputChange(event.target.name, event.target.value)
//               }}
//               style={styleInput}
//             />
//           </div>
//         </div>
//     )
//
//       const applyButton = this.createApplyButton()
//
//       const clearButton = this.createClearButton()
//
//     const inputColumn = (
//       <FlexColumn
//         items={[
//           <div key="TextFilterInput::all" style={styleBreathingRoom}>
//             <form
//               style={styleForm}
//               onKeyDown={this.handleKeyDown}
//             >
//               {inputField}
//             </form>
//           </div>,
//           <div key="TextFilter::InputColumn::Buttons" style={styleButtonRow}>
//             {applyButton}
//             {clearButton}
//           </div>,
//           <div
//             key="Text::InputColumn::Warning"
//             style={this.warningStyle()}
//             role="alert"
//           >
//             {this.state.warning}
//           </div>,
//         ]}
//       />
//     )
//     return (
//       <div style={styleTextFilter}>
//           {inputColumn}
//       </div>
//     )
//   }
// }
//
// export default GranuleSearch
