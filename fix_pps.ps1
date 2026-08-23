$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt
$lines[0..17] | Set-Content temp1.txt
Add-Content temp1.txt "import androidx.compose.foundation.shape.RoundedCornerShape"
Add-Content temp1.txt "import androidx.compose.ui.unit.sp"
$lines[18..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt -Force
