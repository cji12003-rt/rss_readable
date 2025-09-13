# rss_readable
# RSS読み上げニュースアプリ (MVP)

**概要**

個人用の Android アプリ（Pixel 7a での利用を想定）。起動時にパラメータで指定した RSS フィード（`rssUrl`）から記事を取得し、記事タイトルを Text‑to‑Speech で順次読み上げます。画面オフでも読み上げが継続することを MVP の最優先目標とします。

---

## 現状のゴール（MVP）

* 起動 Intent またはアプリ内の既定値で指定された `rssUrl` を読み込む
* 取得した記事タイトルを一覧表示
* タイトルを TTS で連続読み上げ（読み上げ完了で次へ）
* `ForegroundService` を用いて画面オフでも読み上げ継続
* 通知に「停止」ボタンを表示し、停止操作でサービスを終了できる

---

## 主要機能

* RSS取得・パース（タイトル優先）
* TextToSpeech による連続読み上げ
* ForegroundService（通知）でバックグラウンド継続
* 起動パラメータ `extra_rss_url` によるフィード指定

---

## 前提条件 / 必要な環境

* Kotlin + Android Studio
* 実機検証推奨（Pixel 7a 等）
* Android SDK (API 26 以上を推奨)

---

## クイックスタート（開発者用）

1. リポジトリをクローンして Android Studio で開く
2. `DEFAULT_RSS_URL` を `app/src/main/java/.../Constants.kt`（または同等の場所）で設定する
3. 実機を接続してビルド・インストール

### adb を使った起動（起動パラメータを渡す例）

```bash
# パッケージ名/Activity は実装に合わせて置換してください
adb shell am start -n com.example.rssreader/.MainActivity --es extra_rss_url "https://example.com/feed"
```

※ 上記で起動すると `extra_rss_url` が優先的に利用されます。指定が無ければ `DEFAULT_RSS_URL` が使用されます。

---

## 実機でのバッテリー最適化回避（Pixel 7a 等）

Android のバッテリー最適化により ForegroundService の稼働が制限される場合があります。MVP 検証時には以下を確認／案内してください。

1. 設定 → アプリ → （本アプリ）→ バッテリー
2. バッテリー最適化 (Battery optimization) で「最適化しない」に設定

アプリ側では、初回起動時またはサービス起動時にユーザーへバッテリー最適化無効化を促す画面／ダイアログを用意することを推奨します。

---

## 動作確認（MVP向けテスト手順）

1. 起動時に `extra_rss_url` を渡してアプリを起動 → タイトル一覧が表示される
2. 再生ボタンを押す → TTS がタイトルを読み上げる
3. 画面をオフにして 15～30 秒程度待つ → 読み上げが継続する
4. 通知の「停止」ボタンを押す → 読み上げ停止、通知が消える
5. ネットワークを切断 → エラーメッセージ表示やリトライ可能であること

---

## 推奨プロジェクト構成（例）

```
app/
 ├─ src/
 │   ├─ main/
 │   │   ├─ java/com/example/rssreader/
 │   │   │   ├─ MainActivity.kt
 │   │   │   ├─ NewsReaderService.kt
 │   │   │   ├─ RssRepository.kt
 │   │   │   ├─ TtsManager.kt
 │   │   │   └─ model/Article.kt
 │   │   └─ res/
 │   │       └─ layout/
 │   └─ AndroidManifest.xml
 └─ build.gradle
```

---

## 設定 (定数 / Intent extras)

* `DEFAULT_RSS_URL` — アプリに組み込む初期値フィード
* Intent extra: `extra_rss_url` (String) — 起動時に優先される RSS URL
* Intent actions: `action_play`, `action_stop`, `action_skip` — 通知/Intent 経由で Service を操作する際に使用

---

## 将来の拡張案（優先度順）

1. `SharedPreferences` による RSS URL 保存・編集（第2フェーズ）
2. 通知からの「一時停止」「スキップ」アクション
3. 複数RSSのサポート（登録/管理）
4. 正規表現による記事フィルタリング
5. 記事本文の取得・本文読み上げ
6. 語句の読み替え（ユーザ辞書）・速度/声質設定

---

## テスト・デバッグのヒント

* ForegroundService の挙動は実機で確認すること（エミュレータと実機で差が出ることがある）
* Notification チャネルの作成を忘れない（API 26+）
* TTS の初期化は非同期のため、初回発話前に初期化完了を待つロジックが必要

---

## ライセンス

* MIT ライセンス

---

## 次のアクション提案

* agent.md をもとに GitHub Issue を自動生成して欲しい場合は実行します。
* あるいは、まずはプロジェクトのコード雛形（MainActivity, Service, RssRepository, TtsManager のテンプレート）を生成できます。

---

