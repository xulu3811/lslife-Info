$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListScreen.kt
$content = $lines -join "`n"
$content = $content -replace "viewModel.init\(userId\)", "viewModel.initialize(userId)"
$content | Set-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListScreen.kt
