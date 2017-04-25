import fetch from 'isomorphic-fetch'

export const SET_INFO = 'set_info'
export const CLEAR_INFO = 'clear_info'

export const setInfo = (info) => {
    return {
        type: SET_INFO,
        info: info
    }
}

export const clearInfo = () => {
    return {
        type: CLEAR_INFO
    }
}

export const fetchInfo = () => {
    return (dispatch, getState) => {
        const apiHost = getState().domain.info.apiHost || ''
        const url = apiHost + '/onestop/api/info'
        console.log( "fetching info")
        const params = {headers: {'Accept': 'application/json'}}
        return fetch(url, params)
            .then(response => response.json())
            .then((json) => dispatch(setInfo(json)))
            .catch(error => console.debug('no info file available'))
    }
}
