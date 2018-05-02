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

    const samlRequest = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpBdXRoblJlcXVlc3QgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L1NBTUwyL1BPU1QiIERlc3RpbmF0aW9uPSJodHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L29uZXN0b3AvIy9jb2xsZWN0aW9ucz9xPVNTTyIgRm9yY2VBdXRobj0idHJ1ZSIgSUQ9IlNDSUFQUFNfYTdmN2YxNjgtODI5ZC00ZDY3LWE0NjMtZjViODRmNzI2NjE0IiBJc1Bhc3NpdmU9ImZhbHNlIiBJc3N1ZUluc3RhbnQ9IjIwMTgtMDUtMDJUMjM6MjM6MTIuODE4WiIgUHJvdG9jb2xCaW5kaW5nPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YmluZGluZ3M6SFRUUC1BcnRpZmFjdCIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwyOklzc3VlciB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cHM6Ly9zY2lhcHBzLmNvbG9yYWRvLmVkdS9zcDwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbz4KPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPgo8ZHM6UmVmZXJlbmNlIFVSST0iI1NDSUFQUFNfYTdmN2YxNjgtODI5ZC00ZDY3LWE0NjMtZjViODRmNzI2NjE0Ij4KPGRzOlRyYW5zZm9ybXM+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPgo8ZHM6RGlnZXN0VmFsdWU+VDVHOTVlTmNEQ0RjSW1BeDZtYmEvaDFxb2c4NWludDV2OEl4OGU2SjIyND08L2RzOkRpZ2VzdFZhbHVlPgo8L2RzOlJlZmVyZW5jZT4KPC9kczpTaWduZWRJbmZvPgo8ZHM6U2lnbmF0dXJlVmFsdWU+ClVCWTZ2TG9JWStlMEtyYTgxekx6VXE3WVNiaEdWSDlIalh5QkhPNlRrSmxsM1d5Q05wRld0cUxqMWxKTFFXNm5nbWppUU4rY3hHZmEKM2JTWllBR20xRjZ3cFN3MWxvUWo5Z01aUlEyc2hvSy9BalZpNkp0bmRKUzRrZzRWZmUrakVvZDJVMmhMUXk2dTJWZ3NwWEtiOHRzVgp0MHMxWm5mYThzYkJZaGNpY3FieGNxR1RiZWsvY2VRN0VRS2QvOEdIaUtYQVBNU1hUOVJMVk5iY01HUHlOVGkwRVRxTWJFZENJVndLClorOWgvRzZsOWdVQzhqbWlrMlc4MzlyQTFCVEpveXM1dmUxbkFLKzBHRUM4eUoxTXRtSVRNbEc1cUw5TVVZd2ZjLytNWm5raStEM1UKek1ZK2IvdVlJNjFWVWhTbW1VRDh1c0hPL1QvK2ZtVGdsMDVNclE9PQo8L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXoxR3lhcjVQd1FLODFpQVhtdzE0Qng3VDRCR0svQk1qCkRjZ0ttQWNYdUdIb3FCcWhiWWdlY0g3YlNqWmt2dFlWK1pXR0VHaS9WbDFuTXJCdHJydGVBZVBVV1pqNEM1bGs2QitMSEtoWEVNSkMKZ1I4czRzNEd2MU96NzQyTStOeUpOZDcvaVNXK243MjBHTW1jUUFHcU9LQWd6bi9xR1ltclRneUUyNzg5WFRvOG9LNzdrZ1V4a0tZbApIeWV5OHFvaGdxbGVIY05qbHc3SDZMSFJiZ0hnRnhTTkxSR2RUTjBLa29MSWFORlVzeXhXT0VWMW5HR1crRXFyK3AxL1NCSzlaM0tnCmR5YUpPRzRUS2ZIZmlJT29lNDIyR2NzUHJOMms3MU1hcVRzc3ZPYXIvanVTYy9MMXVRazY2V0ZrYkl3Y3pvZXdkek5QbngxK2dqcWkKN1JrZk93SURBUUFCPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwycDpOYW1lSURQb2xpY3kgQWxsb3dDcmVhdGU9InRydWUiIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6ZW1haWxBZGRyZXNzIiBTUE5hbWVRdWFsaWZpZXI9Imh0dHBzOi8vc2NpYXBwcy5jb2xvcmFkby5lZHUvc3AiLz48c2FtbDJwOlJlcXVlc3RlZEF1dGhuQ29udGV4dCBDb21wYXJpc29uPSJtaW5pbXVtIj48c2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWYgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPnVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0PC9zYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQ+PC9zYW1sMnA6QXV0aG5SZXF1ZXN0Pg=="
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