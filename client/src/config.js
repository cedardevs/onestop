import fetch from 'isomorphic-fetch'

const configUrl = '/onestopConfig.json'

export const config = fetch(configUrl)
    .then(response => response.json())
    .catch(error => console.debug('no config file available'))

export default config