# 掲示板サービスAPI
## 開発の始め方

### リソースの取得と実行方法
```text
# リソースの取得
> git clone https://github.com/louvre2489/json-server.git

> cd json-server

# 実行
# マルチプロジェクトなので、プロジェクト直下で`sbt run`しても動かない
> sbt 'project infrastructure' run

# curlで動作確認
> curl -X POST https://localhost:5000/api/v1/user/create -H "Content-Type: application/json" -d '{"mailAddress": "abc@xyz.co.jp", "userName": "dummy", "password":"1234567890"}' --insecure
{"userId":30,"userName":"dummy","mailAddress":"abc@xyz.co.jp"}

# スレッド/ポストの操作に対してはアクセストークンが要求されます。
# ログイン時に払い出されるアクセストークンをヘッダにセットしてリクエスト要求を行ってください。
curl -X GET -H "accessToken:9999" https://localhost:5000/api/v1/threads/0/posts/0 --insecure
```
