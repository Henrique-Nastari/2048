# Gera todos os ícones do 2048 Classic:
#  - camadas do adaptive icon (background, foreground, monochrome) em drawable-nodpi;
#  - PNGs legados (API 24-25) em todas as densidades;
#  - previews do resultado em build/ (pasta ignorada pelo Git).
#
# Uso: powershell -ExecutionPolicy Bypass -File tools\generate-launcher-icons.ps1

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$res = Join-Path $root "androidApp\src\main\res"
$previewDir = Join-Path $root "build"

# ── Paleta (cores do jogo) ────────────────────────────────────────────────
$gradientFrom = [System.Drawing.Color]::FromArgb(255, 246, 94, 59)    # vermelho-laranja (64)
$gradientTo   = [System.Drawing.Color]::FromArgb(255, 237, 194, 46)   # dourado (2048)
$textColor    = [System.Drawing.Color]::White
$shadowColor  = [System.Drawing.Color]::FromArgb(95, 70, 30, 8)       # sombra quente translúcida

$base = 432  # cobre xxxhdpi (108dp * 4)

# ── Helpers ───────────────────────────────────────────────────────────────
function Get-BestFamily {
    $names = [System.Drawing.FontFamily]::Families | ForEach-Object { $_.Name }
    foreach ($candidate in @("Segoe UI Black", "Arial Black", "Segoe UI", "Arial")) {
        if ($names -contains $candidate) { return $candidate }
    }
    return "Arial"
}

function New-Graphics([System.Drawing.Bitmap]$bitmap) {
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAlias
    return $g
}

function Add-Gradient([System.Drawing.Graphics]$g, [int]$size) {
    $rect = [System.Drawing.Rectangle]::new(0, 0, $size, $size)
    $brush = [System.Drawing.Drawing2D.LinearGradientBrush]::new($rect, $gradientFrom, $gradientTo, [single]38)
    $g.FillRectangle($brush, $rect)
    $brush.Dispose()
}

function Get-FittedFont([System.Drawing.Graphics]$g, [string]$text, [string]$family, [int]$maxWidth, [int]$maxHeight) {
    $fontSize = 420
    while ($fontSize -gt 12) {
        $font = [System.Drawing.Font]::new($family, $fontSize, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
        $measure = $g.MeasureString($text, $font)
        if ($measure.Width -le $maxWidth -and $measure.Height -le $maxHeight) { return $font }
        $font.Dispose()
        $fontSize -= 6
    }
    return [System.Drawing.Font]::new($family, 12, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
}

function Draw-CenteredText(
    [System.Drawing.Graphics]$g,
    [int]$size,
    [string]$text,
    [System.Drawing.Font]$font,
    [int]$shadowOffset,
    [bool]$withShadow
) {
    $measure = $g.MeasureString($text, $font)
    $x = ($size - $measure.Width) / 2
    # Optical centering: block digits have no descenders, so nudge slightly down.
    $y = ($size - $measure.Height) / 2 + ($size * 0.012)

    if ($withShadow -and $shadowOffset -gt 0) {
        $shadowBrush = [System.Drawing.SolidBrush]::new($shadowColor)
        $g.DrawString($text, $font, $shadowBrush, [single]($x + $shadowOffset), [single]($y + $shadowOffset))
        $shadowBrush.Dispose()
    }

    $brush = [System.Drawing.SolidBrush]::new($textColor)
    $g.DrawString($text, $font, $brush, [single]$x, [single]$y)
    $brush.Dispose()
}

function Get-RoundedPath([int]$size, [single]$radius) {
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = $radius * 2
    $path.AddArc(0, 0, $d, $d, 180, 90)
    $path.AddArc($size - $d, 0, $d, $d, 270, 90)
    $path.AddArc($size - $d, $size - $d, $d, $d, 0, 90)
    $path.AddArc(0, $size - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    return $path
}

function Save-Resized([System.Drawing.Bitmap]$bitmap, [int]$size, [string]$path) {
    $small = [System.Drawing.Bitmap]::new($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = New-Graphics $small
    $g.DrawImage($bitmap, 0, 0, $size, $size)
    $g.Dispose()
    $small.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $small.Dispose()
}

# ── Camadas do adaptive icon ──────────────────────────────────────────────
$family = Get-BestFamily
New-Item -ItemType Directory -Force -Path (Join-Path $res "drawable-nodpi") | Out-Null
New-Item -ItemType Directory -Force -Path $previewDir | Out-Null

# Background (full bleed gradient — the launcher applies the mask)
$bg = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$bgGraphics = New-Graphics $bg
$bgGraphics.Clear([System.Drawing.Color]::Transparent)
Add-Gradient $bgGraphics $base
$bg.Save((Join-Path $res "drawable-nodpi\ic_launcher_background.png"), [System.Drawing.Imaging.ImageFormat]::Png)

# Foreground (text keeps inside the 66dp safe zone)
$fg = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$fgGraphics = New-Graphics $fg
$fgGraphics.Clear([System.Drawing.Color]::Transparent)
$fgFont = Get-FittedFont $fgGraphics "2048" $family ([int]($base * 0.60)) ([int]($base * 0.42))
Draw-CenteredText $fgGraphics $base "2048" $fgFont 7 $true
$fg.Save((Join-Path $res "drawable-nodpi\ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$fgFont.Dispose()

# Monochrome (Android 13+ themed icons: only alpha matters)
$mono = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$monoGraphics = New-Graphics $mono
$monoGraphics.Clear([System.Drawing.Color]::Transparent)
$monoFont = Get-FittedFont $monoGraphics "2048" $family ([int]($base * 0.56)) ([int]($base * 0.38))
Draw-CenteredText $monoGraphics $base "2048" $monoFont 0 $false
$mono.Save((Join-Path $res "drawable-nodpi\ic_launcher_monochrome.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$monoFont.Dispose()

# Preview of the composed adaptive icon (build/ is git-ignored)
$preview = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$previewGraphics = New-Graphics $preview
$previewGraphics.Clear([System.Drawing.Color]::Transparent)
$previewGraphics.DrawImage($bg, 0, 0)
$previewGraphics.DrawImage($fg, 0, 0)
$previewGraphics.Dispose()
$preview.Save((Join-Path $previewDir "icon-preview-adaptive.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$preview.Dispose()

# Masked preview: simulates how a launcher crops the adaptive icon (circular mask)
$masked = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$maskedGraphics = New-Graphics $masked
$maskedGraphics.Clear([System.Drawing.Color]::FromArgb(255, 236, 236, 236))
$maskSize = [int]($base * 0.67)
$maskOffset = [int](($base - $maskSize) / 2)
$maskClip = [System.Drawing.Drawing2D.GraphicsPath]::new()
$maskClip.AddEllipse($maskOffset, $maskOffset, $maskSize, $maskSize)
$maskedGraphics.SetClip($maskClip)
$maskedGraphics.DrawImage($bg, 0, 0)
$maskedGraphics.DrawImage($fg, 0, 0)
$maskedGraphics.ResetClip()
$maskedGraphics.Dispose()
$masked.Save((Join-Path $previewDir "icon-preview-masked.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$masked.Dispose()
$bgGraphics.Dispose(); $bg.Dispose()
$fgGraphics.Dispose(); $fg.Dispose()
$monoGraphics.Dispose(); $mono.Dispose()

# ── Legacy icons (API 24-25): composed, rounded / circular ────────────────
$legacy = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$legacyGraphics = New-Graphics $legacy
$legacyGraphics.Clear([System.Drawing.Color]::Transparent)
$rounded = Get-RoundedPath $base ([single]($base * 0.22))
$legacyGraphics.SetClip($rounded)
Add-Gradient $legacyGraphics $base
$legacyFont = Get-FittedFont $legacyGraphics "2048" $family ([int]($base * 0.64)) ([int]($base * 0.46))
Draw-CenteredText $legacyGraphics $base "2048" $legacyFont 9 $true
$legacyGraphics.ResetClip()
$legacyFont.Dispose()

$round = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$roundGraphics = New-Graphics $round
$roundGraphics.Clear([System.Drawing.Color]::Transparent)
$circle = [System.Drawing.Drawing2D.GraphicsPath]::new()
$circle.AddEllipse(0, 0, $base, $base)
$roundGraphics.SetClip($circle)
Add-Gradient $roundGraphics $base
$roundFont = Get-FittedFont $roundGraphics "2048" $family ([int]($base * 0.60)) ([int]($base * 0.42))
Draw-CenteredText $roundGraphics $base "2048" $roundFont 9 $true
$roundGraphics.ResetClip()
$roundFont.Dispose()

$densities = @{
    "mdpi"    = 48
    "hdpi"    = 72
    "xhdpi"   = 96
    "xxhdpi"  = 144
    "xxxhdpi" = 192
}

foreach ($density in $densities.Keys) {
    $dir = Join-Path $res "mipmap-$density"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $px = $densities[$density]
    Save-Resized $legacy $px (Join-Path $dir "ic_launcher.png")
    Save-Resized $round $px (Join-Path $dir "ic_launcher_round.png")
}

# Extra previews to validate the legacy artwork at real size
Save-Resized $legacy 192 (Join-Path $previewDir "icon-preview-legacy-192.png")
Save-Resized $legacy 48 (Join-Path $previewDir "icon-preview-legacy-48.png")

$legacyGraphics.Dispose(); $legacy.Dispose()
$roundGraphics.Dispose(); $round.Dispose()

# ── Play Store assets (versioned, ready to upload) ────────────────────────
$storeDir = Join-Path $root "store-assets"
New-Item -ItemType Directory -Force -Path $storeDir | Out-Null

# Store icon: 512x512, full bleed (the store applies its own mask)
$storeIcon = [System.Drawing.Bitmap]::new($base, $base, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$storeIconGraphics = New-Graphics $storeIcon
$storeIconGraphics.Clear([System.Drawing.Color]::Transparent)
Add-Gradient $storeIconGraphics $base
$storeIconFont = Get-FittedFont $storeIconGraphics "2048" $family ([int]($base * 0.62)) ([int]($base * 0.44))
Draw-CenteredText $storeIconGraphics $base "2048" $storeIconFont 9 $true
$storeIconGraphics.Dispose()
$storeIconFont.Dispose()
Save-Resized $storeIcon 512 (Join-Path $storeDir "play-store-icon-512.png")
$storeIcon.Dispose()

# Feature graphic: 1024x500
$featureWidth = 1024
$featureHeight = 500
$feature = [System.Drawing.Bitmap]::new($featureWidth, $featureHeight, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$featureGraphics = New-Graphics $feature
$featureRect = [System.Drawing.Rectangle]::new(0, 0, $featureWidth, $featureHeight)
$featureBrush = [System.Drawing.Drawing2D.LinearGradientBrush]::new($featureRect, $gradientFrom, $gradientTo, [single]18)
$featureGraphics.FillRectangle($featureBrush, $featureRect)
$featureBrush.Dispose()
$featureFont = Get-FittedFont $featureGraphics "2048" $family ([int]($featureWidth * 0.42)) ([int]($featureHeight * 0.62))
$featureMeasure = $featureGraphics.MeasureString("2048", $featureFont)
$featureX = ($featureWidth - $featureMeasure.Width) / 2
$featureY = ($featureHeight - $featureMeasure.Height) / 2
$featureShadow = [System.Drawing.SolidBrush]::new($shadowColor)
$featureGraphics.DrawString("2048", $featureFont, $featureShadow, [single]($featureX + 10), [single]($featureY + 10))
$featureBrushText = [System.Drawing.SolidBrush]::new($textColor)
$featureGraphics.DrawString("2048", $featureFont, $featureBrushText, [single]$featureX, [single]$featureY)
$featureShadow.Dispose(); $featureBrushText.Dispose(); $featureFont.Dispose()
$featureGraphics.Dispose()
$feature.Save((Join-Path $storeDir "play-store-feature-1024x500.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$feature.Dispose()

Write-Host ""
Write-Host "Icones gerados com a fonte: $family" -ForegroundColor Green
Write-Host "Preview adaptive: build\icon-preview-adaptive.png"
Write-Host "Preview mascarado: build\icon-preview-masked.png"
Write-Host "Preview legacy 192: build\icon-preview-legacy-192.png"
Write-Host "Preview legacy 48: build\icon-preview-legacy-48.png"
Write-Host "Assets da Play Store: store-assets\play-store-icon-512.png e play-store-feature-1024x500.png"
