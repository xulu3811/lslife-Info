
$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\PostDetailScreen.kt
$pre = $lines[0..488]
$post = $lines[534..($lines.Length - 1)]
$mid = @"
                                modifier = Modifier.padding(Dimens.lg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = post.user?.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(scheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(Dimens.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.user?.nickname ?: "????", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    val authLabel = post.user?.authLabel ?: "??????"
                                    val isMerchant = post.user?.isMerchant == true || post.publisherType == "MERCHANT"
                                    Text(
                                        text = authLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMerchant) Color(0xFFD4AF37) else scheme.onSurfaceVariant
                                    )
                                }
                                
                                val isFollowing = post.isFollowing
                                Button(
                                    onClick = { viewModel.toggleFollow(post.user?.id ?: "") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color(0xFFFF2442),
                                        contentColor = if (isFollowing) Color(0xFF999999) else Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isFollowing) "???" else "??", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
"@
Set-Content -Path D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\PostDetailScreen.kt -Value ($pre + $mid + $post)

