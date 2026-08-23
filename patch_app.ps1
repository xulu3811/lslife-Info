$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt
$newText = @"
            composable(Routes.FOLLOW_LIST) {
                com.lianshan.lslife.feature.profile.FollowListScreen(
                    userId = authState.user?.id ?: "",
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { uid -> navController.navigate(Routes.publicProfile(uid)) }
                )
            }
"@
$lines[0..388] | Set-Content temp1.txt
$lines[389..($lines.Length - 1)] | Set-Content temp2.txt
Add-Content temp1.txt $newText
Get-Content temp2.txt | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt -Force
Remove-Item temp2.txt
