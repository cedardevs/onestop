import React from 'react'
import styles from './section508.css'

class Section508LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
    this.state = {
      formFields: [
        { label: 'Search Text', name: 'search-text', placeholder: 'e.g. ocean', action: {}},
        { label: 'Start Date', name: 'start-date', placeholder: 'e.g. 1940-02-01T00:00:00Z', action: {}},
        { label: 'End Date', name: 'end-date', placeholder: 'e.g. 2017-02-13T00:00:00Z', action: {}},
        { label: 'Bounding Box', name: 'geometry', placeholder: `e.g. -180.00,-90.00,180.00,90.00 (W,S,E,N)`, action: {}}
      ]
    }
  }

  search(query) {
    this.updateQuery(query);
    this.submit(query);
  }

  render() {
    const { formFields } = this.state
    let form = formFields.map(field => {
      return <div className={styles.formRow}>
        <label htmlFor={field.name} className={styles.formLabel}>{field.label}</label>
        <input type="text" className={styles.formInput} name={field.name} id={field.name} placeholder={field.placeholder}/>
      </div>
    })
    const searchButton = <button className={`${styles.button} pure-button`}
      onclick={this.submit}>Search</button>
    const clearButton = <button className={`${styles.button} pure-button`}>Clear</button>

    return( <div className={`${styles.form} pure-form`}>
      {form}
      {searchButton}
      {clearButton}
    </div> )
  }

  componentDidMount() {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 0);
  }
}

export default Section508LandingComponent
