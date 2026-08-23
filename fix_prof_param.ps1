$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
$lines[0..74] | Set-Content temp1.txt
Add-Content temp1.txt "    onOpenFavorites: () -> Unit,"
Add-Content temp1.txt "    onOpenFootprints: () -> Unit,"
Add-Content temp1.txt "    onOpenFollowList: () -> Unit,"
$lines[77..225] | Add-Content temp1.txt
$mid = @"
                DataBoardItem(
                    label = "??/??", 
                    count = user?.followersCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFollowList
                )
            }
"@
Add-Content temp1.txt $mid
$lines[233..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt -Force
