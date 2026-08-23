$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt
$newText = @"
                                    Spacer(modifier = Modifier.height(Dimens.md))
                                    
                                    val isFollowing = user.isFollowing
                                    Button(
                                        onClick = { viewModel.toggleFollow(user.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color(0xFFFF2442),
                                            contentColor = if (isFollowing) Color(0xFF999999) else Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 0.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(if (isFollowing) "???" else "+ ??", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
"@
$lines[0..136] | Set-Content temp1.txt
$lines[137..($lines.Length - 1)] | Set-Content temp2.txt
Add-Content temp1.txt $newText
Get-Content temp2.txt | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt -Force
Remove-Item temp2.txt
