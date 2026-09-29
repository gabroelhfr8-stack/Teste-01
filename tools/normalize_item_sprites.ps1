$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$folder = Join-Path $PSScriptRoot '../src/main/resources/assets/selarium/textures/item'
foreach ($file in Get-ChildItem -LiteralPath $folder -Filter '*.png') {
    $source = [System.Drawing.Bitmap]::FromFile($file.FullName)
    try {
        if ($source.Width -le 64 -and $source.Height -le 64) { continue }
        $target = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($target)
            try {
                $graphics.Clear([System.Drawing.Color]::Transparent)
                $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
                $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
                $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(0, 0, 32, 32),
                    0, 0, $source.Width, $source.Height, [System.Drawing.GraphicsUnit]::Pixel)
            } finally { $graphics.Dispose() }
            $temporary = "$($file.FullName).normalized.png"
            $target.Save($temporary, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $target.Dispose() }
    } finally { $source.Dispose() }
    Move-Item -LiteralPath $temporary -Destination $file.FullName -Force
}
