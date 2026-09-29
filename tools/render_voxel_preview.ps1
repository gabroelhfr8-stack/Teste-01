param([string]$Output = (Join-Path $env:TEMP 'selarium_voxel_preview.png'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression

$assets = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium'
$vanillaJar = Join-Path $env:USERPROFILE '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar'
$archive = [System.IO.Compression.ZipFile]::OpenRead($vanillaJar)
$cache = @{}

function Texture([string]$id) {
    if ($cache.ContainsKey($id)) { return $cache[$id] }
    if ($id.StartsWith('selarium:')) {
        $path = Join-Path $assets ('textures/' + $id.Substring(9) + '.png')
        $bitmap = [System.Drawing.Bitmap]::FromFile((Resolve-Path $path))
    } else {
        $path = 'assets/minecraft/textures/' + $id.Substring(10) + '.png'
        $entry = $archive.GetEntry($path)
        if ($null -eq $entry) { throw "Minecraft texture missing: $path" }
        $stream = $entry.Open()
        $memory = [System.IO.MemoryStream]::new()
        try {
            $stream.CopyTo($memory)
            $memory.Position = 0
            $source = [System.Drawing.Bitmap]::new($memory)
            $bitmap = [System.Drawing.Bitmap]::new($source)
            $source.Dispose()
        } finally { $stream.Dispose(); $memory.Dispose() }
    }
    $cache[$id] = $bitmap
    return $bitmap
}

function Rotate($v, $rotation) {
    while ($v.Count -eq 1 -and $v[0] -is [array]) { $v = $v[0] }
    $x = [double]$v[0]; $y = [double]$v[1]; $z = [double]$v[2]
    if ($null -eq $rotation -or $rotation.axis -ne 'z') { return ,([double[]]@($x,$y,$z)) }
    $radians = [Math]::PI * [double]$rotation.angle / 180.0
    $ox=[double]$rotation.origin[0]; $oy=[double]$rotation.origin[1]
    $dx = $x - $ox; $dy = $y - $oy
    $rx=$ox + $dx * [Math]::Cos($radians) - $dy * [Math]::Sin($radians)
    $ry=$oy + $dx * [Math]::Sin($radians) + $dy * [Math]::Cos($radians)
    return ,([double[]]@($rx,$ry,$z))
}
function Project($v, [int]$cellX, [int]$cellY) {
    return [System.Drawing.PointF]::new([float]($cellX + 128 + ($v[0] - $v[2]) * 6),
        [float]($cellY + 78 + ($v[0] + $v[2]) * 3 - $v[1] * 6))
}
function Face($element, [string]$side, $model, [int]$cellX, [int]$cellY) {
    $x0=[double]$element.from[0]; $y0=[double]$element.from[1]; $z0=[double]$element.from[2]
    $x1=[double]$element.to[0]; $y1=[double]$element.to[1]; $z1=[double]$element.to[2]
    if ($side -eq 'up') {
        $vertices = [object[]]@([double[]]@($x0,$y1,$z0),[double[]]@($x1,$y1,$z0),
            [double[]]@($x0,$y1,$z1),[double[]]@($x1,$y1,$z1))
    } elseif ($side -eq 'north') {
        $vertices = [object[]]@([double[]]@($x0,$y1,$z0),[double[]]@($x1,$y1,$z0),
            [double[]]@($x0,$y0,$z0),[double[]]@($x1,$y0,$z0))
    } else {
        $vertices = [object[]]@([double[]]@($x0,$y1,$z0),[double[]]@($x0,$y1,$z1),
            [double[]]@($x0,$y0,$z0),[double[]]@($x0,$y0,$z1))
    }
    $rotated = @($vertices | ForEach-Object { Rotate $_ $element.rotation })
    $points = [System.Drawing.PointF[]]::new(3)
    for($i=0;$i -lt 3;$i++){ $points[$i]=Project $rotated[$i] $cellX $cellY }
    $fourth=Project $rotated[3] $cellX $cellY
    $textureKey = [string]$element.faces[$side].texture
    $id = [string]$model.textures[$textureKey.Substring(1)]
    $depth = ($rotated | ForEach-Object { $_[0] + $_[2] - $_[1] * 2.0 } | Measure-Object -Average).Average
    return [pscustomobject]@{ Points=$points; Fourth=$fourth; Texture=$id; Depth=$depth; Side=$side }
}

$names = @('inscription_bench','arcane_grinder','mana_tank','small_arcane_crystal_bud',
    'medium_arcane_crystal_bud','large_arcane_crystal_bud','arcane_crystal_cluster')
$sheet = [System.Drawing.Bitmap]::new(1024, 512)
$graphics = [System.Drawing.Graphics]::FromImage($sheet)
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(37,39,45))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $font=[System.Drawing.Font]::new('Arial', 12, [System.Drawing.FontStyle]::Bold)
    try {
        for($index=0;$index -lt $names.Count;$index++) {
            $name=$names[$index]
            $cellX=($index%4)*256; $cellY=[Math]::Floor($index/4)*256
            $model=Get-Content -LiteralPath (Join-Path $assets "models/block/$name.json") -Raw | ConvertFrom-Json -AsHashtable
            $faces=@()
            foreach($element in $model.elements) {
                foreach($side in @('up','north','west')) { $faces += Face $element $side $model $cellX $cellY }
            }
            foreach($face in $faces | Sort-Object Depth -Descending) {
                $bitmap=Texture $face.Texture
                $graphics.DrawImage($bitmap,$face.Points,
                    [System.Drawing.RectangleF]::new(0,0,$bitmap.Width,$bitmap.Height),
                    [System.Drawing.GraphicsUnit]::Pixel)
                $polygon=[System.Drawing.PointF[]]@($face.Points[0],$face.Points[1],$face.Fourth,$face.Points[2])
                if($face.Side -eq 'west') { $graphics.FillPolygon([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(28,0,0,0)),$polygon) }
                if($face.Side -eq 'north') { $graphics.FillPolygon([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(14,0,0,0)),$polygon) }
            }
            $graphics.DrawString($name,$font,[System.Drawing.Brushes]::White,$cellX+10,$cellY+225)
        }
    } finally { $font.Dispose() }
    $sheet.Save($Output,[System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose(); $sheet.Dispose(); $archive.Dispose()
    foreach($bitmap in $cache.Values){ $bitmap.Dispose() }
}
Write-Output $Output
