$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
$pre = $lines[0..227]
$mid = @"
                DataBoardItem(
                    label = "??/??", 
                    count = user?.followersCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.FOLLOW_LIST) }
                )
            }
"@
$post = $lines[235..($lines.Length - 1)]
$pre + $mid + $post | Set-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt
