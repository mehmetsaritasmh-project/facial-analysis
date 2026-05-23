@echo off
echo ===================================================
echo   Yuz Analizi Projesi (Java Backend + Python AI)
echo ===================================================

:: Android Studio'nun gömülü Java (JDK) yolunu sisteme tanımlıyoruz
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: 1. PYTHON SERVISINI MUTLAK YOL ILE BASLAT
echo [1/2] Python FastAPI servisi baslatiliyor...
start "Python FastAPI Service" cmd /k "cd /d C:\Users\user\Desktop\yuzanalizi\python_service && C:\Users\user\mediapipe_env\Scripts\activate && python -m uvicorn app:app --port 8000"

:: Servislerin çakışmaması için 3 saniye bekle
timeout /t 3 /nobreak > NUL

:: 2. JAVA SPRING BOOT'U OPENCV DLL YOLUYLA BERABER BASLAT
echo [2/2] Java Spring Boot backend baslatiliyor...
cd /d C:\Users\user\Desktop\yuzanalizi\yuzanalizapi

:: Java'ya projenin içindeki 'libs' klasöründe bulunan opencv_java4120.dll dosyasını okutuyoruz
start "Spring Boot Service" cmd /k "cd /d C:\Users\user\Desktop\yuzanalizi\yuzanalizapi && mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.library.path=libs""

echo ===================================================
echo  Python ve Java backend servisleri baslatildi!
echo ===================================================
pause