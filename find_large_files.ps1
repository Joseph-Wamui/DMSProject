Get-ChildItem -Recurse -File | 
Where-Object { $_.Length -gt 50MB } | 
Select-Object FullName, @{Name="SizeInMB";Expression={$_.Length / 1MB}} | 
Format-Table -AutoSize
