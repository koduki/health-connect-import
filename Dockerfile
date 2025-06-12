# ベースイメージとしてJavaがプリインストールされたものを選択すると効率的
# (例: eclipse-temurin, openjdkなど)
# ここでは汎用的なUbuntuをベースにします
FROM ubuntu:22.04

# 環境変数でバージョンを管理するとメンテナンス性が向上します
ARG JDK_VERSION=17
ARG ANDROID_CMD_TOOLS_VERSION=11076708
ARG ANDROID_BUILD_TOOLS_VERSION="34.0.0"
ARG ANDROID_PLATFORM_VERSION="34"

# タイムゾーン設定や対話形式のプロンプトを抑制
ENV TZ=Asia/Tokyo
ENV DEBIAN_FRONTEND=noninteractive

# 1. 必要なパッケージとJDKをインストール
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-${JDK_VERSION}-jdk \
    wget \
    unzip \
    git \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 2. Android SDKをインストール
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# コマンドラインツールのダウンロードと展開
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMD_TOOLS_VERSION}_latest.zip -O /tmp/tools.zip && \
    unzip /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/tools.zip

# 3. SDK Managerで必要なコンポーネントをインストール & ライセンスに同意
#    - yes コマンドでライセンス同意のプロンプトを自動化
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" \
               "platforms;android-${ANDROID_PLATFORM_VERSION}" \
               "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"

# 4. デフォルトのワーキングディレクトリを設定
WORKDIR /app

# (オプション) Gradleのラッパーを実行可能にする
# このDockerfile自体はソースコードを含まないので、
# コンテナ実行時にマウントされたgradlewに実行権限を付与するステップ
# CMD ["sh", "-c", "chmod +x ./gradlew && ./gradlew"]