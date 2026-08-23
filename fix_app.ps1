$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
$lines[0..387] | Set-Content temp1.txt
$lines[389..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt -Force
