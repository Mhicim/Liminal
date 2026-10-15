#!/usr/bin/env bash
set -e
echo "========================================================"
echo "  Сборка мода Liminal World Megastructure (Forge 1.20.1)"
echo "========================================================"

if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "[1/3] Скачивание официального Gradle Wrapper..."
    mkdir -p gradle/wrapper
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" -o "gradle/wrapper/gradle-wrapper.jar"
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradlew" -o "gradlew"
    chmod +x gradlew
fi

echo "[2/3] Компиляция и реобфускация мода под Minecraft 1.20.1..."
./gradlew build

cp build/libs/liminalworld-1.0.0.jar ./liminalworld-1.20.1-1.0.0.jar
echo "[3/3] ГОТОВО! Скопируйте файл liminalworld-1.20.1-1.0.0.jar в папку mods!"
