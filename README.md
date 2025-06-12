# Health Connect Import

Android Health Connectからヘルスデータをインポートするためのサンプルアプリケーションです。

## 機能

- Health Connectアプリとの連携
- ヘルスデータの読み取り権限の要求
- 以下のデータタイプに対応：
  - 歩数データ
  - 心拍数データ
  - 距離データ
  - 運動セッションデータ
  - 睡眠セッションデータ

## 技術仕様

- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose
- **最小APIレベル**: 24 (Android 7.0)
- **ターゲットAPIレベル**: 34 (Android 14)
- **主要ライブラリ**:
  - androidx.health.connect:connect-client
  - androidx.compose.material3
  - androidx.activity:activity-compose

## セットアップ

1. Android Studio Hedgehog 以降を使用してプロジェクトを開く
2. プロジェクトをビルドする
3. Android 14 (API 34) 以降が搭載されたデバイスまたはエミュレーターで実行する

## Health Connect について

Health Connect は Google が提供するAndroidのヘルス・フィットネスデータのプラットフォームです。複数のアプリからのデータを一元管理し、ユーザーのプライバシーを保護しながらデータ共有を可能にします。

## 注意事項

- このアプリを実行するには、デバイスにHealth Connectアプリがインストールされている必要があります
- 初回実行時にヘルスデータの読み取り権限を許可する必要があります
- 実際のヘルスデータを取得するには、Health Connectアプリでデータソースアプリを設定する必要があります

## 開発者向け情報

このプロジェクトは学習・開発の参考用として作成されています。実際のアプリ開発では、エラーハンドリング、権限の適切な管理、UIの改善などが必要になります。 