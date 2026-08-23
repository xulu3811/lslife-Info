import os
with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports_target = "import androidx.compose.foundation.lazy.staggeredgrid.items"
if imports_target in content:
    content = content.replace(imports_target, imports_target + "\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.ui.unit.sp\nimport androidx.compose.ui.graphics.Color")

# Add follow button
button_target = """                                    user.createdAt?.let { dateStr ->
                                        Spacer(modifier = Modifier.height(Dimens.xs))
                                        val date = try {
                                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(dateStr)
                                        } catch (e: Exception) { null }
                                        val formatted = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(it) } ?: dateStr
                                        Text(
                                            text = "加入连山同城: $formatted",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Dimens.md))

                                    // 商家资质 / 营业执照区域"""

button_replacement = """                                    user.createdAt?.let { dateStr ->
                                        Spacer(modifier = Modifier.height(Dimens.xs))
                                        val date = try {
                                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(dateStr)
                                        } catch (e: Exception) { null }
                                        val formatted = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(it) } ?: dateStr
                                        Text(
                                            text = "加入连山同城: $formatted",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

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
                                        Text(if (isFollowing) "已关注" else "+ 关注", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.md))

                                    // 商家资质 / 营业执照区域"""

if button_target in content:
    content = content.replace(button_target, button_replacement)
    with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched PublicProfileScreen")
else:
    print("Target not found in PublicProfileScreen")

