import React from 'react'
import Button from '../common/input/Button'
import Meta from '../helmet/Meta'
import InlineError from './InlineError'

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
  }
}

export default Error
