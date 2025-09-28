@echo off
ECHO Iniciando o Bot do Discord em segundo plano...
start "DiscordBot" javaw -jar target/DiscordBot-1.0-SNAPSHOT.jar
EXIT