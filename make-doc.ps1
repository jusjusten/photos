<#
make-doc.ps1 - simplified
Generates Javadoc HTML into the `docs/` directory for this project.
Usage: .\make-doc.ps1
#>

param()

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot
$docsDir = Join-Path $projectRoot 'docs'
$javafxLib = Join-Path $projectRoot 'lib\javafx-sdk-21.0.9\lib'

Write-Host 'make-doc.ps1: starting' -ForegroundColor Cyan

# Check javadoc availability
if (-not (Get-Command javadoc -ErrorAction SilentlyContinue)) {
    Write-Host 'ERROR: javadoc tool not found on PATH. Run this from a JDK installation.' -ForegroundColor Red
    exit 1
}

# Remove old docs if present
if (Test-Path $docsDir) { Remove-Item -Recurse -Force $docsDir }

# Helper: run javadoc with a specified argument array and return exit code
function Invoke-Javadoc([string[]]$params) {
    Write-Host 'Running: javadoc ' + ($params -join ' ')
    & javadoc @params
    return $LASTEXITCODE
}

# Common arguments
$common = @('-d', $docsDir, '-sourcepath', '.', '-author', '-version')

# Try full project using JavaFX module path if available
if (Test-Path $javafxLib) {
    $fullArgs = @('--module-path', $javafxLib, '--add-modules', 'javafx.controls,javafx.fxml') + $common + @('-subpackages', 'photos')
    $code = Invoke-Javadoc -params $fullArgs
    if ($code -eq 0) {
        Write-Host 'Javadoc generated (full project).' -ForegroundColor Green
        if (Test-Path (Join-Path $docsDir 'index.html')) { Start-Process (Join-Path $docsDir 'index.html') }
        exit 0
    } else {
        Write-Host 'Full javadoc run failed, falling back to model-only.' -ForegroundColor Yellow
    }
} else {
    Write-Host 'JavaFX lib not found, will generate model-only docs.' -ForegroundColor Yellow
}

# Fallback: generate model-only docs
$modelArgs = $common + @('-subpackages', 'photos.model')
$code = Invoke-Javadoc -params $modelArgs
if ($code -ne 0) {
    Write-Host 'Javadoc generation failed.' -ForegroundColor Red
    exit $code
}

Write-Host 'Javadoc generated (model-only).' -ForegroundColor Green
if (Test-Path (Join-Path $docsDir 'index.html')) { Start-Process (Join-Path $docsDir 'index.html') }
exit 0
