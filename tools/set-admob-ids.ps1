# Define os IDs do AdMob (producao) no local.properties.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File tools\set-admob-ids.ps1
#
# Onde encontrar no painel do AdMob (admob.google.com):
#   App ID     -> Apps > (seu app) > Configuracoes do app
#   Unit IDs   -> Apps > (seu app) > Blocos de anuncio (crie 3: banner, interstitial, rewarded)

$ErrorActionPreference = "Stop"
$propertyFile = Join-Path (Split-Path -Parent $PSScriptRoot) "local.properties"

if (-not (Test-Path $propertyFile)) {
    Write-Error "local.properties nao encontrado na raiz do projeto."
    exit 1
}

function Read-Id([string]$label, [string]$example) {
    while ($true) {
        $value = Read-Host "$label (ex: $example)"
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }
        Write-Host "  valor vazio - tente novamente" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Cole os 4 IDs do AdMob (Enter apos cada um):" -ForegroundColor Cyan
Write-Host ""

$appId = Read-Id "1/4 AdMob APP ID" "ca-app-pub-0000000000000000~0000000000"
$bannerId = Read-Id "2/4 BANNER unit ID" "ca-app-pub-0000000000000000/1111111111"
$interstitialId = Read-Id "3/4 INTERSTITIAL unit ID" "ca-app-pub-0000000000000000/2222222222"
$rewardedId = Read-Id "4/4 REWARDED unit ID" "ca-app-pub-0000000000000000/3333333333"

$lines = Get-Content $propertyFile
$updated = New-Object System.Collections.Generic.List[string]
$seenApp = $false
$seenBanner = $false
$seenInterstitial = $false
$seenRewarded = $false

foreach ($line in $lines) {
    if ($line -like "*ADMOB_APP_ID=*") {
        $updated.Add("ADMOB_APP_ID=$appId"); $seenApp = $true
    } elseif ($line -like "*ADMOB_BANNER_ID=*") {
        $updated.Add("ADMOB_BANNER_ID=$bannerId"); $seenBanner = $true
    } elseif ($line -like "*ADMOB_INTERSTITIAL_ID=*") {
        $updated.Add("ADMOB_INTERSTITIAL_ID=$interstitialId"); $seenInterstitial = $true
    } elseif ($line -like "*ADMOB_REWARDED_ID=*") {
        $updated.Add("ADMOB_REWARDED_ID=$rewardedId"); $seenRewarded = $true
    } else {
        $updated.Add($line)
    }
}

if (-not $seenApp) { $updated.Add("ADMOB_APP_ID=$appId") }
if (-not $seenBanner) { $updated.Add("ADMOB_BANNER_ID=$bannerId") }
if (-not $seenInterstitial) { $updated.Add("ADMOB_INTERSTITIAL_ID=$interstitialId") }
if (-not $seenRewarded) { $updated.Add("ADMOB_REWARDED_ID=$rewardedId") }

# UTF-8 sem BOM (o Gradle/Java Properties le perfeitamente).
[System.IO.File]::WriteAllLines($propertyFile, $updated.ToArray())

Write-Host ""
Write-Host "[OK] IDs do AdMob salvos no local.properties (arquivo nao vai para o Git)." -ForegroundColor Green
Write-Host "Proximo passo: gerar o AAB de release para publicar a versao com anuncios reais." -ForegroundColor Green
Write-Host ""
Write-Host "[ATENCAO] Nunca clique nos seus proprios anuncios reais (risco de suspensao da conta AdMob)." -ForegroundColor Red
