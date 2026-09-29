$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$block = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium/textures/block'
$item = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium/textures/item'
New-Item -ItemType Directory -Path $block, $item -Force | Out-Null
$png = [System.Drawing.Imaging.ImageFormat]::Png

function Seed([string]$name) {
    [long]$value = 17
    foreach ($character in $name.ToCharArray()) { $value = ($value * 31 + [int]$character) % 2147483647 }
    return [int]$value
}

function Draw-Glyph([string]$name, [string]$path, [bool]$component) {
    if (Test-Path -LiteralPath $path) { return }
    $seed = Seed $name
    $bitmap = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $color = if ($component) { [System.Drawing.Color]::FromArgb(190, 87, 207, 221) }
             else { [System.Drawing.Color]::FromArgb(205, 177, 122, 218) }
    $pen = [System.Drawing.Pen]::new($color, 2.0)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $graphics.DrawArc($pen, 15, 15, 34, 34, ($seed % 110) + 10, 190 + ($seed % 80))
    $graphics.DrawArc($pen, 19, 19, 26, 26, (($seed / 7) % 120) + 180, 105)
    $vertexCount = 3 + ($seed % 4)
    $vertices = [System.Drawing.Point[]]::new($vertexCount + 1)
    for ($index = 0; $index -lt $vertexCount; $index++) {
        $angle = $index * 2 * [Math]::PI / $vertexCount + ($seed % 17) * 0.13
        $radius = 8 + (($seed / (1 + $index * 3)) % 6)
        $vertices[$index] = [System.Drawing.Point]::new(
            [int][Math]::Round(32 + [Math]::Cos($angle) * $radius),
            [int][Math]::Round(32 + [Math]::Sin($angle) * $radius))
    }
    $vertices[$vertexCount] = $vertices[0]
    $graphics.DrawLines($pen, $vertices)
    $graphics.DrawLine($pen, $vertices[0], $vertices[[int][Math]::Floor($vertexCount / 2)])
    for ($index = 0; $index -lt 3; $index++) {
        $angle = ($seed % 13) * 0.25 + $index * 2 * [Math]::PI / 3
        $x = [int][Math]::Round(32 + [Math]::Cos($angle) * 23)
        $y = [int][Math]::Round(32 + [Math]::Sin($angle) * 23)
        $graphics.FillEllipse([System.Drawing.Brushes]::LightCyan, $x - 1, $y - 1, 3, 3)
    }
    $bitmap.Save($path, $png)
    $pen.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
}

$wardNames = @(
    'ambient_mana','whispering','spectral','bulwark','rejuvenation','featherweight',
    'grounding','magnetism','banishment','eclipse','fertility','citadel','disruption',
    'cloaking','accelerating','efficiency','crushing','inversion','aqualung',
    'transmutation','tangible','sanctuary','bounty','immortal','drain','soul_chain',
    'stasis','maelstrom','decay','deflection','silence','phasing'
)
foreach ($name in $wardNames) {
    Draw-Glyph $name (Join-Path $block "arcane_sigil_$name.png") $false
}
foreach ($name in @('arcane','aegis','vital','focus','binding','echo','density','warp','veil','chrono')) {
    Draw-Glyph $name (Join-Path $block "arcane_sigil_component_$name.png") $true
}

function Draw-Dust([string]$name, [string]$path, [System.Drawing.Color]$color) {
    if (Test-Path -LiteralPath $path) { return }
    $bitmap = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $dark = [System.Drawing.Color]::FromArgb(255, [Math]::Max(0, $color.R - 55),
        [Math]::Max(0, $color.G - 55), [Math]::Max(0, $color.B - 55))
    $light = [System.Drawing.Color]::FromArgb(255, [Math]::Min(255, $color.R + 45),
        [Math]::Min(255, $color.G + 45), [Math]::Min(255, $color.B + 45))
    $baseBrush = [System.Drawing.SolidBrush]::new($color)
    $darkBrush = [System.Drawing.SolidBrush]::new($dark)
    $lightBrush = [System.Drawing.SolidBrush]::new($light)
    $pile = [System.Drawing.Point[]]@(
        [System.Drawing.Point]::new(5,23), [System.Drawing.Point]::new(8,17),
        [System.Drawing.Point]::new(12,13), [System.Drawing.Point]::new(16,15),
        [System.Drawing.Point]::new(20,11), [System.Drawing.Point]::new(25,20),
        [System.Drawing.Point]::new(28,24), [System.Drawing.Point]::new(22,27),
        [System.Drawing.Point]::new(10,27))
    $graphics.FillPolygon($darkBrush, $pile)
    $graphics.FillEllipse($baseBrush, 7, 16, 19, 10)
    $seed = Seed $name
    for ($index = 0; $index -lt 18; $index++) {
        $x = 8 + (($seed + $index * 17) % 17)
        $y = 16 + (([int]($seed / (1 + $index)) + $index * 7) % 9)
        $graphics.FillRectangle($(if ($index % 3 -eq 0) { $lightBrush } else { $baseBrush }), $x, $y, 2, 2)
    }
    $graphics.FillRectangle($lightBrush, 19, 12, 3, 3)
    $bitmap.Save($path, $png)
    $baseBrush.Dispose(); $darkBrush.Dispose(); $lightBrush.Dispose()
    $graphics.Dispose(); $bitmap.Dispose()
}

Draw-Dust 'basic_binding' (Join-Path $item 'basic_binding_dust.png') ([System.Drawing.Color]::FromArgb(183, 122, 105))
Draw-Dust 'basic_focus' (Join-Path $item 'basic_focus_dust.png') ([System.Drawing.Color]::FromArgb(91, 189, 209))
Draw-Dust 'basic_vital' (Join-Path $item 'basic_vital_dust.png') ([System.Drawing.Color]::FromArgb(195, 113, 152))
foreach ($name in @('arcane','aegis','binding','focus','vital')) {
    $target = Join-Path $item "${name}_dust.png"
    if (-not (Test-Path -LiteralPath $target)) {
        Copy-Item -LiteralPath (Join-Path $item "basic_${name}_dust.png") -Destination $target
    }
}

function Draw-Scroll([string]$name, [int]$variant) {
    $path = Join-Path $item "$name.png"
    if (Test-Path -LiteralPath $path) { return }
    $bitmap = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.FillRectangle([System.Drawing.Brushes]::SaddleBrown, 5, 5, 22, 23)
    $graphics.FillRectangle([System.Drawing.Brushes]::LemonChiffon, 7, 6, 18, 20)
    $graphics.FillRectangle([System.Drawing.Brushes]::Peru, 4, 5, 24, 3)
    $graphics.FillRectangle([System.Drawing.Brushes]::Peru, 4, 25, 24, 3)
    $graphics.FillRectangle([System.Drawing.Brushes]::BurlyWood, 6, 8, 2, 16)
    if ($variant -eq 1) {
        $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::MediumTurquoise, 2)
        $graphics.DrawEllipse($pen, 11, 11, 10, 10)
        $graphics.DrawLine($pen, 16, 9, 16, 23)
        $pen.Dispose()
    } elseif ($variant -eq 2) {
        $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::MediumPurple, 2)
        $graphics.DrawEllipse($pen, 11, 11, 10, 10)
        $graphics.DrawLine($pen, 11, 21, 21, 11)
        $graphics.DrawLine($pen, 11, 11, 21, 21)
        $pen.Dispose()
    }
    $bitmap.Save($path, $png)
    $graphics.Dispose(); $bitmap.Dispose()
}
Draw-Scroll 'empty_scroll' 0
Draw-Scroll 'attunement_scroll' 1
Draw-Scroll 'ward_scroll' 2
