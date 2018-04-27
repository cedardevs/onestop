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

    const samlRequest = "PD94bWwgdmVyc2lvbj0iMS4wIj8+CjxzYW1sMnA6QXV0aG5SZXF1ZXN0IHhtbG5z\n" +
        "OnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiBB\n" +
        "c3NlcnRpb25Db25zdW1lclNlcnZpY2VVUkw9Imh0dHBzOi8vc2NpYXBwcy5jb2xv\n" +
        "cmFkby5lZHUvU0FNTDIvUE9TVCIgRGVzdGluYXRpb249Imh0dHBzOi8vc3NvLWRl\n" +
        "di5sYi5jc3Aubm9hYS5nb3Y6NDQzL29wZW5hbS9TU09QT1NUL21ldGFBbGlhcy9u\n" +
        "b2FhLW9ubGluZS9ub2FhLW9ubGluZS1pZHAiIEZvcmNlQXV0aG49InRydWUiIElE\n" +
        "PSJwZnhmMDBmNTRhYS1lZWQzLTNhOTEtNTMxNC04OTk1YTBhMDVjYTQiIElzUGFz\n" +
        "c2l2ZT0iZmFsc2UiIElzc3VlSW5zdGFudD0iMjAxOC0wNC0yNlQxNzowMjoyNi45\n" +
        "NjBaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIu\n" +
        "MDpiaW5kaW5nczpIVFRQLUFydGlmYWN0IiBWZXJzaW9uPSIyLjAiPgogICAgPHNh\n" +
        "bWwyOklzc3VlciB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6\n" +
        "Mi4wOmFzc2VydGlvbiI+aHR0cHM6Ly9zY2lhcHBzLmNvbG9yYWRvLmVkdS9zcDwv\n" +
        "c2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cu\n" +
        "dzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgogIDxkczpTaWduZWRJbmZvPjxkczpD\n" +
        "YW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5v\n" +
        "cmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CiAgICA8ZHM6U2lnbmF0dXJlTWV0\n" +
        "aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2ln\n" +
        "I3JzYS1zaGExIi8+CiAgPGRzOlJlZmVyZW5jZSBVUkk9IiNwZnhmMDBmNTRhYS1l\n" +
        "ZWQzLTNhOTEtNTMxNC04OTk1YTBhMDVjYTQiPjxkczpUcmFuc2Zvcm1zPjxkczpU\n" +
        "cmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3ht\n" +
        "bGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3Jp\n" +
        "dGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48\n" +
        "L2RzOlRyYW5zZm9ybXM+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6\n" +
        "Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+PGRzOkRpZ2VzdFZh\n" +
        "bHVlPmpLK1kxN3B4Ulk2WWM5QXJaV1lCVjFLdllScz08L2RzOkRpZ2VzdFZhbHVl\n" +
        "PjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFs\n" +
        "dWU+S3FCaDNSQU9sd0lucllVVFBqenVCRnowQ1Fkb0hncDUwbmVHbE9ZWEorTDUz\n" +
        "U3l2dzlvR1RKNXpUcDBFUUJndnBHRlFtRnpwVWdGL0swNU4xamlZR0pXbnpHdVhz\n" +
        "UHg0bFVkZkdxUTRUN3Nvb0p1ak94WElpQ1BIOUd1K3VMYU9MYWl4QzlrWmNzNUZo\n" +
        "NUZaakpKTFl6bWVFakpQT2d2M2xBQ3Qzd1JjVGdBNy9GWVBNNHpaN09WR2tHdE1S\n" +
        "QVV0dVpkRWFQSndCam4wTUsra080S2VnTy9uVHlySzlvV1cvaTRzdGUramV6SUhJ\n" +
        "MTYzeTU2RFpzckdWaG5WdlhKS3luaW1aNTByMU5uKzF2ZUxHM2ZZUWpId0xCYmlo\n" +
        "NUNGT0JiSEx2dGRVTXRzV2pSNWpMeVA0UWVsSVhINGJlOVJkajdJeEp1aFBXaWFW\n" +
        "WElrZ3hxd3BBPT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6\n" +
        "WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlHRFRDQ0JQV2dBd0lCQWdJ\n" +
        "UWQ3Y29JL3kzNVlldnN3Ni9sT1JJaXpBTkJna3Foa2lHOXcwQkFRc0ZBRENCbGpF\n" +
        "TE1Ba0dBMVVFQmhNQ1IwSXhHekFaQmdOVkJBZ1RFa2R5WldGMFpYSWdUV0Z1WTJo\n" +
        "bGMzUmxjakVRTUE0R0ExVUVCeE1IVTJGc1ptOXlaREVhTUJnR0ExVUVDaE1SUTA5\n" +
        "TlQwUlBJRU5CSUV4cGJXbDBaV1F4UERBNkJnTlZCQU1UTTBOUFRVOUVUeUJTVTBF\n" +
        "Z1QzSm5ZVzVwZW1GMGFXOXVJRlpoYkdsa1lYUnBiMjRnVTJWamRYSmxJRk5sY25a\n" +
        "bGNpQkRRVEFlRncweE5qQTNNVFF3TURBd01EQmFGdzB4T1RBM01UUXlNelU1TlRs\n" +
        "YU1JSHhNUXN3Q1FZRFZRUUdFd0pWVXpFT01Bd0dBMVVFRVJNRk9EQXpNRGt4Q3pB\n" +
        "SkJnTlZCQWdUQWtOUE1SQXdEZ1lEVlFRSEV3ZENiM1ZzWkdWeU1SQXdEZ1lEVlFR\n" +
        "SkV3YzBOVFVnVlVOQ01Tb3dLQVlEVlFRS0V5RlZibWwyWlhKemFYUjVJRzltSUVO\n" +
        "dmJHOXlZV1J2SUdGMElFSnZkV3hrWlhJeERqQU1CZ05WQkFzVEJVTkpVa1ZUTVRR\n" +
        "d01nWURWUVFMRXl0SWIzTjBaV1FnWW5rZ1ZXNXBkbVZ5YzJsMGVTQnZaaUJEYjJ4\n" +
        "dmNtRmtieUJoZENCQ2IzVnNaR1Z5TVJBd0RnWURWUVFMRXdkVFIwTWdVMU5NTVIw\n" +
        "d0d3WURWUVFERXhSelkybGhjSEJ6TG1OdmJHOXlZV1J2TG1Wa2RUQ0NBU0l3RFFZ\n" +
        "SktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU05UnNtcStUOEVDdk5Z\n" +
        "Z0Y1c05lQWNlMCtBUml2d1RJdzNJQ3BnSEY3aGg2S2dhb1cySUhuQisyMG8yWkw3\n" +
        "V0ZmbVZoaEJvdjFaZFp6S3diYTY3WGdIajFGbVkrQXVaWk9nZml4eW9WeERDUW9F\n" +
        "ZkxPTE9CcjlUcysrTmpQamNpVFhlLzRrbHZwKzl0QmpKbkVBQnFqaWdJTTUvNmht\n" +
        "SnEwNE1oTnUvUFYwNlBLQ3UrNUlGTVpDbUpSOG5zdktxSVlLcFhoM0RZNWNPeCtp\n" +
        "eDBXNEI0QmNValMwUm5VemRDcEtDeUdqUlZMTXNWamhGZFp4aGx2aEtxL3FkZjBn\n" +
        "U3ZXZHlvSGNtaVRodUV5bngzNGlEcUh1TnRobkxENnpkcE85VEdxazdMTHptcS80\n" +
        "N2tuUHk5YmtKT3VsaFpHeU1ITTZIc0hjelQ1OGRmb0k2b3UwWkh6c0NBd0VBQWFP\n" +
        "Q0FmZ3dnZ0gwTUI4R0ExVWRJd1FZTUJhQUZKcnpLOXJQclUrMkw3c3FTRWdxRXJj\n" +
        "YlFzRWtNQjBHQTFVZERnUVdCQlRLZGVHRW11Y2JiaVdNUWE4c1c4bmRBZTYybVRB\n" +
        "T0JnTlZIUThCQWY4RUJBTUNCYUF3REFZRFZSMFRBUUgvQkFJd0FEQWRCZ05WSFNV\n" +
        "RUZqQVVCZ2dyQmdFRkJRY0RBUVlJS3dZQkJRVUhBd0l3VUFZRFZSMGdCRWt3UnpB\n" +
        "N0Jnd3JCZ0VFQWJJeEFRSUJBd1F3S3pBcEJnZ3JCZ0VGQlFjQ0FSWWRhSFIwY0hN\n" +
        "Nkx5OXpaV04xY21VdVkyOXRiMlJ2TG1OdmJTOURVRk13Q0FZR1o0RU1BUUlDTUZv\n" +
        "R0ExVWRId1JUTUZFd1Q2Qk5vRXVHU1doMGRIQTZMeTlqY213dVkyOXRiMlJ2WTJF\n" +
        "dVkyOXRMME5QVFU5RVQxSlRRVTl5WjJGdWFYcGhkR2x2YmxaaGJHbGtZWFJwYjI1\n" +
        "VFpXTjFjbVZUWlhKMlpYSkRRUzVqY213d2dZc0dDQ3NHQVFVRkJ3RUJCSDh3ZlRC\n" +
        "VkJnZ3JCZ0VGQlFjd0FvWkphSFIwY0RvdkwyTnlkQzVqYjIxdlpHOWpZUzVqYjIw\n" +
        "dlEwOU5UMFJQVWxOQlQzSm5ZVzVwZW1GMGFXOXVWbUZzYVdSaGRHbHZibE5sWTNW\n" +
        "eVpWTmxjblpsY2tOQkxtTnlkREFrQmdnckJnRUZCUWN3QVlZWWFIUjBjRG92TDI5\n" +
        "amMzQXVZMjl0YjJSdlkyRXVZMjl0TURrR0ExVWRFUVF5TURDQ0ZITmphV0Z3Y0hN\n" +
        "dVkyOXNiM0poWkc4dVpXUjFnaGgzZDNjdWMyTnBZWEJ3Y3k1amIyeHZjbUZrYnk1\n" +
        "bFpIVXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBSVVzOUhlWHh4cTR3aFQ0VWRZ\n" +
        "cDJBV0VnVjZsTE9WbzhGMEZ4aVJMQ0htWEdDem1oeUVMcE9wV2E0N0RGWFRxdTVa\n" +
        "L0RkTEdxTTZTYitxcEI0ZDIxOFJvd2dWVWZXS3F1NUVIVHpraXlWZlo3UjczUENn\n" +
        "M29ycTFabnQza2VwdHMyMTJiTXc4OFpvbHJ1dEdiUFNNVGc0bXQ2QUhxakVnRmtY\n" +
        "VktUTmxMWU9tTm9GYlFsVWxuSDg2a25INjJNSk5YMElaV3c0WFRnK2ZRRWhPd1FC\n" +
        "OHJ0UjVwRElJVzVRaXBGSWtHd1MzVVd2bWtRV2NFajhvbENiZ01ZVGRJM0QrVTZU\n" +
        "UkdUb0JicDZMK3o1VDdZUTBCVjUvWlk0S2p5RXFqRFBmTmNOdWVnZk5ZbFJCNGo2\n" +
        "UDhPL2piNnhTZTFiZTIrU0F5OCticExHMmhjbnRndXJvYXNzPTwvZHM6WDUwOUNl\n" +
        "cnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0\n" +
        "dXJlPgogICAgPHNhbWwycDpOYW1lSURQb2xpY3kgQWxsb3dDcmVhdGU9InRydWUi\n" +
        "IEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3Jt\n" +
        "YXQ6ZW1haWxBZGRyZXNzIiBTUE5hbWVRdWFsaWZpZXI9Imh0dHBzOi8vc2NpYXBw\n" +
        "cy5jb2xvcmFkby5lZHUiLz4KICAgIDxzYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250\n" +
        "ZXh0IENvbXBhcmlzb249Im1pbmltdW0iPgogICAgICAgIDxzYW1sMjpBdXRobkNv\n" +
        "bnRleHRDbGFzc1JlZiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNB\n" +
        "TUw6Mi4wOmFzc2VydGlvbiI+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFj\n" +
        "OmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L3NhbWwyOkF1dGhu\n" +
        "Q29udGV4dENsYXNzUmVmPgogICAgPC9zYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250\n" +
        "ZXh0Pgo8L3NhbWwycDpBdXRoblJlcXVlc3Q+Cg=="

    const relayState = ""

    return (
        <form method="post" action="https://sso-dev.lb.csp.noaa.gov:443/openam/SSOPOST/metaAlias/noaa-online/noaa-online-idp">
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