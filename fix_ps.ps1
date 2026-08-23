$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
$content = $lines -join "`n"
$content = $content -replace "onOpenAdminReview", "onOpenAdminApprovals"
$content | Set-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
