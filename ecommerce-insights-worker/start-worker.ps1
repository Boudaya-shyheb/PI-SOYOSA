$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

if (!(Test-Path -Path "node_modules")) {
  Write-Host "Installing dependencies..."
  npm install
}

Write-Host "Starting ecommerce insights worker..."
npm run start
