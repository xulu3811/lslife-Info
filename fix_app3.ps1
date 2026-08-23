$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
$lines[0..492] | Set-Content temp1.txt
Add-Content temp1.txt "                    onOpenFollowList = { navController.navigate(Routes.FOLLOW_LIST) },"
$lines[493..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt -Force
