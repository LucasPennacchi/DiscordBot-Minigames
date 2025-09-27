@echo off

ECHO.
ECHO Iniciando o Bot do Discord em segundo plano...
ECHO A janela do terminal fechará em instantes.
ECHO.

start "DiscordBot" javaw -jar target/DiscordBot-1.0-SNAPSHOT.jar

EXIT