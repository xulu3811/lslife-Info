$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
$newText = @"
                DataBoardItem(
                    label = "??/??", 
                    count = user?.followersCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.FOLLOW_LIST) }
                )
"@
$lines[0..228] | Set-Content temp1.txt
$lines[234..($lines.Length - 1)] | Set-Content temp2.txt
Add-Content temp1.txt $newText
Get-Content temp2.txt | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt -Force
Remove-Item temp2.txt
