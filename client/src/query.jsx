import queryString from 'query-string'
import store from './store'
import { triggerSearch } from './search/SearchActions'

const loadQuery = () => {
  const urlString = document.location.hash
  let queryParams = queryString.parse(urlString.slice(urlString.indexOf('?')+1,-1))
  for (const key in queryParams){
    if (key !== '_k') { // Don't query hash history param
      queryParams[key] = JSON.parse(queryParams[key])
    } else {
      delete(queryParams[key])
    }
  }
  store.dispatch(triggerSearch(JSON.stringify(queryParams)))
}

export default loadQuery
