# Health Connect CSV Importer

Android Health ConnectにZepp LifeのCSVファイルから体組成データをインポートするためのツールアプリです。

## 機能

- CSVファイルによる体組成データの一括インポート
- Health Connectアプリとの連携と書き込み権限の要求
- 対応データ：
  - 体重 (Weight)
  - 身長 (Height)
  - 体脂肪率 (Body Fat)
  - 骨量 (Bone Mass)
  - 基礎代謝 (Basal Metabolic Rate)
  - 除脂肪体重 (Lean Body Mass)
- インポート結果の表示（成功件数、失敗件数、エラー詳細）

## CSVフォーマット

インポートするCSVファイルは以下の仕様に準拠している必要があります。

- **文字コード**: UTF-8
- **区切り文字**: カンマ (`,`)
- **ヘッダー行**: 必須。以下のキーが含まれている必要があります。
  - `time`: 測定時刻 (例: `2024-05-20 08:30:00+0900`)
  - `weight`: 体重 (kg)
  - `height`: 身長 (cm)
  - `fatRate`: 体脂肪率 (%)
  - `boneMass`: 骨量 (kg)
  - `metabolism`: 基礎代謝 (kcal)
  - `muscleRate`: 筋肉率 (%) ※除脂肪体重の計算に使用
- **必須項目**: `time`, `weight`, `height`
- **データがない場合**: `null` または空欄としてください。

### フォーマット例

**ヘッダー:**
```
time,weight,height,fatRate,boneMass,metabolism,muscleRate
```

**データ行:**
```
2024-05-20 08:30:00+0900,75.5,175.0,22.1,3.2,1700,74.5
2024-05-21 08:32:00+0900,75.2,175.0,null,null,1695,null
```

## 技術仕様

- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose
- **最小APIレベル**: 26 (Android 8.0)
- **ターゲットAPIレベル**: 34 (Android 14)
- **主要ライブラリ**:
  - `androidx.health.connect:connect-client`
  - `androidx.compose.material3`
  - `androidx.activity:activity-compose`
  - `androidx.lifecycle:lifecycle-runtime-ktx`

## セットアップ

リポジトリをクローンし、Android Studioで開いてください。

### ビルド

デバッグビルド (APK) の場合:
```bash
./gradlew assembleDebug
```

リリースビルド (AAB) の場合:
```bash
./gradlew bundleRelease
```

Dockerを使用する場合:
```bash
# ビルダーイメージの作成
docker build -t android-builder:latest .

# デバッグビルド (APK)
docker run --rm -v "${PWD}:/app" -w /app android-builder:latest ./gradlew assembleDebug

# リリースビルド (AAB)
docker run --rm -v "${PWD}:/app" -w /app android-builder:latest ./gradlew bundleRelease
```

## 注意事項

- このアプリを実行するには、デバイスに[Health Connect (Android)](https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata)アプリがインストールされている必要があります。
- 初回実行時にHealth Connectへのデータ書き込み権限を許可する必要があります。

## 開発者向け情報

このプロジェクトは、Health Connect SDKの基本的な使用方法を示すためのサンプルです。実際のアプリ開発では、より堅牢なエラーハンドリング、UI/UXの改善、バックグラウンド処理の実装などを検討してください。 