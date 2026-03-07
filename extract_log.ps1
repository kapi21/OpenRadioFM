$logFile = 'c:\@MIS PROYECTOS\K706_RE\OpenRadioFM\alps-tb8163p3_bsp-Android-9_2026-03-07_085148.logcat'
$outFile = 'c:\@MIS PROYECTOS\K706_RE\OpenRadioFM\filtered_log.txt'

$jsonObj = Get-Content $logFile -Raw | ConvertFrom-Json

$filtered = $jsonObj.logcatMessages | Where-Object { 
    $_.header.applicationId -eq 'com.example.openradiofm' -or 
    $_.header.logLevel -eq 'ERROR' -or 
    $_.header.logLevel -eq 'FATAL' 
}

$filtered | ForEach-Object {
    $time = $_.header.timestamp.seconds
    $tag = $_.header.tag
    $level = $_.header.logLevel
    $msg = $_.message
    "[$time] $level $tag : $msg"
} | Out-File -FilePath $outFile -Encoding utf8
