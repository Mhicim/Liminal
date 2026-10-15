@echo off
chcp 65001 >nul
echo ========================================================
echo   Сборка мода Liminal World Megastructure (Forge 1.20.1)
echo ========================================================
echo.

if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [1/3] Скачивание официального Gradle Wrapper...
    if not exist "gradle\wrapper" mkdir "gradle\wrapper"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradlew.bat' -OutFile 'gradlew.bat'"
)

echo [2/3] Компиляция и реобфускация мода под Minecraft 1.20.1...
call gradlew.bat build

if exist "build\libs\liminalworld-1.0.0.jar" (
    echo.
    echo [3/3] ГОТОВО! Файл мода успешно собран:
    echo       build\libs\liminalworld-1.0.0.jar
    copy /Y "build\libs\liminalworld-1.0.0.jar" "liminalworld-1.20.1-1.0.0.jar" >nul
    echo       Копия также сохранена в корне папки: liminalworld-1.20.1-1.0.0.jar
    echo       Теперь просто перетащите liminalworld-1.20.1-1.0.0.jar в папку .minecraft\mods!
) else (
    echo.
    echo [ОШИБКА] Сборка не удалась. Убедитесь, что установлена Java 17 (JDK 17) и есть подключение к интернету.
)
pause
