# Build and run the Photos JavaFX application
# Usage: .\build.ps1 [all | compile | run | clean]
# Default: compiles, copies resources, and runs

param(
    [string]$Action = "all"
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outDir = Join-Path $projectRoot "out"
$javafxPath = "lib/javafx-sdk-21.0.9/lib"
$modulePath = Join-Path $projectRoot $javafxPath

Write-Host "=== Photos Application Build ===" -ForegroundColor Cyan

# Clean output directory
function Clean {
    Write-Host "Cleaning output directory..." -ForegroundColor Yellow
    if (Test-Path $outDir) {
        Remove-Item -Recurse -Force $outDir
    }
    mkdir -Force $outDir | Out-Null
    Write-Host "[OK] Cleaned" -ForegroundColor Green
}

# Compile sources
function Compile {
    Write-Host "Compiling Java sources..." -ForegroundColor Yellow
    
    # Get all Java files and convert to space-separated list for javac
    $javaFiles = @(Get-ChildItem -Path $projectRoot -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })

    if ($javaFiles.Count -eq 0) {
        Write-Host "[FAIL] No Java files found" -ForegroundColor Red
        exit 1
    }

    Write-Host "Found $($javaFiles.Count) Java files to compile..." -ForegroundColor Gray
    
    # Compile all Java files at once
    & javac -d $outDir --module-path $modulePath --add-modules javafx.controls,javafx.fxml @javaFiles 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[FAIL] Compilation failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Compiled successfully ($($javaFiles.Count) files)" -ForegroundColor Green
}

# Copy resources (FXML, data)
function CopyResources {
    Write-Host "Copying resources..." -ForegroundColor Yellow

    # Copy FXML files to a temp location first, then to the final location
    # to avoid overwriting compiled .class files in out/photos/view
    $viewOutDir = Join-Path $outDir "photos\view"
    
    # Copy FXML files only (not overwriting the entire directory)
    Get-ChildItem -Path "$projectRoot\photos\view\*.fxml" -ErrorAction SilentlyContinue | ForEach-Object {
        Copy-Item -Path $_.FullName -Destination $viewOutDir -Force
    }

    # Copy data directory (stock photos, etc.) if present
    $dataSrc = Join-Path $projectRoot "data"
    if (Test-Path $dataSrc) {
        $dataOutDir = Join-Path $outDir "data"
        Remove-Item -Recurse -Force $dataOutDir -ErrorAction SilentlyContinue
        Copy-Item -Path $dataSrc -Destination $dataOutDir -Recurse -Force
    }

    Write-Host "[OK] Resources copied" -ForegroundColor Green
}

# Run the application
function Run {
    Write-Host "Starting application..." -ForegroundColor Yellow
    java --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $outDir photos.Photos
}

# Main
try {
    Set-Location $projectRoot

    switch ($Action.ToLower()) {
        "clean" {
            Clean
        }
        "compile" {
            Clean
            Compile
        }
        "run" {
            Run
        }
        "all" {
            Clean
            Compile
            CopyResources
            Run
        }
        default {
            Write-Host "[FAIL] Unknown action: $Action" -ForegroundColor Red
            Write-Host "Usage: .\build.ps1 [all | compile | run | clean]" -ForegroundColor Yellow
            exit 1
        }
    }
}
catch {
    Write-Host "[ERROR] $_" -ForegroundColor Red
    exit 1
}
