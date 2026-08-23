$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
$content = $lines -join "`n"
$content = $content -replace "userId = authState.user\?.id \?: `"`",", "userId = `"`","
$content | Set-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
