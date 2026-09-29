$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$folder = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium/textures/block'
$png = [System.Drawing.Imaging.ImageFormat]::Png

function Ink([int]$a, [int]$r, [int]$g, [int]$b, [int]$width = 2) {
    $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb($a, $r, $g, $b), $width)
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Bevel
    return $pen
}
function Line($g, $p, [int]$x1, [int]$y1, [int]$x2, [int]$y2) { $g.DrawLine($p, $x1, $y1, $x2, $y2) }
function Circle($g, $p, [int]$x, [int]$y, [int]$w, [int]$h) { $g.DrawEllipse($p, $x, $y, $w, $h) }
function Arc($g, $p, [int]$x, [int]$y, [int]$w, [int]$h, [int]$start, [int]$sweep) {
    $g.DrawArc($p, $x, $y, $w, $h, $start, $sweep)
}
function Path($g, $p, [int[]]$xy, [bool]$closed = $false) {
    $points = [System.Drawing.Point[]]::new($xy.Count / 2)
    for ($i = 0; $i -lt $xy.Count; $i += 2) { $points[$i / 2] = [System.Drawing.Point]::new($xy[$i], $xy[$i + 1]) }
    $g.DrawLines($p, $points)
    if ($closed) { $g.DrawLine($p, $points[$points.Length - 1], $points[0]) }
}
function Canvas([string]$fileName, [scriptblock]$paint) {
    $bitmap = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        & $paint $graphics
        $bitmap.Save((Join-Path $folder "$fileName.png"), $png)
    } finally { $graphics.Dispose(); $bitmap.Dispose() }
}

# A chalked ring with brass anchor pins; the selected ward is the only large mark.
Canvas 'arcane_sigil' {
    param($g)
    $chalk = Ink 200 194 184 165
    $shadow = Ink 170 83 69 93
    $brass = Ink 210 191 142 82
    try {
        foreach ($angle in @(12,102,192,282)) { Arc $g $shadow 5 5 54 54 ($angle + 2) 66; Arc $g $chalk 7 7 50 50 $angle 63 }
        foreach ($angle in @(46,136,226,316)) { Arc $g $chalk 14 14 36 36 $angle 43 }
        foreach ($pair in @(@(31,2,31,10),@(31,54,31,62),@(2,31,10,31),@(54,31,62,31))) {
            Line $g $brass $pair[0] $pair[1] $pair[2] $pair[3]
        }
        foreach ($point in @(@(15,15),@(47,15),@(15,47),@(47,47))) { Circle $g $brass $point[0] $point[1] 2 2 }
        foreach ($point in @(@(7,19),@(20,5),@(49,9),@(57,42),@(10,51),@(43,58))) {
            $g.FillRectangle([System.Drawing.Brushes]::LightGray, $point[0], $point[1], 1, 1)
        }
    } finally { $chalk.Dispose(); $shadow.Dispose(); $brass.Dispose() }
}
Canvas 'arcane_sigil_active' {
    param($g)
    $glow = Ink 130 101 209 215 3
    $spark = Ink 180 220 245 223 2
    try {
        foreach ($angle in @(16,106,196,286)) { Arc $g $glow 4 4 56 56 $angle 54 }
        foreach ($pair in @(@(31,2,31,8),@(31,56,31,62),@(2,31,8,31),@(56,31,62,31))) {
            Line $g $spark $pair[0] $pair[1] $pair[2] $pair[3]
        }
    } finally { $glow.Dispose(); $spark.Dispose() }
}

function Draw-Symbol([string]$name, $g, $p, $a) {
    switch ($name) {
        'ambient_mana' { Path $g $p @(32,16,23,29,23,39,32,46,41,39,41,29,32,16) $true; Circle $g $a 29 30 6 6 }
        'whispering' { Arc $g $p 20 19 20 27 255 290; Arc $g $a 37 22 10 20 280 160; Arc $g $a 42 18 10 28 280 160 }
        'spectral' { Path $g $p @(16,32,24,25,32,22,40,25,48,32,40,39,32,42,24,39,16,32) $true; Circle $g $a 28 28 8 8 }
        'bulwark' { Path $g $p @(32,15,46,21,44,38,32,48,20,38,18,21,32,15) $true; Line $g $a 32 20 32 42 }
        'rejuvenation' { Path $g $p @(32,46,18,33,19,24,25,21,32,27,39,21,45,24,46,33,32,46) $true; Line $g $a 32 27 32 37; Line $g $a 27 32 37 32 }
        'featherweight' { Path $g $p @(18,45,26,24,43,17,46,24,38,40,18,45) $true; Line $g $a 20 43 42 22; Line $g $a 27 36 25 28; Line $g $a 34 29 37 21 }
        'grounding' { Line $g $p 32 16 32 39; Path $g $p @(23,31,32,42,41,31); Line $g $a 18 45 46 45; Line $g $a 23 49 41 49 }
        'magnetism' { Arc $g $p 20 18 24 28 0 180; Line $g $p 20 32 20 43; Line $g $p 44 32 44 43; Line $g $a 17 43 23 43; Line $g $a 41 43 47 43 }
        'banishment' { Circle $g $p 27 27 10 10; foreach ($q in @(@(32,18,32,12),@(32,46,32,52),@(18,32,12,32),@(46,32,52,32))) { Line $g $a $q[0] $q[1] $q[2] $q[3] } }
        'eclipse' { Arc $g $p 18 17 29 29 45 270; Arc $g $a 26 15 24 26 65 215 }
        'fertility' { Line $g $p 32 47 32 28; Path $g $a @(32,34,22,31,18,24,27,22,32,28); Path $g $a @(32,29,38,21,47,23,43,30,32,34); Line $g $p 22 48 42 48 }
        'citadel' { Path $g $p @(19,46,19,25,24,25,24,19,29,19,29,25,35,25,35,19,40,19,40,25,45,25,45,46,19,46) $true; Path $g $a @(28,46,28,35,32,32,36,35,36,46) }
        'disruption' { Path $g $p @(20,24,26,18,31,23); Path $g $p @(33,40,38,46,44,40); Line $g $a 20 43 44 21; Line $g $a 20 21 44 43 }
        'cloaking' { Path $g $p @(18,42,23,23,32,18,41,23,46,42,38,37,32,40,26,37,18,42) $true; Line $g $a 21 45 45 18 }
        'accelerating' { Path $g $p @(17,22,29,32,17,42); Path $g $p @(30,22,42,32,30,42); Line $g $a 43 32 49 32 }
        'efficiency' { Circle $g $p 23 23 18 18; Circle $g $a 29 29 6 6; foreach ($q in @(@(32,17,32,12),@(32,47,32,52),@(17,32,12,32),@(47,32,52,32))) { Line $g $p $q[0] $q[1] $q[2] $q[3] } }
        'crushing' { Path $g $p @(20,22,44,22,40,28,24,28,20,22) $true; Line $g $a 32 29 32 39; Path $g $p @(22,43,42,43,46,47,18,47,22,43) $true }
        'inversion' { Path $g $p @(21,30,32,18,43,30); Line $g $p 32 19 32 45; Line $g $a 22 47 42 47 }
        'aqualung' { Arc $g $p 19 18 25 25 20 140; Circle $g $a 24 18 5 5; Circle $g $a 35 15 4 4; Path $g $p @(17,39,23,36,29,39,35,36,41,39,47,36) }
        'transmutation' { Path $g $p @(32,17,45,39,19,39,32,17) $true; Path $g $a @(40,35,45,39,45,31); Path $g $a @(24,35,19,39,26,42) }
        'tangible' { Path $g $p @(20,23,32,17,44,23,44,40,32,47,20,40,20,23) $true; Line $g $a 20 23 32 30; Line $g $a 44 23 32 30; Line $g $a 32 30 32 47 }
        'sanctuary' { Arc $g $a 17 17 30 13 180 180; Path $g $p @(32,22,44,27,42,40,32,47,22,40,20,27,32,22) $true }
        'bounty' { Circle $g $p 20 20 24 24; Path $g $a @(32,23,35,29,42,30,37,35,38,42,32,38,26,42,27,35,22,30,29,29,32,23) $true }
        'immortal' { Path $g $p @(32,45,19,33,19,25,25,21,32,27,39,21,45,25,45,33,32,45) $true; Arc $g $a 25 27 14 11 45 270 }
        'drain' { Path $g $p @(32,17,23,31,23,40,32,46,41,40,41,31,32,17) $true; Line $g $a 32 26 32 40; Path $g $a @(27,35,32,41,37,35) }
        'soul_chain' { Circle $g $p 17 25 17 15; Circle $g $p 30 25 17 15; Line $g $a 27 32 37 32 }
        'stasis' { Path $g $p @(21,18,43,18,36,29,32,32,28,29,21,18) $true; Path $g $p @(21,46,43,46,36,35,32,32,28,35,21,46) $true; Line $g $a 32 29 32 35 }
        'maelstrom' { Arc $g $p 17 17 30 30 10 300; Arc $g $a 24 24 16 16 35 260; Circle $g $a 30 30 4 4 }
        'decay' { Path $g $p @(18,44,22,24,38,18,46,28,41,44,18,44) $true; Line $g $a 20 42 39 23; Line $g $a 30 34 35 39; Line $g $a 34 29 29 24 }
        'deflection' { Path $g $p @(30,17,43,23,42,38,30,46,19,38,18,23,30,17) $true; Path $g $a @(37,28,47,28,43,22); Line $g $a 47 28 42 34 }
        'silence' { Path $g $p @(18,27,24,27,32,21,32,43,24,37,18,37,18,27) $true; Line $g $a 17 47 47 17 }
        'phasing' { Line $g $p 21 18 21 46; Line $g $p 43 18 43 46; foreach ($y in @(22,29,36,43)) { Line $g $a 28 $y 36 $y } }
        'incomplete' { Path $g $p @(32,18,43,31,32,45,21,31,32,18); Line $g $a 32 23 32 29; Circle $g $a 31 35 2 2 }
        'focus' { Circle $g $p 22 22 20 20; Line $g $a 32 17 32 25; Line $g $a 32 39 32 47; Line $g $a 17 32 25 32; Line $g $a 39 32 47 32 }
        'aegis' { Path $g $p @(32,17,44,22,42,39,32,47,22,39,20,22,32,17) $true }
        'vital' { Path $g $p @(32,46,19,33,19,25,25,21,32,27,39,21,45,25,45,33,32,46) $true }
        'echo' { Arc $g $p 22 23 13 18 280 160; Arc $g $a 28 19 15 26 280 160 }
        'binding' { Circle $g $p 17 25 18 15; Circle $g $a 29 25 18 15 }
        'density' { Path $g $p @(20,24,32,18,44,24,44,40,32,46,20,40,20,24) $true }
        'warp' { Arc $g $p 18 18 28 28 20 280; Arc $g $a 26 25 13 13 50 250 }
        'veil' { Path $g $p @(18,43,22,24,32,18,42,24,46,43,32,39,18,43) $true }
        'chrono' { Path $g $p @(22,18,42,18,35,29,29,35,22,46,42,46,35,35,29,29,22,18) }
        'arcane' { Path $g $p @(32,17,37,27,47,32,37,37,32,47,27,37,17,32,27,27,32,17) $true }
    }
}

$wards = @('ambient_mana','whispering','spectral','bulwark','rejuvenation','featherweight',
    'grounding','magnetism','banishment','eclipse','fertility','citadel','disruption','cloaking',
    'accelerating','efficiency','crushing','inversion','aqualung','transmutation','tangible',
    'sanctuary','bounty','immortal','drain','soul_chain','stasis','maelstrom','decay',
    'deflection','silence','phasing','incomplete')
$helpful = @('ambient_mana','whispering','spectral','bulwark','rejuvenation','featherweight',
    'grounding','magnetism','aqualung','fertility','cloaking','efficiency','immortal','sanctuary')
$mechanical = @('citadel','tangible','transmutation','accelerating','bounty','deflection')
foreach ($name in $wards) {
    $rgb = if ($helpful -contains $name) { @(141,214,209) }
           elseif ($mechanical -contains $name) { @(219,177,111) }
           else { @(187,152,213) }
    Canvas "arcane_sigil_$name" {
        param($g)
        $outline = Ink 120 45 40 58 4
        $main = Ink 235 $rgb[0] $rgb[1] $rgb[2] 3
        $accent = Ink 225 226 226 201 2
        try {
            Draw-Symbol $name $g $outline $outline
            Draw-Symbol $name $g $main $accent
        } finally { $outline.Dispose(); $main.Dispose(); $accent.Dispose() }
    }
}
foreach ($name in @('arcane','aegis','vital','focus','binding','echo','density','warp','veil','chrono')) {
    Canvas "arcane_sigil_component_$name" {
        param($g)
        $main = Ink 205 182 146 202 3
        $accent = Ink 210 113 199 207 2
        try { Draw-Symbol $name $g $main $accent }
        finally { $main.Dispose(); $accent.Dispose() }
    }
}
