$gradleFile = "gradle.properties"
if (-Not (Test-Path $gradleFile)) {
    Write-Error "No se encontro gradle.properties"
    exit 1
}

$matchingLines = @(Get-Content $gradleFile | Where-Object { $_ -match '^(mod_)?version=' })
if ($matchingLines.Count -eq 0) {
    Write-Error "No se encontro version en gradle.properties"
    exit 1
}

$versionLine = $matchingLines[0]
$version = ($versionLine -split '=')[1].Trim()

try {
    $gitHash = git rev-parse --short HEAD
} catch {
    Write-Error "No se pudo obtener el hash de git."
    exit 1
}

$safeVersion = -join ($version.ToCharArray() | Where-Object { $_ -match '[a-zA-Z0-9._-]' })
$tag = "v${safeVersion}-dev-${gitHash}"

# Comprobar si el tag ya existe
$existingTags = git tag --list $tag
if ($existingTags) {
    Write-Warning "El tag '$tag' ya existe localmente."
} else {
    git tag -a $tag -m "Dev build $tag"
    Write-Host "Tag local creado: $tag"
}

git push origin $tag
Write-Host "Tag subido a origin: $tag"
