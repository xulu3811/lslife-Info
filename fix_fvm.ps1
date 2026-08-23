$lines = Get-Content D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListViewModel.kt
$lines[0..27] | Set-Content temp1.txt
$mid = @"
class FollowListViewModel @Inject constructor(
    private val repository: LsRepository,
    private val authRepository: com.lianshan.lslife.core.data.AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FollowListState())
    val state = _state.asStateFlow()

    private var userId: String = ""

    fun init(id: String) {
        val finalId = if (id.isEmpty()) authRepository.cachedMe()?.id ?: return else id
        if (userId == finalId) return
        userId = finalId
        loadFollowing()
        loadFollowers()
    }
"@
Add-Content temp1.txt $mid
$lines[39..($lines.Length - 1)] | Add-Content temp1.txt
Move-Item temp1.txt D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListViewModel.kt -Force
