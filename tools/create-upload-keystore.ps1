# Gera a keystore de upload do 2048 Classic (assinatura de release para a Play Store).
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File tools\create-upload-keystore.ps1
#
# O keytool vai pedir interativamente:
#   1) uma senha para a keystore (guarde!);
#   2) dados do certificado (nome, organização, cidade, estado, país);
#   3) a senha da chave (pode ser a mesma da keystore).
#
# Depois preencha RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS e
# RELEASE_KEY_PASSWORD no local.properties (NUNCA versione esse arquivo).

param(
    [string]$Alias = "hcn2048",
    [string]$OutputPath = "$env:USERPROFILE\.android-keys\hcn2048-upload.jks"
)

$ErrorActionPreference = "Stop"

$keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
if (-not $keytool -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    if (Test-Path $candidate) { $keytool = $candidate }
}
if (-not $keytool -or -not (Test-Path $keytool)) {
    Write-Error "keytool não encontrado. Instale um JDK ou rode com o JDK do Android Studio e defina JAVA_HOME."
    exit 1
}

$dir = Split-Path $OutputPath -Parent
if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
}

if (Test-Path $OutputPath) {
    Write-Error "Já existe uma keystore em $OutputPath. Remova ou escolha outro caminho com -OutputPath."
    exit 1
}

Write-Host ""
Write-Host "Keystore será criada em: $OutputPath" -ForegroundColor Cyan
Write-Host "Validade: 10.000 dias | Algoritmo: RSA 2048 | Alias: $Alias" -ForegroundColor Cyan
Write-Host ""

& $keytool -genkeypair -v -keystore $OutputPath -alias $Alias -keyalg RSA -keysize 2048 -validity 10000

if ($LASTEXITCODE -ne 0) {
    Write-Error "O keytool falhou. Nenhum arquivo foi configurado."
    exit $LASTEXITCODE
}

$pathForProperties = $OutputPath -replace '\\', '/'

Write-Host ""
Write-Host "✔ Keystore criada com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Adicione ao seu local.properties:" -ForegroundColor Green
Write-Host "RELEASE_STORE_FILE=$pathForProperties"
Write-Host "RELEASE_STORE_PASSWORD=<a senha da keystore que você digitou>"
Write-Host "RELEASE_KEY_ALIAS=$Alias"
Write-Host "RELEASE_KEY_PASSWORD=<a senha da chave que você digitou>"
Write-Host ""
Write-Host "⚠ Faça backup da keystore e das senhas em local seguro (gerenciador de senhas/cofre)." -ForegroundColor Red
Write-Host "⚠ Se perder esta chave, os usuários atuais não poderão receber atualizações pelo mesmo app." -ForegroundColor Red
