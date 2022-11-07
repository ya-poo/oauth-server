# oauth-server

## Reference

### OAuth 2.0

* [The OAuth 2.0 Authorization Framework (RFC 6749)](https://www.rfc-editor.org/rfc/rfc6749)
* [The OAuth 2.0 Authorization Framework: Bearer Token Usage (RFC 6750)](https://www.rfc-editor.org/rfc/rfc6750)
* [OAuth 2.0 Token Revocation (RFC 7009)](https://www.rfc-editor.org/rfc/rfc7009)
* [Proof Key for Code Exchange by OAuth Public Clients (RFC 7636)](https://www.rfc-editor.org/rfc/rfc7636)
* [OAuth 2.0 Authorization Server Metadata (RFC 8414)](https://www.rfc-editor.org/rfc/rfc8414)
* [OAuth 2.0 Device Authorization Grant (RFC 8628)](https://www.rfc-editor.org/rfc/rfc8628)
* [OAuth 2.0 Authorization Server Issuer Identification (RFC 9207)](https://www.rfc-editor.org/rfc/rfc9207)

### OpenID Connect

* [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

## JWT の署名用キー生成

```
openssl genrsa -out key_rsa.pem 2048
openssl rsa -in key_rsa.pem -pubout -out key_rsa.pub
openssl pkcs8 -in key_rsa.pem -topk8 -nocrypt -out key_rsa.pk8
```
