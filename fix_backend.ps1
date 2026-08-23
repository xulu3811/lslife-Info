$lines = Get-Content D:\LsLife\backend\src\modules\users.ts
$content = $lines -join "`n"
$content = $content -replace "prisma\.\\\(\[", "prisma.`$transaction(["
$content | Set-Content D:\LsLife\backend\src\modules\users.ts
