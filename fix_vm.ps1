$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileViewModel.kt
$lines[0..53] | Set-Content temp1.txt
$lines[55..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileViewModel.kt -Force
