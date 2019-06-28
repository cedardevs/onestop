import React from 'react'
import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'
import _ from 'lodash'
import Meta from '../helmet/Meta'
import InlineError from './InlineError'

// const defaultError = {
//   title: 'Sorry, something has gone wrong',
//   detail:
//     'Looks like something has gone wrong on our end. Please try again later.',
// }
//
// const styleError = {
//   backgroundColor: '#E74C3C',
//   margin: '1.618em',
//   padding: '2em',
//   boxShadow: boxShadow,
// }
//
// const styleErrorHeading = {
//   margin: '0 0 0.618em 0',
//   padding: 0,
// }
//
// const styleErrorDescription = {
//   margin: '0 0 1.618em 0',
//   padding: 0,
// }

const styleActions = {
  display: 'flex',
  justifyContent: 'flex-end',
}

const styleHomeButton = {
  color: '#000',
  background: '#e6e6e6',
}

const styleHomeButtonHover = {
  color: '#000',
  background: 'linear-gradient(#e6e6e6, #b6b6b6)',
}

class Error extends React.Component {
  constructor(props) {
    super(props)

    this.errors = this.extractErrors(props)
    this.goBack = props.goBack.bind(this)
    this.goHome = props.goHome.bind(this)
  }

  componentWillReceiveProps(nextProps) {
    this.errors = this.extractErrors(nextProps)
  }

  // extractErrors(props) {
  //   return _.chain(this.getErrorsArray(props.errors))
  //     .map(this.normalizeError)
  //     .uniqWith((a, b) => a.title === b.title && a.detail === b.detail)
  //     .value()
  // }
  //
  // getErrorsArray(errors) {
  //   if (_.isArray(errors) && errors.length > 0) {
  //     return errors
  //   }
  //   else if (_.isObject(errors)) {
  //     return [ errors ]
  //   }
  //   else {
  //     return [ defaultError ]
  //   }
  // }
  //
  // normalizeError(error) {
  //   if (_.isError(error)) {
  //     return defaultError
  //   }
  //   else if (error.title) {
  //     return error
  //   }
  //   else if (error.message) {
  //     error.title = error.message
  //     return error
  //   }
  //   else {
  //     return defaultError
  //   }
  // }

  render() {
    const meta = <Meta title="Error" robots="noindex" />
    const action = (
      <div style={styleActions}>
        <Button
          text="Start a New Search"
          onClick={this.goHome}
          style={styleHomeButton}
          styleHover={styleHomeButtonHover}
        />
      </div>
    )
    return <InlineError errors={errors} meta={meta} action={action} />
    //     <div style={styleError}>
    //       <Meta title="Error" robots="noindex" />
    //       {this.errors.map((error, i) => {
    //         return (
    //           <div key={i}>
    //             <h2 style={styleErrorHeading}>{error.title}</h2>
    //             <p style={styleErrorDescription}>{error.detail}</p>
    //           </div>
    //         )
    //       })}
    //       <div style={styleActions}>
    //         <Button
    //           text="Start a New Search"
    //           onClick={this.goHome}
    //           style={styleHomeButton}
    //           styleHover={styleHomeButtonHover}
    //         />
    //       </div>
    //     </div>
    //   )
  }
}

export default Error
