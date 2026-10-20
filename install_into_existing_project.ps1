# ByteQuest: copy the complete Android main source+resources into an Android Studio project.
param([Parameter(Mandatory=$true)][string]$AndroidProject)
$ErrorActionPreference = "Stop"
$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $base "app\src\main"
$dest = Join-Path $AndroidProject "app\src\main"
if (-not (Test-Path (Join-Path $AndroidProject "settings.gradle.kts"))) { throw "Pass the Android Studio project root (directory with settings.gradle.kts)." }
New-Item -ItemType Directory -Path $dest -Force | Out-Null
Copy-Item -Path (Join-Path $source '*') -Destination $dest -Recurse -Force
Write-Host "Copied complete ByteQuest main source, manifest and resources. Sync and Run in Android Studio."
