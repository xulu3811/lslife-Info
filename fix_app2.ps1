$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
$lines[0..394] | Set-Content temp1.txt
Add-Content temp1.txt "            composable("
$lines[395..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt -Force
