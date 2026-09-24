@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM CondoManager - Gerenciador do Banco de Dados Docker
REM ============================================================

set ACTION=%1
if "%ACTION%"=="" goto help

if /i "%ACTION%"=="start" goto start
if /i "%ACTION%"=="stop" goto stop
if /i "%ACTION%"=="restart" goto restart
if /i "%ACTION%"=="status" goto status
if /i "%ACTION%"=="logs" goto logs
if /i "%ACTION%"=="reset" goto reset
if /i "%ACTION%"=="backup" goto backup
if /i "%ACTION%"=="restore" goto restore
if /i "%ACTION%"=="cli" goto cli

:help
echo ============================================================
echo   CondoManager - Comandos de Gerenciamento do Banco (Docker)
echo ============================================================
echo   db.bat start              Inicia o container do banco MySQL
echo   db.bat stop               Para o container do banco
echo   db.bat restart            Reinicia o container do banco
echo   db.bat status             Verifica se o banco esta rodando
echo   db.bat logs               Exibe os logs do banco em tempo real
echo   db.bat reset              Reseta o banco e reaplica schema e dados de teste
echo   db.bat backup [arquivo]   Exporta um dump SQL (padrao: backup.sql)
echo   db.bat restore [arquivo]  Restaura um dump SQL para o banco
echo   db.bat cli                Abre o terminal interativo do MySQL
echo ============================================================
goto end

:start
echo [CondoManager] Iniciando banco de dados no Docker...
docker compose up -d db
goto end

:stop
echo [CondoManager] Parando banco de dados...
docker compose stop db
goto end

:restart
echo [CondoManager] Reiniciando banco de dados...
docker compose restart db
goto end

:status
docker compose ps db
goto end

:logs
docker compose logs -f db
goto end

:reset
echo ============================================================
echo AVISO: Isso ira apagar todo o volume do banco e recria-lo
echo do zero com schema.sql e dados_teste.sql!
echo ============================================================
docker compose down -v
docker compose up -d db
echo [CondoManager] Banco recriado com sucesso!
goto end

:backup
set BACKUP_FILE=%2
if "%BACKUP_FILE%"=="" set BACKUP_FILE=backup_%date:~6,4%-%date:~3,2%-%date:~0,2%.sql
echo [CondoManager] Exportando dados para %BACKUP_FILE%...
docker compose exec -T db mysqldump -u root -proot condominio_db > "%BACKUP_FILE%"
echo [CondoManager] Backup concluido: %BACKUP_FILE%
goto end

:restore
set RESTORE_FILE=%2
if "%RESTORE_FILE%"=="" (
    echo [ERRO] Informe o arquivo SQL a restaurar: db.bat restore arquivo.sql
    goto end
)
if not exist "%RESTORE_FILE%" (
    echo [ERRO] Arquivo '%RESTORE_FILE%' nao encontrado.
    goto end
)
echo [CondoManager] Restaurando %RESTORE_FILE% no banco...
docker compose exec -T db mysql -u root -proot condominio_db < "%RESTORE_FILE%"
echo [CondoManager] Restauracao concluida!
goto end

:cli
echo [CondoManager] Abrindo terminal MySQL no container...
docker compose exec -it db mysql -u root -proot condominio_db
goto end

:end
endlocal
