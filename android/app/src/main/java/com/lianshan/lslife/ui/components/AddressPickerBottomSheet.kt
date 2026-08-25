package com.lianshan.lslife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.core.data.AddressNode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerBottomSheet(
    addressNodes: List<AddressNode>,
    onDismissRequest: () -> Unit,
    onAddressSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedProvince by remember { mutableStateOf<AddressNode?>(null) }
    var selectedCity by remember { mutableStateOf<AddressNode?>(null) }
    var selectedCounty by remember { mutableStateOf<AddressNode?>(null) }
    var selectedTown by remember { mutableStateOf<AddressNode?>(null) }
    
    var currentTabIndex by remember { mutableIntStateOf(0) }

    val tabs = mutableListOf("请选择")
    if (selectedProvince != null) tabs[0] = selectedProvince!!.name
    if (selectedProvince != null) {
        if (selectedCity == null) tabs.add("请选择") else tabs.add(selectedCity!!.name)
    }
    if (selectedCity != null) {
        if (selectedCounty == null) tabs.add("请选择") else tabs.add(selectedCounty!!.name)
    }
    if (selectedCounty != null && !selectedCounty!!.children.isNullOrEmpty()) {
        if (selectedTown == null) tabs.add("请选择") else tabs.add(selectedTown!!.name)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择所在地区", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            ScrollableTabRow(
                selectedTabIndex = currentTabIndex,
                containerColor = Color.White,
                edgePadding = 16.dp,
                divider = { HorizontalDivider() },
                indicator = { tabPositions ->
                    if (currentTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                            color = Color(0xFFE53935)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTabIndex == index,
                        onClick = { currentTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                color = if (currentTabIndex == index) Color(0xFFE53935) else Color.Black,
                                fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }

            val currentList = when (currentTabIndex) {
                0 -> addressNodes
                1 -> selectedProvince?.children ?: emptyList()
                2 -> selectedCity?.children ?: emptyList()
                3 -> selectedCounty?.children ?: emptyList()
                else -> emptyList()
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currentList) { node ->
                    val isSelected = when (currentTabIndex) {
                        0 -> node.code == selectedProvince?.code
                        1 -> node.code == selectedCity?.code
                        2 -> node.code == selectedCounty?.code
                        3 -> node.code == selectedTown?.code
                        else -> false
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (currentTabIndex) {
                                    0 -> {
                                        selectedProvince = node
                                        selectedCity = null
                                        selectedCounty = null
                                        selectedTown = null
                                        currentTabIndex = 1
                                    }
                                    1 -> {
                                        selectedCity = node
                                        selectedCounty = null
                                        selectedTown = null
                                        currentTabIndex = 2
                                    }
                                    2 -> {
                                        selectedCounty = node
                                        selectedTown = null
                                        if (node.children.isNullOrEmpty()) {
                                            // 只有三级
                                            val addressStr = "${selectedProvince?.name ?: ""}-${selectedCity?.name ?: ""}-${node.name}"
                                            onAddressSelected(addressStr)
                                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                                        } else {
                                            currentTabIndex = 3
                                        }
                                    }
                                    3 -> {
                                        selectedTown = node
                                        val addressStr = "${selectedProvince?.name ?: ""}-${selectedCity?.name ?: ""}-${selectedCounty?.name ?: ""}-${node.name}"
                                        onAddressSelected(addressStr)
                                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = node.name,
                            color = if (isSelected) Color(0xFFE53935) else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
