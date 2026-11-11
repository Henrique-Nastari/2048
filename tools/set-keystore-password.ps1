# Define a senha da keystore no local.properties (pede a senha de forma oculta).
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File tools\set-keystore-password.ps1
#
# Antes de rodar, feche o local.properties se ele estiver aberto em algum editor
# (para nao sobrescrever esta alteracao com uma versao antiga).

$ErrorActionPreference = "Stop"
$propertyFile = Join-Path (Split-Path -Parent $PSScriptRoot) "local.properties"

if (-not (Test-Path $propertyFile)) {
    Write-Error "local.properties nao encontrado na raiz do projeto."
    exit 1
}

$secure = Read-Host -AsSecureString "Digite a senha da keystore (nao aparece na tela)"
$plain = [System.Net.NetworkCredential]::new("", $secure).Password
if ([string]::IsNullOrEmpty($plain)) {
    Write-Error "Senha vazia. Nada foi alterado."
    exit 1
}

$lines = Get-Content $propertyFile
$updated = New-Object System.Collections.Generic.List[string]
$storeCount = 0
$keyCount = 0
foreach ($line in $lines) {
    if ($line -like "RELEASE_STORE_PASSWORD=*") {
        $updated.Add("RELEASE_STORE_PASSWORD=$plain")
        $storeCount++
    } elseif ($line -like "RELEASE_KEY_PASSWORD=*") {
        $updated.Add("RELEASE_KEY_PASSWORD=$plain")
        $keyCount++
    } else {
        $updated.Add($line)
    }
}

if ($storeCount -eq 0 -and $keyCount -eq 0) {
    Write-Error "Linhas RELEASE_STORE_PASSWORD/RELEASE_KEY_PASSWORD nao encontradas no local.properties."
    exit 1
}

# UTF-8 sem BOM (o Gradle/Java Properties le perfeitamente).
[System.IO.File]::WriteAllLines($propertyFile, $updated.ToArray())
$plain = $null

Write-Host ""
Write-Host "[OK] Senha aplicada em RELEASE_STORE_PASSWORD e RELEASE_KEY_PASSWORD do local.properties." -ForegroundColor Green
Write-Host "Arquivo salvo em disco. Proximo passo: gerar o AAB assinado (bundleRelease)." -ForegroundColor Green
