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

    const samlRequest = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpBdXRoblJlcXVlc3QgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L1NBTUwyL1BPU1QiIERlc3RpbmF0aW9uPSJodHRwczovL3Nzby1kZXYubGIuY3NwLm5vYWEuZ292OjQ0My9vcGVuYW0vU1NPUE9TVC9tZXRhQWxpYXMvbm9hYS1vbmxpbmUvbm9hYS1vbmxpbmUtaWRwIiBJRD0iU0NJQVBQU18xNjk4OWM3NC0xYWRmLTRjOGMtODc2OC1mNmI3NDM1ZDY2M2IiIElzc3VlSW5zdGFudD0iMjAxOC0wNS0wOFQxODo0Mjo0MC4xNTdaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpiaW5kaW5nczpIVFRQLVBPU1QiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHBzOi8vc2NpYXBwcy5jb2xvcmFkby5lZHUvc3A8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj4KPGRzOlNpZ25lZEluZm8+CjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4KPGRzOlJlZmVyZW5jZSBVUkk9IiNTQ0lBUFBTXzE2OTg5Yzc0LTFhZGYtNGM4Yy04NzY4LWY2Yjc0MzVkNjYzYiI+CjxkczpUcmFuc2Zvcm1zPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPgo8L2RzOlRyYW5zZm9ybXM+CjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz4KPGRzOkRpZ2VzdFZhbHVlPlFxazFNUFc0cXpvM2hQMHhKSDlkZW5VbnJpUEFuNTBGMlZnQmJOeG5BMzg9PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjwvZHM6U2lnbmVkSW5mbz4KPGRzOlNpZ25hdHVyZVZhbHVlPgprZittQlZKL1JyTzZCc09rYUxVUFBVU3N2c3VTaUM4YTJZRXBKb1psNnJBejJMbEx4RlVXTEpIZUtTWG90aGNuS1JBd3JuWVExS1BnCjhGUjE1QUJ6NjBERFc4bjZEMW1zeGdtQzBKMllEY2JOZU4vNElpMHpibW83WjFKcWpZWXFMNVkwdHpncEN4ak12T2JwK1BOSXhCWUQKeVdpV2xNN3dXQUUyQVpYY0NzbVdJSjBhRGU2T1ZhWmwwTDBQUnBTNHExUHRtNkMydHhpRk5FM3JOa1dIanlSRjFuMHAvRVVYNGI1Qwp5OW1Xa2xoVEdaT015cFcwK0ZpZjhRMURMNHNYVXYrdXZIZXhVdFVaZjk0ZWFFckFQSzVKTDlyMHBkaTV3Q05iMGhuNnQwZHg0KytPCm1zbldlNDhEb3ZtTTVueDQrVlRuZDhHOHhqMVpMRnJRbjFxVW1nPT0KPC9kczpTaWduYXR1cmVWYWx1ZT4KPGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF6MUd5YXI1UHdRSzgxaUFYbXcxNEJ4N1Q0QkdLL0JNagpEY2dLbUFjWHVHSG9xQnFoYllnZWNIN2JTalprdnRZVitaV0dFR2kvVmwxbk1yQnRycnRlQWVQVVdaajRDNWxrNkIrTEhLaFhFTUpDCmdSOHM0czRHdjFPejc0Mk0rTnlKTmQ3L2lTVytuNzIwR01tY1FBR3FPS0Fnem4vcUdZbXJUZ3lFMjc4OVhUbzhvSzc3a2dVeGtLWWwKSHlleThxb2hncWxlSGNOamx3N0g2TEhSYmdIZ0Z4U05MUkdkVE4wS2tvTElhTkZVc3l4V09FVjFuR0dXK0VxcitwMS9TQks5WjNLZwpkeWFKT0c0VEtmSGZpSU9vZTQyMkdjc1ByTjJrNzFNYXFUc3N2T2FyL2p1U2MvTDF1UWs2NldGa2JJd2N6b2V3ZHpOUG54MStnanFpCjdSa2ZPd0lEQVFBQjwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6TmFtZUlEUG9saWN5IEFsbG93Q3JlYXRlPSJ0cnVlIiBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjEuMTpuYW1laWQtZm9ybWF0OmVtYWlsQWRkcmVzcyIgU1BOYW1lUXVhbGlmaWVyPSJodHRwczovL3NjaWFwcHMuY29sb3JhZG8uZWR1L3NwIi8+PHNhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQgQ29tcGFyaXNvbj0ibWluaW11bSI+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250ZXh0Pjwvc2FtbDJwOkF1dGhuUmVxdWVzdD4="
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