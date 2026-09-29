$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$assets = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium'
$models = Join-Path $assets 'models/block'
$items = Join-Path $assets 'models/item'
$textures = Join-Path $assets 'textures/block'
$png = [System.Drawing.Imaging.ImageFormat]::Png

function Color([string]$hex) { return [System.Drawing.ColorTranslator]::FromHtml($hex) }
function Pixel($bmp, [int]$x, [int]$y, $color) {
    if ($x -ge 0 -and $x -lt 16 -and $y -ge 0 -and $y -lt 16) { $bmp.SetPixel($x, $y, $color) }
}
function Rect($bmp, [int]$x0, [int]$y0, [int]$x1, [int]$y1, $color) {
    for ($y = $y0; $y -le $y1; $y++) { for ($x = $x0; $x -le $x1; $x++) { Pixel $bmp $x $y $color } }
}
function Texture([string]$name, [scriptblock]$paint) {
    $bmp = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $bmp.MakeTransparent()
    & $paint $bmp
    $bmp.Save((Join-Path $textures "$name.png"), $png)
    $bmp.Dispose()
}

$stone = Color '#C8C4C0'; $stoneLight = Color '#E7E3D8'; $stoneDark = Color '#93919A'
$copper = Color '#AC7049'; $copperLight = Color '#DEAC73'; $copperDark = Color '#603F3E'
$ink = Color '#383343'; $violet = Color '#665290'; $cyan = Color '#72CDD9'; $bright = Color '#C4F4EB'
Texture 'workshop_stone' {
    param($b)
    Rect $b 0 0 15 15 $stone
    Rect $b 0 0 15 0 $stoneLight
    Rect $b 0 15 15 15 $stoneDark
    Rect $b 0 0 0 15 $stoneLight
    Rect $b 15 0 15 15 $stoneDark
    foreach ($p in @(@(3,3),@(10,2),@(12,7),@(5,11),@(2,13))) { Pixel $b $p[0] $p[1] $stoneLight }
    foreach ($p in @(@(6,5),@(13,12),@(1,8),@(8,14))) { Pixel $b $p[0] $p[1] $stoneDark }
}
Texture 'workshop_copper' {
    param($b)
    Rect $b 0 0 15 15 $copper
    Rect $b 0 0 15 1 $copperLight
    Rect $b 0 14 15 15 $copperDark
    Rect $b 0 0 1 15 $copperLight
    Rect $b 14 0 15 15 $copperDark
    Rect $b 3 5 12 5 (Color '#C38A5D')
    Rect $b 3 10 12 10 (Color '#86523F')
    foreach ($p in @(@(3,3),@(12,3),@(3,12),@(12,12))) { Pixel $b $p[0] $p[1] $copperLight }
}
Texture 'workshop_metal' {
    param($b)
    Rect $b 0 0 15 15 (Color '#484354')
    Rect $b 0 0 15 1 (Color '#746979')
    Rect $b 0 14 15 15 $ink
    Rect $b 2 3 13 3 $copper
    Rect $b 2 12 13 12 $copperDark
    foreach ($x in @(3,12)) { Pixel $b $x 7 $copperLight }
}
Texture 'workshop_rune' {
    param($b)
    Rect $b 0 0 15 15 $ink
    Rect $b 0 0 15 1 $copper
    Rect $b 0 14 15 15 $copperDark
    Rect $b 1 0 1 15 $copper
    Rect $b 14 0 14 15 $copperDark
    foreach ($x in 4..11) { Pixel $b $x 4 $violet; Pixel $b $x 11 $violet }
    foreach ($y in 4..11) { Pixel $b 4 $y $violet; Pixel $b 11 $y $violet }
    Rect $b 7 5 8 10 $cyan
    Rect $b 5 7 10 8 $cyan
    Rect $b 7 7 8 8 $bright
}
Texture 'workshop_parchment' {
    param($b)
    Rect $b 0 0 15 15 (Color '#E9DABC')
    Rect $b 0 0 15 1 (Color '#F7ECD6')
    Rect $b 0 14 15 15 (Color '#C4A88C')
    foreach ($y in @(4,7,10)) { Rect $b 3 $y 12 $y (Color '#B69D94') }
    Rect $b 3 12 8 12 (Color '#B69D94')
    Rect $b 12 0 15 3 (Color '#D1B79C')
}
Texture 'workshop_bench_surface' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 1 1 14 14 $copper
    Rect $b 2 2 13 13 $stoneLight
    Rect $b 3 3 11 12 (Color '#E9DABC')
    foreach ($y in @(5,8,11)) { Rect $b 4 $y 9 $y (Color '#9A7C88') }
    Rect $b 11 4 12 10 $violet
    Pixel $b 12 5 $cyan
    foreach ($p in @(@(1,1),@(14,1),@(1,14),@(14,14))) { Pixel $b $p[0] $p[1] $copperLight }
}
Texture 'workshop_bench_drawer' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 1 1 14 13 $copper
    Rect $b 2 3 13 11 (Color '#784E45')
    Rect $b 3 4 12 10 (Color '#A96D4F')
    Rect $b 6 6 9 7 $copperLight
    Rect $b 7 7 8 9 $ink
    Rect $b 0 14 15 15 $stoneDark
}
Texture 'workshop_basin' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 2 2 13 13 $copper
    Rect $b 3 3 12 12 $ink
    Rect $b 5 5 10 10 $violet
    Rect $b 6 6 9 9 (Color '#3B667F')
    Rect $b 7 7 8 8 $cyan
    foreach ($p in @(@(2,2),@(13,2),@(2,13),@(13,13))) { Pixel $b $p[0] $p[1] $copperLight }
}
Texture 'workshop_grinder_rim' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 1 1 14 14 $copper
    Rect $b 2 2 13 3 $copperLight
    Rect $b 2 12 13 13 (Color '#86523F')
    foreach ($x in @(3,7,11)) { Rect $b $x 5 $x 10 $violet; Pixel $b $x 7 $cyan }
    foreach ($p in @(@(2,2),@(13,2),@(2,13),@(13,13))) { Pixel $b $p[0] $p[1] $copperLight }
}
Texture 'workshop_grinder_plate' {
    param($b)
    Rect $b 0 0 15 15 $stoneDark
    Rect $b 1 1 14 14 $stoneLight
    Rect $b 2 2 13 13 $ink
    Rect $b 3 3 12 12 $copper
    Rect $b 5 5 10 10 $violet
    Rect $b 6 6 9 9 $ink
    Rect $b 7 7 8 8 $cyan
}
Texture 'workshop_glass' {
    param($b)
    Rect $b 1 0 2 15 (Color '#9EDFE1')
    Rect $b 13 0 14 15 (Color '#468997')
    Rect $b 3 1 12 1 (Color '#BAF1E8')
    Rect $b 4 5 4 8 (Color '#BAF1E8')
    Rect $b 11 9 11 12 (Color '#6AADC0')
    Pixel $b 6 3 (Color '#D2FFFF')
}
Texture 'workshop_tank_frame' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 1 0 4 15 $copperLight
    Rect $b 5 0 12 15 $copper
    Rect $b 13 0 15 15 (Color '#7C5144')
    foreach ($y in @(2,7,12)) { Rect $b 6 $y 11 $y $stoneLight; Pixel $b 8 $y $cyan }
}
Texture 'workshop_tank_cap' {
    param($b)
    Rect $b 0 0 15 15 $stoneDark
    Rect $b 1 1 14 14 $stoneLight
    Rect $b 2 2 13 13 $copper
    Rect $b 3 3 12 12 $ink
    Rect $b 4 4 11 11 $violet
    Rect $b 5 5 10 10 $copper
    Rect $b 6 6 9 9 $cyan
    Rect $b 7 7 8 8 $bright
    foreach ($p in @(@(2,2),@(13,2),@(2,13),@(13,13))) { Pixel $b $p[0] $p[1] $copperLight }
}
Texture 'workshop_gauge' {
    param($b)
    Rect $b 0 0 15 15 $copperDark
    Rect $b 1 1 14 14 $copper
    Rect $b 3 2 12 13 $ink
    Rect $b 7 3 8 12 $cyan
    foreach ($y in @(4,7,10)) { Rect $b 3 $y 5 $y $copperLight; Rect $b 10 $y 12 $y $copperLight }
}
Texture 'workshop_crystal_side' {
    param($b)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $color = if ($x -lt 4) { Color '#4684C0' }
                     elseif ($x -lt 8) { Color '#8FDDE7' }
                     elseif ($x -lt 12) { Color '#9D77DB' }
                     else { Color '#614CBA' }
            if ($y -lt 3) { $color = if ($x -lt 8) { Color '#C5F6F0' } else { Color '#CBABED' } }
            if ($y -gt 12) { $color = if ($x -lt 8) { Color '#365F99' } else { Color '#504190' } }
            Pixel $b $x $y $color
        }
    }
    for ($y = 3; $y -lt 12; $y++) { Pixel $b 7 $y $bright }
}
Texture 'workshop_crystal_tip' {
    param($b)
    Rect $b 0 0 15 15 (Color '#8064C9')
    Rect $b 2 2 13 13 (Color '#9DDDE7')
    Rect $b 5 5 10 10 $bright
    Rect $b 7 7 8 8 (Color '#FFFFFF')
}
Texture 'mana_tank_fill' {
    param($b)
    for ($y = 0; $y -lt 16; $y++) {
        $base = if ($y -lt 5) { Color '#70D9DA' } elseif ($y -lt 11) { Color '#6274C8' } else { Color '#6247A9' }
        Rect $b 0 $y 15 $y $base
    }
    Rect $b 0 1 15 2 (Color '#BCF6EC')
    Rect $b 3 5 5 5 (Color '#9EE9E4')
    Rect $b 10 9 12 9 (Color '#8994D5')
}
Texture 'workshop_brass_trim' {
    param($b)
    Rect $b 0 0 15 15 (Color '#72533D')
    Rect $b 1 1 14 14 (Color '#B58B58')
    Rect $b 2 2 13 12 (Color '#9A714A')
    Rect $b 2 2 13 3 (Color '#D6B47A')
    Rect $b 2 11 13 12 (Color '#694B3C')
    foreach ($p in @(@(3,5),@(12,5),@(3,10),@(12,10))) { Pixel $b $p[0] $p[1] (Color '#E0BF84') }
}
Texture 'workshop_book_left' {
    param($b)
    Rect $b 0 0 15 15 (Color '#6C413A')
    Rect $b 1 1 14 14 (Color '#D8C7A6')
    Rect $b 2 2 13 13 (Color '#EADDBF')
    Rect $b 13 1 14 14 (Color '#B79E84')
    foreach ($y in @(4,7,10,12)) { Rect $b 3 $y 10 $y (Color '#9F887D') }
    Rect $b 3 3 6 3 (Color '#826889')
}
Texture 'workshop_book_right' {
    param($b)
    Rect $b 0 0 15 15 (Color '#6C413A')
    Rect $b 1 1 14 14 (Color '#D8C7A6')
    Rect $b 2 2 13 13 (Color '#EADDBF')
    Rect $b 1 1 2 14 (Color '#B79E84')
    foreach ($y in @(4,7,10,12)) { Rect $b 5 $y 12 $y (Color '#9F887D') }
    Rect $b 7 5 9 7 (Color '#72629D')
    Pixel $b 8 6 (Color '#78C8CD')
}
Texture 'workshop_grinder_core' {
    param($b)
    Rect $b 0 0 15 15 (Color '#272634')
    Rect $b 1 1 14 14 (Color '#51485C')
    Rect $b 3 3 12 12 (Color '#302B3E')
    Rect $b 5 5 10 10 (Color '#493C68')
    Rect $b 7 7 8 8 (Color '#74B8C1')
    foreach ($p in @(@(2,2),@(13,2),@(2,13),@(13,13))) { Pixel $b $p[0] $p[1] (Color '#BA975E') }
}
Texture 'workshop_grinder_gear' {
    param($b)
    Rect $b 0 0 15 15 (Color '#36333D')
    Rect $b 2 2 13 13 (Color '#B58B58')
    Rect $b 4 4 11 11 (Color '#70523D')
    Rect $b 6 6 9 9 (Color '#353342')
    Rect $b 7 7 8 8 (Color '#77BDC4')
    foreach ($p in @(@(7,1),@(1,7),@(14,7),@(7,14))) { Pixel $b $p[0] $p[1] (Color '#D6B47A') }
}
Texture 'workshop_tank_window' {
    param($b)
    Rect $b 0 0 1 15 (Color '#527879')
    Rect $b 14 0 15 15 (Color '#527879')
    Rect $b 2 0 13 1 (Color '#8AB7B4')
    Rect $b 2 14 13 15 (Color '#446E76')
    Rect $b 3 2 4 12 (Color '#B5D8D2')
    Rect $b 11 3 11 8 (Color '#80B4B5')
    Pixel $b 7 4 (Color '#DBF0DB')
}
Texture 'workshop_tank_gauge' {
    param($b)
    Rect $b 0 0 15 15 (Color '#514741')
    Rect $b 1 1 14 14 (Color '#B58B58')
    Rect $b 3 2 12 13 (Color '#282934')
    Rect $b 7 3 8 12 (Color '#4B5669')
    foreach ($y in @(4,7,10)) { Rect $b 3 $y 5 $y (Color '#D7B77D'); Rect $b 10 $y 12 $y (Color '#D7B77D') }
}
Texture 'workshop_crystal_violet' {
    param($b)
    Rect $b 0 0 15 15 (Color '#43366D')
    Rect $b 1 0 4 15 (Color '#62519B')
    Rect $b 5 0 10 15 (Color '#9876BA')
    Rect $b 11 0 13 15 (Color '#785DA7')
    Rect $b 14 0 15 15 (Color '#392F64')
    Rect $b 5 0 10 2 (Color '#BDADE0')
    Rect $b 7 3 8 11 (Color '#BCD4E2')
    Rect $b 0 13 15 15 (Color '#34305E')
}

$tex = @{
    particle='minecraft:block/polished_andesite'
    stone='minecraft:block/polished_andesite'
    pale='minecraft:block/calcite'
    wood='minecraft:block/spruce_planks'
    darkwood='minecraft:block/dark_oak_planks'
    copper='minecraft:block/cut_copper'
    black='minecraft:block/polished_blackstone'
    brass='selarium:block/workshop_brass_trim'
    book_left='selarium:block/workshop_book_left'
    book_right='selarium:block/workshop_book_right'
    drawer='selarium:block/workshop_bench_drawer'
    core='selarium:block/workshop_grinder_core'
    gear='selarium:block/workshop_grinder_gear'
    glass='selarium:block/workshop_tank_window'
    gauge='selarium:block/workshop_tank_gauge'
    crystal='selarium:block/workshop_crystal_side'
    violet='selarium:block/workshop_crystal_violet'
    tip='selarium:block/workshop_crystal_tip'
}
function Element($from, $to, [string]$material, [string]$top = '', $rotation = $null) {
    if ($top -eq '') { $top = $material }
    $faces = @{}
    foreach ($side in @('north','south','east','west','down')) { $faces[$side] = @{ texture="#$material" } }
    $faces['up'] = @{ texture="#$top" }
    $part = @{ from=$from; to=$to; faces=$faces }
    if ($null -ne $rotation) { $part.rotation = $rotation }
    return $part
}
function FrontPanel($from, $to, [string]$material, [string]$front) {
    $part = Element $from $to $material
    $part.faces['north'] = @{ texture="#$front" }
    return $part
}
function Model([string]$name, $parts, $texturesUsed = $tex) {
    @{ parent='block/block'; textures=$texturesUsed; elements=@($parts); ambientocclusion=$true } |
        ConvertTo-Json -Depth 25 | Set-Content -LiteralPath (Join-Path $models "$name.json") -Encoding utf8
}
function ItemModel([string]$name, [double]$scale) {
    @{ parent="selarium:block/$name"; display=@{
        gui=@{ rotation=@(30,225,0); translation=@(0,0,0); scale=@($scale,$scale,$scale) }
        ground=@{ scale=@(0.5,0.5,0.5) }
        fixed=@{ rotation=@(0,180,0); scale=@(0.75,0.75,0.75) }
    }} | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath (Join-Path $items "$name.json") -Encoding utf8
}

# Writing table: spruce frame, calcite desk, open codex and brass fittings.
$bench = @(
    (Element @(1,0,1) @(5,2,5) 'stone'),
    (Element @(11,0,1) @(15,2,5) 'stone'),
    (Element @(1,0,11) @(5,2,15) 'stone'),
    (Element @(11,0,11) @(15,2,15) 'stone'),
    (Element @(2,2,2) @(4,10,4) 'darkwood'),
    (Element @(12,2,2) @(14,10,4) 'darkwood'),
    (Element @(2,2,12) @(4,10,14) 'darkwood'),
    (Element @(12,2,12) @(14,10,14) 'darkwood'),
    (Element @(4,4,2) @(12,6,4) 'wood'),
    (Element @(4,4,12) @(12,6,14) 'wood'),
    (Element @(1,9,1) @(15,12,15) 'darkwood' 'wood'),
    (Element @(0,11,0) @(16,13,16) 'brass' 'wood'),
    (Element @(2,13,2) @(14,14,13) 'pale'),
    (Element @(3,14,3) @(8,15,11) 'book_left'),
    (Element @(8,14,3) @(13,15,11) 'book_right'),
    (Element @(7,14,3) @(9,15,11) 'darkwood'),
    (Element @(2,13,13) @(14,16,16) 'darkwood' 'brass'),
    (FrontPanel @(5,6,0) @(11,10,2) 'darkwood' 'drawer'),
    (Element @(12,14,4) @(14,16,6) 'crystal' 'tip')
)
Model 'inscription_bench' $bench
ItemModel 'inscription_bench' 0.78

# Grinder: true recessed basin, guarded rotor, front gear and crank.
$grinder = @(
    (Element @(0,0,0) @(16,3,16) 'stone'),
    (Element @(1,3,1) @(15,4,15) 'brass'),
    (Element @(2,4,2) @(14,10,4) 'black'),
    (Element @(2,4,12) @(14,10,14) 'black'),
    (Element @(2,4,4) @(4,10,12) 'black'),
    (Element @(12,4,4) @(14,10,12) 'black'),
    (Element @(4,4,4) @(12,5,12) 'black' 'core'),
    (Element @(1,10,1) @(15,12,4) 'brass'),
    (Element @(1,10,12) @(15,12,15) 'brass'),
    (Element @(1,10,4) @(4,12,12) 'brass'),
    (Element @(12,10,4) @(15,12,12) 'brass'),
    (Element @(5,7,7) @(11,8,9) 'copper'),
    (Element @(7,7,5) @(9,8,11) 'copper'),
    (Element @(7,8,7) @(9,10,9) 'crystal' 'tip'),
    (FrontPanel @(5,4,0) @(11,10,2) 'black' 'gear'),
    (Element @(0,5,6) @(2,9,10) 'brass'),
    (Element @(14,5,6) @(16,9,10) 'brass'),
    (Element @(13,9,7) @(16,11,9) 'black'),
    (Element @(14,11,7) @(16,15,9) 'darkwood')
)
Model 'arcane_grinder' $grinder
ItemModel 'arcane_grinder' 0.82

# Vessel: four open glazed panels and a stepped lid with a pressure dial.
$tank = @(
    (Element @(1,0,1) @(15,3,15) 'stone'),
    (Element @(2,3,2) @(14,4,14) 'brass'),
    (Element @(3,4,3) @(13,13,13) 'glass'),
    (Element @(1,3,1) @(3,14,3) 'brass'),
    (Element @(13,3,1) @(15,14,3) 'brass'),
    (Element @(1,3,13) @(3,14,15) 'brass'),
    (Element @(13,3,13) @(15,14,15) 'brass'),
    (Element @(2,12,2) @(14,14,4) 'copper'),
    (Element @(2,12,12) @(14,14,14) 'copper'),
    (Element @(2,12,4) @(4,14,12) 'copper'),
    (Element @(12,12,4) @(14,14,12) 'copper'),
    (Element @(4,14,4) @(12,16,12) 'stone' 'brass'),
    (Element @(6,15,6) @(10,16,10) 'black' 'tip'),
    (FrontPanel @(7,5,1) @(9,12,3) 'brass' 'gauge')
)
Model 'mana_tank' $tank
ItemModel 'mana_tank' 0.82

function Shard([double]$x, [double]$z, [double]$width, [double]$height, [double]$angle, [string]$material) {
    $origin = @(($x + $width / 2),2,($z + $width / 2))
    $rotation = @{ origin=$origin; axis='z'; angle=$angle }
    return @(
        (Element @($x,1,$z) @(($x+$width),($height-3),($z+$width)) $material $material $rotation),
        (Element @(($x+0.5),($height-3),($z+0.5)) @(($x+$width-0.5),($height-1),($z+$width-0.5)) $material 'tip' $rotation),
        (Element @(($x+1),($height-1),($z+1)) @(($x+$width-1),$height,($z+$width-1)) 'tip' 'tip' $rotation)
    )
}
$crystalTextures = @{
    stone=$tex.stone; pale=$tex.pale; crystal=$tex.crystal; violet=$tex.violet; tip=$tex.tip
    particle=$tex.crystal
}
$small = @((Element @(5,0,5) @(11,2,11) 'pale')) + (Shard 6 6 4 7 0 'crystal')
$medium = @((Element @(4,0,4) @(12,2,12) 'pale')) +
    (Shard 6 6 4 9 0 'crystal') + (Shard 3 5 3 6 -22.5 'violet')
$large = @((Element @(3,0,3) @(13,2,13) 'pale')) +
    (Shard 6 6 5 12 0 'crystal') + (Shard 3 5 3 8 -22.5 'violet') + (Shard 10 6 3 7 22.5 'crystal')
$cluster = @((Element @(2,0,2) @(14,3,14) 'pale')) +
    (Shard 5 5 5 14 0 'crystal') + (Shard 2 5 4 10 -22.5 'violet') +
    (Shard 10 5 4 10 22.5 'crystal') + (Shard 4 10 3 7 -22.5 'violet') + (Shard 9 10 3 8 22.5 'crystal')
Model 'small_arcane_crystal_bud' $small $crystalTextures
Model 'medium_arcane_crystal_bud' $medium $crystalTextures
Model 'large_arcane_crystal_bud' $large $crystalTextures
Model 'arcane_crystal_cluster' $cluster $crystalTextures
ItemModel 'small_arcane_crystal_bud' 1.25
ItemModel 'medium_arcane_crystal_bud' 1.1
ItemModel 'large_arcane_crystal_bud' 1.0
ItemModel 'arcane_crystal_cluster' 0.9
