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
        "PSJwZnhmYzEzYTFkMC0wOWNkLTg2MzItZmVlMi0xYjNkYjliNTk5N2YiIElzUGFz\n" +
        "c2l2ZT0iZmFsc2UiIElzc3VlSW5zdGFudD0iMjAxOC0wNC0zMFQyMjoxODo0Ny43\n" +
        "MzNaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIu\n" +
        "MDpiaW5kaW5nczpIVFRQLUFydGlmYWN0IiBWZXJzaW9uPSIyLjAiPjxkczpTaWdu\n" +
        "YXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNp\n" +
        "ZyMiPgogIDxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9k\n" +
        "IEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMx\n" +
        "NG4jIi8+CiAgICA8ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDov\n" +
        "L3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIi8+CiAgPGRzOlJl\n" +
        "ZmVyZW5jZSBVUkk9IiNwZnhmYzEzYTFkMC0wOWNkLTg2MzItZmVlMi0xYjNkYjli\n" +
        "NTk5N2YiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJo\n" +
        "dHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25h\n" +
        "dHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9y\n" +
        "Zy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48L2RzOlRyYW5zZm9ybXM+PGRzOkRp\n" +
        "Z2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkv\n" +
        "eG1sZHNpZyNzaGExIi8+PGRzOkRpZ2VzdFZhbHVlPjNlMGxtcTNrOFBXTnhSMktV\n" +
        "QmN2UEg5blNTRT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6\n" +
        "U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+TkxRTWFWaCt3Z2YvN1NqRmJX\n" +
        "eDlXVmQ4L0ZncUF6YjVqZXhiWkh5RnA0Qks4ZWN4dHl0K2UzR2ljWlpUVWhya2o3\n" +
        "WHdVN0JXT3czVXZJTml3RUJaZ0xXaktIN2k3RVd3VGMzZUFJTzVTaVZvTVVKVXZi\n" +
        "TmYrNTFNYW12a2I5VDZLMG1sZEFidFpEdjhpN3BzL1RYYjVoVzI4T1hjS3AvZTVQ\n" +
        "bzY2ajd0cyt4Rk9DV2sxejNVMWRQY3hUZ1loRkxWdnorN1hBK0JKVHhXYXlFYTMw\n" +
        "dHpmWHlqdWx3cjFkbTQyUS9rY0tPcGVMd3NzMFpzRjhBK0UzQU9QTTVUM2ZWS1VR\n" +
        "cGhxZkRHVVo3MWlTV1hQOVBlRFRlemlhb29ZcHNIbW00dmJrd3AxaXBMZys2Ukpw\n" +
        "d0JxMjJtWVlnbEYwMHFwbjZGcUJDdWpOaGk3K3Jxb0JIVWZRPT08L2RzOlNpZ25h\n" +
        "dHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0\n" +
        "aWZpY2F0ZT4xNC02ODpTQU1MZWxsaW90dCRsZXNzZXhhbXBsZTJfYXV0aG5SZXF1\n" +
        "ZXN0MTQtNjg6U0FNTGVsbGlvdHQkY2xlYXJNSUlHRFRDQ0JQV2dBd0lCQWdJUWQ3\n" +
        "Y29JL3kzNVlldnN3Ni9sT1JJaXpBTkJna3Foa2lHOXcwQkFRc0ZBRENCbGpFTE1B\n" +
        "a0dBMVVFQmhNQ1IwSXhHekFaQmdOVkJBZ1RFa2R5WldGMFpYSWdUV0Z1WTJobGMz\n" +
        "UmxjakVRTUE0R0ExVUVCeE1IVTJGc1ptOXlaREVhTUJnR0ExVUVDaE1SUTA5TlQw\n" +
        "UlBJRU5CSUV4cGJXbDBaV1F4UERBNkJnTlZCQU1UTTBOUFRVOUVUeUJTVTBFZ1Qz\n" +
        "Sm5ZVzVwZW1GMGFXOXVJRlpoYkdsa1lYUnBiMjRnVTJWamRYSmxJRk5sY25abGNp\n" +
        "QkRRVEFlRncweE5qQTNNVFF3TURBd01EQmFGdzB4T1RBM01UUXlNelU1TlRsYU1J\n" +
        "SHhNUXN3Q1FZRFZRUUdFd0pWVXpFT01Bd0dBMVVFRVJNRk9EQXpNRGt4Q3pBSkJn\n" +
        "TlZCQWdUQWtOUE1SQXdEZ1lEVlFRSEV3ZENiM1ZzWkdWeU1SQXdEZ1lEVlFRSkV3\n" +
        "YzBOVFVnVlVOQ01Tb3dLQVlEVlFRS0V5RlZibWwyWlhKemFYUjVJRzltSUVOdmJH\n" +
        "OXlZV1J2SUdGMElFSnZkV3hrWlhJeERqQU1CZ05WQkFzVEJVTkpVa1ZUTVRRd01n\n" +
        "WURWUVFMRXl0SWIzTjBaV1FnWW5rZ1ZXNXBkbVZ5YzJsMGVTQnZaaUJEYjJ4dmNt\n" +
        "RmtieUJoZENCQ2IzVnNaR1Z5TVJBd0RnWURWUVFMRXdkVFIwTWdVMU5NTVIwd0d3\n" +
        "WURWUVFERXhSelkybGhjSEJ6TG1OdmJHOXlZV1J2TG1Wa2RUQ0NBU0l3RFFZSktv\n" +
        "WklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU05UnNtcStUOEVDdk5ZZ0Y1\n" +
        "c05lQWNlMCtBUml2d1RJdzNJQ3BnSEY3aGg2S2dhb1cySUhuQisyMG8yWkw3V0Zm\n" +
        "bVZoaEJvdjFaZFp6S3diYTY3WGdIajFGbVkrQXVaWk9nZml4eW9WeERDUW9FZkxP\n" +
        "TE9CcjlUcysrTmpQamNpVFhlLzRrbHZwKzl0QmpKbkVBQnFqaWdJTTUvNmhtSnEw\n" +
        "NE1oTnUvUFYwNlBLQ3UrNUlGTVpDbUpSOG5zdktxSVlLcFhoM0RZNWNPeCtpeDBX\n" +
        "NEI0QmNValMwUm5VemRDcEtDeUdqUlZMTXNWamhGZFp4aGx2aEtxL3FkZjBnU3ZX\n" +
        "ZHlvSGNtaVRodUV5bngzNGlEcUh1TnRobkxENnpkcE85VEdxazdMTHptcS80N2tu\n" +
        "UHk5YmtKT3VsaFpHeU1ITTZIc0hjelQ1OGRmb0k2b3UwWkh6c0NBd0VBQWFPQ0Fm\n" +
        "Z3dnZ0gwTUI4R0ExVWRJd1FZTUJhQUZKcnpLOXJQclUrMkw3c3FTRWdxRXJjYlFz\n" +
        "RWtNQjBHQTFVZERnUVdCQlRLZGVHRW11Y2JiaVdNUWE4c1c4bmRBZTYybVRBT0Jn\n" +
        "TlZIUThCQWY4RUJBTUNCYUF3REFZRFZSMFRBUUgvQkFJd0FEQWRCZ05WSFNVRUZq\n" +
        "QVVCZ2dyQmdFRkJRY0RBUVlJS3dZQkJRVUhBd0l3VUFZRFZSMGdCRWt3UnpBN0Jn\n" +
        "d3JCZ0VFQWJJeEFRSUJBd1F3S3pBcEJnZ3JCZ0VGQlFjQ0FSWWRhSFIwY0hNNkx5\n" +
        "OXpaV04xY21VdVkyOXRiMlJ2TG1OdmJTOURVRk13Q0FZR1o0RU1BUUlDTUZvR0Ex\n" +
        "VWRId1JUTUZFd1Q2Qk5vRXVHU1doMGRIQTZMeTlqY213dVkyOXRiMlJ2WTJFdVky\n" +
        "OXRMME5QVFU5RVQxSlRRVTl5WjJGdWFYcGhkR2x2YmxaaGJHbGtZWFJwYjI1VFpX\n" +
        "TjFjbVZUWlhKMlpYSkRRUzVqY213d2dZc0dDQ3NHQVFVRkJ3RUJCSDh3ZlRCVkJn\n" +
        "Z3JCZ0VGQlFjd0FvWkphSFIwY0RvdkwyTnlkQzVqYjIxdlpHOWpZUzVqYjIwdlEw\n" +
        "OU5UMFJQVWxOQlQzSm5ZVzVwZW1GMGFXOXVWbUZzYVdSaGRHbHZibE5sWTNWeVpW\n" +
        "TmxjblpsY2tOQkxtTnlkREFrQmdnckJnRUZCUWN3QVlZWWFIUjBjRG92TDI5amMz\n" +
        "QXVZMjl0YjJSdlkyRXVZMjl0TURrR0ExVWRFUVF5TURDQ0ZITmphV0Z3Y0hNdVky\n" +
        "OXNiM0poWkc4dVpXUjFnaGgzZDNjdWMyTnBZWEJ3Y3k1amIyeHZjbUZrYnk1bFpI\n" +
        "VXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBSVVzOUhlWHh4cTR3aFQ0VWRZcDJB\n" +
        "V0VnVjZsTE9WbzhGMEZ4aVJMQ0htWEdDem1oeUVMcE9wV2E0N0RGWFRxdTVaL0Rk\n" +
        "TEdxTTZTYitxcEI0ZDIxOFJvd2dWVWZXS3F1NUVIVHpraXlWZlo3UjczUENnM29y\n" +
        "cTFabnQza2VwdHMyMTJiTXc4OFpvbHJ1dEdiUFNNVGc0bXQ2QUhxakVnRmtYVktU\n" +
        "TmxMWU9tTm9GYlFsVWxuSDg2a25INjJNSk5YMElaV3c0WFRnK2ZRRWhPd1FCOHJ0\n" +
        "UjVwRElJVzVRaXBGSWtHd1MzVVd2bWtRV2NFajhvbENiZ01ZVGRJM0QrVTZUUkdU\n" +
        "b0JicDZMK3o1VDdZUTBCVjUvWlk0S2p5RXFqRFBmTmNOdWVnZk5ZbFJCNGo2UDhP\n" +
        "L2piNnhTZTFiZTIrU0F5OCticExHMmhjbnRndXJvYXNzPTwvZHM6WDUwOUNlcnRp\n" +
        "ZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJl\n" +
        "PgogICAgPHNhbWwycDpOYW1lSURQb2xpY3kgQWxsb3dDcmVhdGU9InRydWUiIEZv\n" +
        "cm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6\n" +
        "ZW1haWxBZGRyZXNzIiBTUE5hbWVRdWFsaWZpZXI9Imh0dHBzOi8vc2NpYXBwcy5j\n" +
        "b2xvcmFkby5lZHUvc3AiLz4KICAgIDxzYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250\n" +
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