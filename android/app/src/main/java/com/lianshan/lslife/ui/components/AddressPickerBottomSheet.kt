package com.qingyuan.lslife.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingyuan.lslife.core.data.AddressNode
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

    var selectedProvince by remember { mutableStateOf<AddressNode?>(addressNodes.firstOrNull()) }
    var selectedCity by remember { mutableStateOf<AddressNode?>(selectedProvince?.children?.firstOrNull()) }
    var selectedCounty by remember { mutableStateOf<AddressNode?>(selectedCity?.children?.firstOrNull()) }
    var selectedTown by remember { mutableStateOf<AddressNode?>(selectedCounty?.children?.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
            // Header with Cancel and Confirm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                }) {
                    Text("取消", color = Color.Gray, fontSize = 16.sp)
                }
                
                Text("选择所在地区", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                
                TextButton(onClick = {
                    val addressStr = listOfNotNull(
                        selectedProvince?.name,
                        selectedCity?.name,
                        selectedCounty?.name,
                        selectedTown?.name
                    ).joinToString("-")
                    
                    onAddressSelected(addressStr)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                }) {
                    Text("确定", color = Color(0xFFE53935), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wheel Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Province Wheel
                if (addressNodes.isNotEmpty()) {
                    WheelPicker(
                        modifier = Modifier.weight(1f),
                        items = addressNodes,
                        itemToString = { it.name },
                        onItemSelected = { _, item ->
                            selectedProvince = item
                            selectedCity = item.children?.firstOrNull()
                            selectedCounty = selectedCity?.children?.firstOrNull()
                            selectedTown = selectedCounty?.children?.firstOrNull()
                        }
                    )
                }

                // City Wheel
                val cityNodes = selectedProvince?.children ?: emptyList()
                if (cityNodes.isNotEmpty()) {
                    WheelPicker(
                        modifier = Modifier.weight(1f),
                        items = cityNodes,
                        itemToString = { it.name },
                        onItemSelected = { _, item ->
                            selectedCity = item
                            selectedCounty = item.children?.firstOrNull()
                            selectedTown = selectedCounty?.children?.firstOrNull()
                        }
                    )
                }

                // County Wheel
                val countyNodes = selectedCity?.children ?: emptyList()
                if (countyNodes.isNotEmpty()) {
                    WheelPicker(
                        modifier = Modifier.weight(1f),
                        items = countyNodes,
                        itemToString = { it.name },
                        onItemSelected = { _, item ->
                            selectedCounty = item
                            selectedTown = item.children?.firstOrNull()
                        }
                    )
                }

                // Town Wheel
                val townNodes = selectedCounty?.children ?: emptyList()
                if (townNodes.isNotEmpty()) {
                    WheelPicker(
                        modifier = Modifier.weight(1f),
                        items = townNodes,
                        itemToString = { it.name },
                        onItemSelected = { _, item ->
                            selectedTown = item
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
