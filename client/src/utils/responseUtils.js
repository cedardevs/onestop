export const checkForErrors = response => {
  if (response.status < 200 || response.status >= 400) {
    let error = new Error(response.statusText)
    error.response = response

    throw error
  }
  else {
    return response
  }
}
