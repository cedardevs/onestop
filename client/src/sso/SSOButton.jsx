import React from 'react'
import Button from '../common/input/Button'
import lockIcon from 'fa/lock.svg'

const styleButton = {maxWidth: "fit-content", alignSelf: "flex-end", fontSize: "1em", marginLeft: "1em", padding: "0.309em 0.618em 0.309em 0.309em"}

const styleButtonIcon = {marginRight: "0.309em", width: "1.618em", height: "1.618em"}

export default class SSOButton extends React.Component {

  // login = () => {
  //   let xhr = new XMLHttpRequest();
  //   xhr.open("POST", '/server', true);
  //
  //   //Send the proper header information along with the request
  //   xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  //
  //   xhr.onload = function () {
  //     console.log(this.responseText)
  //   }
  //
  //   //
  //   // xhr.onreadystatechange = function() {//Call a function when the state changes.
  //   //   if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
  //   //     // Request finished. Do processing here.
  //   //
  //   //   }
  //   //   xhr.send(`SAMLRequest=${samlRequest}&RelayState=${relayState}");
  // }

  render() {

    const samlRequest = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpBdXRoblJlcXVlc3QgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L1NBTUwyL1BPU1QiIERlc3RpbmF0aW9uPSJodHRwczovL3d3dy5nb29nbGUuY29tIiBGb3JjZUF1dGhuPSJ0cnVlIiBJRD0iU0NJQVBQU19hMTgwMjkwNi00YTczLTQxMjgtOGY5Yi0xMDUzYWFhOGFhZjgiIElzUGFzc2l2ZT0iZmFsc2UiIElzc3VlSW5zdGFudD0iMjAxOC0wNS0wMlQyMjo1MTo1Mi41ODhaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpiaW5kaW5nczpIVFRQLUFydGlmYWN0IiBWZXJzaW9uPSIyLjAiIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDI6SXNzdWVyIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5odHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L3NwPC9zYW1sMjpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CjxkczpTaWduZWRJbmZvPgo8ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPgo8ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIi8+CjxkczpSZWZlcmVuY2UgVVJJPSIjU0NJQVBQU19hMTgwMjkwNi00YTczLTQxMjgtOGY5Yi0xMDUzYWFhOGFhZjgiPgo8ZHM6VHJhbnNmb3Jtcz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPC9kczpUcmFuc2Zvcm1zPgo8ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+CjxkczpEaWdlc3RWYWx1ZT5YckgyY0hXRUxCKzZONWVoMDhYaEloL082SnFTSnpLeEdLU1RoVXpNYnFFPTwvZHM6RGlnZXN0VmFsdWU+CjwvZHM6UmVmZXJlbmNlPgo8L2RzOlNpZ25lZEluZm8+CjxkczpTaWduYXR1cmVWYWx1ZT4KQjFlbTRXZXJZVk5QRXhBdUw2L1VUNWlzZmdrem96emxkdUpVRFZnWEFDU2JqaFJQT0FlSWdMRHptVXZlQ0MvS3hWZmxPOXBQa2IvUgpQa01GSGtaVFQwOVczaWFPM0JlaDZXTklsSENpRGFmSHN0RXpJa1VOV0JjV2RhcnhDTDZvLzV2V283ZGhHTkNDTlk5UFRXSzl0RlF2CmVaQjQwaGU0UHZNTC9vTlYwQVdpNXdmSy9GbXFIRFhuU2p6MjlhbXFSWTNjeW5SaFhMQkhxRzdKVUVuaWRxUWlrY0xLMVlFcUpsR2wKOHZ5R0swd2NENGtuTkJYcVVRRW82SDBUTVJmZmhHWGhOS2YzTFdsSWtwbUJOTUpJOFBvQmFTa0lkN29Vemo0R0kyaHFmOGxOMno2eQpSOFh0VjdkckRFeVVXUkp0dUYxSVdLSk9tVS9oZ3FWV3Njdk9oUT09CjwvZHM6U2lnbmF0dXJlVmFsdWU+CjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBejFHeWFyNVB3UUs4MWlBWG13MTRCeDdUNEJHSy9CTWoKRGNnS21BY1h1R0hvcUJxaGJZZ2VjSDdiU2paa3Z0WVYrWldHRUdpL1ZsMW5NckJ0cnJ0ZUFlUFVXWmo0QzVsazZCK0xIS2hYRU1KQwpnUjhzNHM0R3YxT3o3NDJNK055Sk5kNy9pU1crbjcyMEdNbWNRQUdxT0tBZ3puL3FHWW1yVGd5RTI3ODlYVG84b0s3N2tnVXhrS1lsCkh5ZXk4cW9oZ3FsZUhjTmpsdzdINkxIUmJnSGdGeFNOTFJHZFROMEtrb0xJYU5GVXN5eFdPRVYxbkdHVytFcXIrcDEvU0JLOVozS2cKZHlhSk9HNFRLZkhmaUlPb2U0MjJHY3NQck4yazcxTWFxVHNzdk9hci9qdVNjL0wxdVFrNjZXRmtiSXdjem9ld2R6TlBueDErZ2pxaQo3UmtmT3dJREFRQUI8L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDJwOk5hbWVJRFBvbGljeSBBbGxvd0NyZWF0ZT0idHJ1ZSIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDplbWFpbEFkZHJlc3MiIFNQTmFtZVF1YWxpZmllcj0iaHR0cHM6Ly9zY2lhcHBzLmNvbG9yYWRvLmVkdS9zcCIvPjxzYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250ZXh0IENvbXBhcmlzb249Im1pbmltdW0iPjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L3NhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPjwvc2FtbDJwOlJlcXVlc3RlZEF1dGhuQ29udGV4dD48L3NhbWwycDpBdXRoblJlcXVlc3Q+"

    const relayState = ""

    return (
        <form method="post" action="https://sso-dev.lb.csp.noaa.gov:8443/openam/SSOPOST/metaAlias/noaa-online/noaa-online-idp">
          <input type="hidden" name="SAMLRequest" value={samlRequest} />
          <input type="hidden" name="RelayState" value={relayState} />
          <input type="submit" value="SSO" />
        </form>
    )

    // return (
    //     <Button
    //         text="SSO"
    //         icon={lockIcon}
    //         style={styleButton}
    //         styleIcon={styleButtonIcon}
    //         onClick={this.login}
    //     />
    // )
  }
}