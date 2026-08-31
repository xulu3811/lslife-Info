path = 'app/src/main/java/com/lianshan/lslife/feature/settings/BindEmailScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('fun BindEmailScreen(\n    onBack: () -> Unit,\n    viewModel: SettingsViewModel = hiltViewModel(),', 
              'fun BindEmailScreen(\n    onBack: () -> Unit,\n    onChangeEmail: () -> Unit = {},\n    viewModel: SettingsViewModel = hiltViewModel(),')

c = c.replace('onClick = { /* TODO: Navigate to change phone flow */ }', 'onClick = onChangeEmail')
c = c.replace('onClick = { /* TODO: Navigate to change email flow */ }', 'onClick = onChangeEmail')
c = c.replace('onClick = { /* TODO: Navigate to bind phone flow */ }', 'onClick = onChangeEmail')
c = c.replace('onClick = { /* TODO: Navigate to bind email flow */ }', 'onClick = onChangeEmail')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated BindEmailScreen")
