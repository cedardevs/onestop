import { connect } from 'react-redux'
import ResultLayout from './ResultLayout'

const mapStateToProps = (state) => {
  return {
    count: state.get('results').count()
  }
}

const ResultLayoutContainer = connect(mapStateToProps)(ResultLayout)

export default ResultLayoutContainer