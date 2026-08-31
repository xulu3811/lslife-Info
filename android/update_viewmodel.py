path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsViewModel.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Add phone to state
c = c.replace('val message: String? = null,\n)', 'val message: String? = null,\n    val phone: String? = null,\n)')

# Update init block to collect user
old_init = """    init {
        viewModelScope.launch {
            combine(
                tokenStore.themeModeFlow,
                tokenStore.notificationModeFlow,
            ) { themeMode, notificationMode ->
                themeMode to notificationMode
            }.collect { (themeMode, notificationMode) ->
                _state.update {
                    it.copy(
                        themeMode = themeMode,
                        notificationMode = notificationMode,
                    )
                }
            }
        }
    }"""
new_init = """    init {
        viewModelScope.launch {
            combine(
                tokenStore.themeModeFlow,
                tokenStore.notificationModeFlow,
                authRepository.userFlow
            ) { themeMode, notificationMode, user ->
                Triple(themeMode, notificationMode, user)
            }.collect { (themeMode, notificationMode, user) ->
                _state.update {
                    it.copy(
                        themeMode = themeMode,
                        notificationMode = notificationMode,
                        phone = user?.phone
                    )
                }
            }
        }
    }"""
c = c.replace(old_init, new_init)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated SettingsViewModel.kt")
