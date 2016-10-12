export const LOADING_SHOW = 'LOADING_SHOW'
export const LOADING_HIDE = 'LOADING_HIDE'

export const showLoading = () => {
  return {
    type: LOADING_SHOW
  }
}

export const hideLoading = () => {
  return {
    type: LOADING_HIDE
  }
}