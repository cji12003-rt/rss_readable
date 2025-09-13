# agent.md

## プロジェクト名

RSS読み上げニュースアプリ（個人用）

## 目的

Pixel 7a 等の Android 環境で、ユーザーが指定した RSS フィードを読み上げるMVPを迅速に実装するためのAI向け作業仕様書。

---

## 要約（短く）

* **MVPゴール**: アプリを起動時にパラメータで与えた RSS URL を読み込み、取得した記事タイトルを Text-to-Speech で**連続読み上げ**する。画面オフでも読み上げを継続すること。
* **パラメータ要件**: 起動 Intent やアプリ内設定で `rssUrl` を渡せること（MVPではコマンドライン/Intent パラメータまたはリソースで事前設定可）。

---

## 重要ポリシー（開発時の注意）

* ForegroundService を必ず用いる（通知表示必須）。
* Android のバッテリー最適化により挙動が変わるため、挙動不良時はユーザーへ設定画面で案内する文言を表示すること。
* 本プロジェクトは個人用途。外部公開や商用配布での追加要件は考慮しない。

---

## 優先順位（MVP → 第2 → 第3）

### MVP（必須）

1. 起動パラメータ `rssUrl` の受け取りと既定値の設定（例: `DEFAULT_RSS_URL`）。
2. RSS を取得して記事タイトルをパース（タイトルのみで良い）。
3. TTS による**連続読み上げ**（読み上げ完了時に次を再生）。
4. ForegroundService によるバックグラウンド動作（通知に「停止」ボタン）。
5. 基本的な UI（MainActivity）に再生/停止ボタンと取得したタイトル一覧表示。

### 第2フェーズ（中）

6. `SharedPreferences` による RSS URL の保存・編集機能（初回はパラメータで設定）。
7. 通知からの「一時停止／再開／スキップ」アクション実装。
8. 複数 RSS のサポート（リスト管理）。

### 第3フェーズ（低）

9. 正規表現による記事フィルタリング（読み飛ばし）。
10. RSS の `<description>` / `<content>` 等を用いた本文読み上げ。HTML パース処理。
11. 語句の読み替え（ユーザ辞書）や読み上げ設定（速度・音声）。

---

## 技術スタック

* 言語: Kotlin
* IDE: Android Studio
* Minimum API: 26 以上推奨（実際のデバイス Pixel 7a は API 33/34 を想定）
* ライブラリ: Jsoup（本文パースが必要な場合）または XmlPullParser（軽量）
* 永続化: SharedPreferences（MVP）→ Room（拡張時）
* TTS: Android `TextToSpeech` API
* サービス: `ForegroundService` + Notification (MediaStyle optional)

---

## アーキテクチャ（高レベル）

```
MainActivity (UI)  <-->  RssRepository  <-->  Network
                          |
                          v
                NewsReaderService (ForegroundService)
                          |
                          v
                    TtsManager (TextToSpeech wrapper)
```

### コンポーネント役割

* **MainActivity**: 起動パラメータ受け取り、UI表示、再生/停止操作を送信。MVPでは URL の手動入力は不要（パラメータと既定値で運用）。
* **RssRepository**: RSS の取得・パースを行い、`List<Article>` を返す。シンプルな cache を保持。
* **NewsReaderService**: ForegroundService。RssRepository から記事を受け取り、TtsManager に渡して読み上げを連続制御する。通知のアクションを受領する。
* **TtsManager**: `TextToSpeech` の初期化、読み上げ、コールバックのハンドリングを抽象化。

---

## インターフェース（Intent / Parameter）

* 起動 Intent extras:

  * `extra_rss_url` (String) — 優先される RSS URL。指定がない場合は `DEFAULT_RSS_URL` を使用。
  * `action_play`, `action_stop`, `action_skip` — Notification/Intent 経由で Service を操作するためのアクション定義。

---

## タスク一覧（AI agent に渡す単位）

各タスクは「入力」「出力」「受け入れ条件」を明示する。

### タスク A: プロジェクト雛形作成

* 入力: `package` 名、`DEFAULT_RSS_URL`（例: [https://example.com/feed）](https://example.com/feed）)
* 出力: Android Studio を使える Gradle プロジェクト、MainActivity、NewsReaderService、RssRepository、TtsManager のひな形ファイル。
* 受け入れ条件: アプリをビルドでき、MainActivity が起動する。

### タスク B: RSS 取得＆パース（RssRepository）

* 入力: `rssUrl` 文字列
* 出力: `List<Article>`（data class Article(val title\:String, val link\:String?, val pubDate\:String? )）
* 受け入れ条件: 指定 RSS のタイトルを 10 件まで取得して返す。ネットワーク失敗時に明示的な例外/エラーを返す。

### タスク C: TTS ラッパー（TtsManager）

* 入力: `String` を読み上げる API
* 出力: 読み上げ開始、終了コールバックが利用可能
* 受け入れ条件: 実機またはエミュレータ上で `speak("test")` が音声再生される。読み上げ完了でコールバックされる。

### タスク D: ForegroundService（NewsReaderService）

* 入力: `List<Article>` と `action_play/stop/skip` Intent
* 出力: 読み上げをサービスで継続。通知に「停止」ボタンを表示。
* 受け入れ条件: 再生開始後に画面オフにしても読み上げが継続する（少なくとも短時間のスリープで停止しないことを確認）。停止アクションでサービスが終了する。

### タスク E: UI 統合（MainActivity）

* 入力: `DEFAULT_RSS_URL` または `extra_rss_url` を用いて取得→サービスタート
* 出力: タイトル一覧の RecyclerView、再生/停止ボタンを表示
* 受け入れ条件: ユーザーが再生ボタンを押すと Service が起動して読み上げを開始すること。

### タスク F: テストケース作成

* 入力: 上記機能
* 出力: 手順化されたテストケース（受け入れ基準に沿う）
* 受け入れ条件: 全ての MVP 受け入れ条件を満たすテストが存在すること。

---

## 受け入れ基準（MVP）

* アプリ起動時に `extra_rss_url` が指定されていればそれを使い、なければ `DEFAULT_RSS_URL` を使用する。
* RSS のタイトルを取得し、MainActivity で一覧表示できる。
* 再生ボタンで読み上げが開始され、読み上げ完了後に自動で次の記事を読み上げる。
* 読み上げ中に画面をオフにしても（短時間のスリープで）読み上げが継続する。
* 通知の「停止」ボタンで読み上げが停止し、フォアグラウンドサービスが終了する。

---

## テストシナリオ（簡易）

1. 起動 with `extra_rss_url` → タイトル一覧が表示される
2. 再生開始 → TTS がタイトルを読み上げる
3. 画面オフにしても読み上げ継続（15秒程度で確認）
4. 通知の停止を押す → 読み上げ停止、通知消失
5. ネットワーク切断時の挙動確認 → エラーメッセージ表示／リトライ可能

---

## デリバリ（成果物）

* Git リポジトリの初期コミット（README、LICENSE、.gitignore）
* 実装ソースコード（MainActivity、NewsReaderService、RssRepository、TtsManager）
* 手動テスト手順書（上記シナリオ）

---

## 開発上の補足メモ

* ForegroundService の通知には `NotificationChannel` を確実に作成すること。
* Android 12 以降でのバックグラウンド制限に注意（必要に応じて `foregroundServiceType` を宣言）。
* 実機での検証は必須（エミュレータでは通知/電力最適化の違いがある）。
* 将来的にユーザー設定（SharedPreferences）を入れる場合は、データ構造を simple String list で持つと拡張しやすい。

---

## AI agent への指示例（テンプレート）

次のようなプロンプトで AI にタスクを投げると実行しやすい。

```
タスク: タスクB（RSS取得＆パース）を実装してください。
言語: Kotlin
入力: `rssUrl` (string)
出力: `List<Article>`
細かい要件:
 - タイトルを安全に取得すること（例外処理必須）
 - 最大 20 件まで返す
 - ネットワーク失敗時は `IOException` を返す
 - Unit test を 1 つ作成する
```

---

## 付録: 参考URL（実装時に確認する箇所）

* Android Foreground Service ガイド: [https://developer.android.com/guide/components/foreground-services](https://developer.android.com/guide/components/foreground-services)
* TextToSpeech ドキュメント: [https://developer.android.com/reference/android/speech/tts/TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
* XmlPullParser サンプル: [https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser](https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser)

---

以上。MVP を最優先に、段階的に拡張する想定で設計済み。必要なら、この agent.md をタスク分解して GitHub Issues 形式で出力します。
