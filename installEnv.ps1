#requires -version 5.1

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# ============================================================
# CONFIGURAÇÕES
# ============================================================

$JavaWingetId = "Microsoft.OpenJDK.17"
$PostgreSQLWingetId = "PostgreSQL.PostgreSQL"

$JavaFXVersion = "21.0.2"
$JavaFXUrl = "https://download2.gluonhq.com/openjfx/$JavaFXVersion/openjfx-$JavaFXVersion" + "_windows-x64_bin-sdk.zip"

$JavaFXBasePath = "C:\JavaFX"
$JavaFXZip = Join-Path $env:TEMP "javafx-sdk-$JavaFXVersion.zip"

# ============================================================
# FUNÇÕES
# ============================================================

function Write-Section {
    param([string]$Message)

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Test-Administrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()

    $principal = New-Object Security.Principal.WindowsPrincipal($identity)

    return $principal.IsInRole(
        [Security.Principal.WindowsBuiltInRole]::Administrator
    )
}

function Install-WingetPackage {
    param(
        [Parameter(Mandatory)]
        [string]$Id,

        [Parameter(Mandatory)]
        [string]$Name
    )

    Write-Section "Verificando $Name"

    $installed = winget list --id $Id -e 2>$null | Out-String

    if ($installed -match [regex]::Escape($Id)) {

        Write-Host "$Name já está instalado." -ForegroundColor Green
        return
    }

    Write-Host "$Name não encontrado. Instalando..." -ForegroundColor Yellow

    winget install `
        --id $Id `
        -e `
        --silent `
        --accept-package-agreements `
        --accept-source-agreements

    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao instalar $Name. Código de saída: $LASTEXITCODE"
    }

    Write-Host "$Name instalado com sucesso." -ForegroundColor Green
}

function Invoke-Download {
    param(
        [Parameter(Mandatory)]
        [string]$Url,

        [Parameter(Mandatory)]
        [string]$Destination,

        [int]$Retries = 3
    )

    for ($attempt = 1; $attempt -le $Retries; $attempt++) {

        try {

            Write-Host "Download - tentativa $attempt de $Retries..."

            Invoke-WebRequest `
                -Uri $Url `
                -OutFile $Destination `
                -UseBasicParsing

            if (!(Test-Path $Destination)) {
                throw "O arquivo não foi criado."
            }

            if ((Get-Item $Destination).Length -lt 1MB) {
                throw "O arquivo baixado parece inválido."
            }

            return
        }
        catch {

            Write-Warning "Falha no download: $($_.Exception.Message)"

            if ($attempt -eq $Retries) {
                throw
            }

            Start-Sleep -Seconds 3
        }
    }
}

# ============================================================
# ADMINISTRADOR
# ============================================================

if (!(Test-Administrator)) {

    Write-Host "O script precisa de privilégios de administrador." -ForegroundColor Yellow
    Write-Host "Reabrindo automaticamente como administrador..."

    $arguments = @(
        "-NoProfile"
        "-ExecutionPolicy", "Bypass"
        "-File", "`"$PSCommandPath`""
    )

    Start-Process `
        powershell.exe `
        -Verb RunAs `
        -ArgumentList $arguments

    exit
}

# ============================================================
# WINGET
# ============================================================

Write-Section "Verificando Winget"

if (!(Get-Command winget -ErrorAction SilentlyContinue)) {

    Write-Host "Winget não foi encontrado." -ForegroundColor Red
    Write-Host ""
    Write-Host "Instale ou atualize o 'App Installer' da Microsoft Store."
    Write-Host "Depois execute este script novamente."

    Read-Host "Pressione ENTER para sair"
    exit 1
}

Write-Host "Winget encontrado." -ForegroundColor Green

# Atualiza as fontes do Winget
try {
    winget source update
}
catch {
    Write-Warning "Não foi possível atualizar as fontes do Winget."
}

# ============================================================
# JAVA 17
# ============================================================

try {

    Install-WingetPackage `
        -Id $JavaWingetId `
        -Name "Microsoft OpenJDK 17"

}
catch {

    Write-Host "Erro ao instalar Java 17:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# ============================================================
# POSTGRESQL
# ============================================================

try {

    Install-WingetPackage `
        -Id $PostgreSQLWingetId `
        -Name "PostgreSQL"

}
catch {

    Write-Warning "Não foi possível instalar usando o ID padrão."

    Write-Host ""
    Write-Host "Procurando PostgreSQL disponível no Winget..."

    winget search PostgreSQL
}

# ============================================================
# JAVAFX
# ============================================================

Write-Section "Verificando JavaFX $JavaFXVersion"

$JavaFXExpectedFolder = Join-Path `
    $JavaFXBasePath `
    "javafx-sdk-$JavaFXVersion"

$JavaFXLibFolder = Join-Path `
    $JavaFXExpectedFolder `
    "lib"

if (Test-Path $JavaFXLibFolder) {

    Write-Host "JavaFX já está instalado:" -ForegroundColor Green
    Write-Host $JavaFXExpectedFolder

}
else {

    Write-Host "JavaFX não encontrado."

    # Remove ZIP antigo/corrompido
    if (Test-Path $JavaFXZip) {
        Remove-Item $JavaFXZip -Force
    }

    # Cria C:\JavaFX
    if (!(Test-Path $JavaFXBasePath)) {

        New-Item `
            -ItemType Directory `
            -Path $JavaFXBasePath `
            -Force |
            Out-Null
    }

    try {

        Write-Host ""
        Write-Host "Baixando:"
        Write-Host $JavaFXUrl

        Invoke-Download `
            -Url $JavaFXUrl `
            -Destination $JavaFXZip

        Write-Host ""
        Write-Host "Extraindo JavaFX..."

        Expand-Archive `
            -Path $JavaFXZip `
            -DestinationPath $JavaFXBasePath `
            -Force

        if (!(Test-Path $JavaFXLibFolder)) {

            throw @"
JavaFX foi extraído, mas a pasta esperada não foi encontrada:

$JavaFXLibFolder
"@
        }

        Write-Host "JavaFX instalado com sucesso." -ForegroundColor Green
    }
    catch {

        Write-Host "Falha ao instalar JavaFX." -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red

        Read-Host "Pressione ENTER para sair"
        exit 1
    }
    finally {

        if (Test-Path $JavaFXZip) {
            Remove-Item $JavaFXZip -Force -ErrorAction SilentlyContinue
        }
    }
}

# ============================================================
# VARIÁVEL JAVAFX_HOME
# ============================================================

Write-Section "Configurando JAVAFX_HOME"

[Environment]::SetEnvironmentVariable(
    "JAVAFX_HOME",
    $JavaFXExpectedFolder,
    [EnvironmentVariableTarget]::Machine
)

Write-Host "JAVAFX_HOME configurado como:" -ForegroundColor Green
Write-Host $JavaFXExpectedFolder

# Atualiza também na sessão atual
$env:JAVAFX_HOME = $JavaFXExpectedFolder

# ============================================================
# VERIFICAÇÃO JAVA
# ============================================================

Write-Section "Verificando Java"

$javaCommand = Get-Command java -ErrorAction SilentlyContinue

if ($javaCommand) {

    java -version

}
else {

    Write-Warning @"
O Java foi instalado, mas ainda não apareceu no PATH desta sessão.

Isso normalmente é resolvido fechando e abrindo novamente o terminal.
"@
}

# ============================================================
# RESULTADO
# ============================================================

Write-Section "Instalação concluída"

Write-Host "Componentes configurados:" -ForegroundColor Green
Write-Host "  Java 17"
Write-Host "  PostgreSQL"
Write-Host "  JavaFX $JavaFXVersion"
Write-Host ""
Write-Host "JavaFX:"
Write-Host "  $JavaFXExpectedFolder"
Write-Host ""
Write-Host "JAVAFX_HOME:"
Write-Host "  $env:JAVAFX_HOME"
Write-Host ""

Read-Host "Pressione ENTER para fechar"