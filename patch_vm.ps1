$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileViewModel.kt
$newText = @"
    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val res = repository.toggleFollow(userId)
            if (res.isSuccess) {
                val isFollowing = res.getOrNull()?.get("isFollowing") == true
                _state.update {
                    it.copy(user = it.user?.copy(isFollowing = isFollowing))
                }
            } else {
                _state.update { it.copy(error = res.exceptionOrNull()?.message ?: "????") }
            }
        }
    }
}
"@
$lines[0..54] | Set-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileViewModel.kt
Add-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileViewModel.kt $newText
