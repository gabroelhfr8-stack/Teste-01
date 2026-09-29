$ErrorActionPreference = 'Stop'
$assetRoot = (Resolve-Path (Join-Path $PSScriptRoot '../src/main/resources/assets/selarium')).Path
$resourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '../src/main/resources')).Path
$javaRoot = (Resolve-Path (Join-Path $PSScriptRoot '../src/main/java/com/seleris/selarium')).Path
$errors = [System.Collections.Generic.List[string]]::new()

function Scan-Json($node, [string]$origin) {
    if ($node -is [System.Collections.IDictionary]) {
        foreach ($key in $node.Keys) {
            $value = $node[$key]
            if (($key -eq 'parent' -or $key -eq 'model') -and $value -is [string] -and $value.StartsWith('selarium:')) {
                $target = Join-Path $assetRoot ('models/' + $value.Substring(9) + '.json')
                if (-not (Test-Path -LiteralPath $target)) { $script:errors.Add("$origin -> model $value") }
            }
            if ($key -eq 'textures' -and $value -is [System.Collections.IDictionary]) {
                foreach ($texture in $value.Values) {
                    if ($texture -is [string] -and $texture.StartsWith('selarium:')) {
                        $target = Join-Path $assetRoot ('textures/' + $texture.Substring(9) + '.png')
                        if (-not (Test-Path -LiteralPath $target)) { $script:errors.Add("$origin -> texture $texture") }
                    }
                }
            }
            Scan-Json $value $origin
        }
    } elseif ($node -is [System.Collections.IEnumerable] -and $node -isnot [string]) {
        foreach ($value in $node) { Scan-Json $value $origin }
    }
}

Get-ChildItem -LiteralPath $assetRoot -Filter '*.json' -File -Recurse | ForEach-Object {
    try {
        $json = Get-Content -LiteralPath $_.FullName -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
        Scan-Json $json $_.FullName
    } catch { $errors.Add("$($_.FullName) -> invalid JSON: $_") }
}

$items = Get-Content -LiteralPath (Join-Path $javaRoot 'registry/SelariumItems.java') -Raw
$itemNames = [regex]::Matches($items, '(?:ITEMS\.register|registerDust|registerLegacyDust)\("([a-z_]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
foreach ($name in $itemNames) {
    if (-not (Test-Path -LiteralPath (Join-Path $assetRoot "models/item/$name.json"))) {
        $errors.Add("Missing item model: $name")
    }
}

$blocks = Get-Content -LiteralPath (Join-Path $javaRoot 'registry/SelariumBlocks.java') -Raw
$blockNames = [regex]::Matches($blocks, 'BLOCKS\.register\("([a-z_]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
foreach ($name in $blockNames) {
    if (-not (Test-Path -LiteralPath (Join-Path $assetRoot "blockstates/$name.json"))) {
        $errors.Add("Missing blockstate: $name")
    }
}

$layers = Get-Content -LiteralPath (Join-Path $javaRoot 'client/sigil/ArcaneSigilVisualLayer.java') -Raw
$layerNames = [regex]::Matches($layers, '"(arcane_sigil_[a-z_]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
foreach ($name in $layerNames) {
    if (-not (Test-Path -LiteralPath (Join-Path $assetRoot "textures/block/$name.png"))) {
        $errors.Add("Missing dynamic sigil texture: $name")
    }
}

$wardNames = [regex]::Matches((Get-Content -LiteralPath (Join-Path $javaRoot 'ward/WardType.java') -Raw),
    '([A-Z_]+)\("([a-z_]+)"\)') | Where-Object { $_.Groups[2].Value -ne 'none' }
foreach ($ward in $wardNames) {
    $name = $ward.Groups[2].Value
    if (-not (Test-Path -LiteralPath (Join-Path $assetRoot "textures/block/arcane_sigil_$name.png"))) {
        $errors.Add("Missing ward signature: $name")
    }
}

$pt = Get-Content -LiteralPath (Join-Path $assetRoot 'lang/pt_br.json') -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
$en = Get-Content -LiteralPath (Join-Path $assetRoot 'lang/en_us.json') -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
foreach ($key in $pt.Keys) { if (-not $en.ContainsKey($key)) { $errors.Add("Missing en_us key: $key") } }
foreach ($key in $en.Keys) { if (-not $pt.ContainsKey($key)) { $errors.Add("Missing pt_br key: $key") } }
if (-not (Test-Path -LiteralPath (Join-Path $resourceRoot 'data/selarium/loot_tables/blocks/arcane_crystal_cluster.json'))) {
    $errors.Add('Arcane Crystal Cluster loot table missing')
}

Write-Output "itemModels=$($itemNames.Count) blockstates=$($blockNames.Count) sigilTextures=$($layerNames.Count) wards=$($wardNames.Count) translations=$($pt.Count)"
if ($errors.Count -gt 0) {
    $errors | Sort-Object -Unique | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Output 'All Selarium model, texture and localization references resolved.'
