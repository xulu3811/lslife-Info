package com.lianshan.lslife.feature.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.lianshan.lslife.R
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    merchantId: String?,
    sellerId: String?,
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit,
    onAddressClick: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val deliveryMethod = viewModel.deliveryMethod
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedPaymentMethod by remember { mutableStateOf("wechat") }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            TopAppBar(
                title = { Text("确认订单", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            if (!state.loading && state.error == null && state.entries.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.md, vertical = Dimens.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = Dimens.md)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("实付: ", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${"%.2f".format(state.totalAmount)}元",
                                    color = Color(0xFFE52F2F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Button(
                            onClick = { 
                                viewModel.submitOrder { orderId ->
                                    onOrderCreated(orderId)
                                } 
                            },
                            enabled = !state.isCreatingOrder && state.address != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F2F)),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                        ) {
                            Text(if (state.isCreatingOrder) "提交中..." else "立即支付", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(state.error!!, onRetry = { viewModel.loadData() }, modifier = Modifier.padding(padding).fillMaxSize())
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    // Header Section (Pickup or Delivery)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(Dimens.sm),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            if (deliveryMethod == "PICKUP") {
                                // Pickup UI
                                Column(Modifier.fillMaxWidth()) {
                                    // Map placeholder / Depot list
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color(0xFFE0E0E0))
                                    ) {
                                        // Mock map image would go here
                                        Text("地图区域加载中...", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                                    }
                                    
                                    Column(Modifier.padding(Dimens.md)) {
                                        Text("选择自提点", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(Dimens.sm))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE52F2F), RoundedCornerShape(8.dp)).padding(Dimens.sm),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(Icons.Filled.LocationOn, "自提点", tint = Color(0xFFE52F2F))
                                            Spacer(modifier = Modifier.width(Dimens.sm))
                                            Column(modifier = Modifier.weight(1f)) {
                                                val shopName = state.entries.firstOrNull()?.product?.merchant?.name ?: state.entries.firstOrNull()?.post?.user?.nickname ?: "Joybuy Delivery Depot"
                                                val address = state.entries.firstOrNull()?.product?.merchant?.address ?: "Unit 1, Southwark, London SE1"
                                                Text(shopName, fontWeight = FontWeight.Bold)
                                                Text(address, fontSize = 12.sp, color = Color.Gray)
                                                Text("距离 1.2km", fontSize = 12.sp, color = Color(0xFFE52F2F))
                                            }
                                            Icon(Icons.Filled.CheckCircle, "已选", tint = Color(0xFFE52F2F))
                                        }
                                        
                                        Spacer(modifier = Modifier.height(Dimens.md))
                                        Divider()
                                        Spacer(modifier = Modifier.height(Dimens.md))
                                        
                                        // Pickup Person
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onAddressClick() }) {
                                            Icon(Icons.Filled.Person, "联系人", tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(Dimens.sm))
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (state.address != null) {
                                                    Text("自提人: ${state.address!!.name} ${state.address!!.phone}", fontWeight = FontWeight.Bold)
                                                } else {
                                                    Text("请填写自提人信息", color = Color(0xFFE52F2F))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Delivery UI
                                Row(
                                    modifier = Modifier.padding(Dimens.lg).fillMaxWidth().clickable { onAddressClick() },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                                ) {
                                    Icon(Icons.Filled.LocationOn, "地址", tint = Color(0xFFE52F2F))
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (state.address != null) {
                                            Text("${state.address!!.name} ${state.address!!.phone}", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(state.address!!.address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        } else {
                                            Text("请先添加收货地址", color = Color(0xFFE52F2F), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Package details
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(Dimens.sm),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(Dimens.md)) {
                                Text("包裹 1/1", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(Dimens.sm))
                                
                                if (deliveryMethod == "PICKUP") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F7FF), RoundedCornerShape(4.dp)).padding(Dimens.sm)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Info, "提示", tint = Color(0xFF1677FF), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("预计自提时间: 15:00 - 16:00", color = Color(0xFF1677FF), fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.md))
                                }
                                
                                state.entries.forEach { entry ->
                                    val name = entry.product?.name ?: entry.post?.title ?: "商品"
                                    val price = entry.product?.price ?: entry.post?.price ?: 0.0
                                    val image = entry.product?.image ?: entry.post?.images?.firstOrNull() ?: ""

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.xs),
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        NetworkImage(image, name, Modifier.size(72.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                                            Spacer(modifier = Modifier.height(Dimens.xs))
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                                Text("${"%.2f".format(price)}元", color = Color(0xFFE52F2F), fontWeight = FontWeight.Bold)
                                                Text("x${entry.quantity}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Payment Methods
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(Dimens.sm),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(Dimens.md)) {
                                Text("支付方式", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(Dimens.sm))
                                
                                val isEnglish = java.util.Locale.getDefault().language == "en"
                                val paymentMethods = listOf(
                                    Triple("wechat", if (isEnglish) "WeChat Pay" else "微信支付", "安全快捷"),
                                    Triple("alipay", if (isEnglish) "Alipay" else "支付宝", "支持花呗")
                                )
                                
                                paymentMethods.forEach { (id, name, desc) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { selectedPaymentMethod = id }.padding(vertical = Dimens.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, fontWeight = FontWeight.Medium)
                                            Text(desc, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Icon(
                                            imageVector = if (selectedPaymentMethod == id) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (selectedPaymentMethod == id) Color(0xFFE52F2F) else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Summary
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(Dimens.sm),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(Dimens.md)) {
                                Text("汇总", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(Dimens.sm))
                                
                                Row(Modifier.fillMaxWidth().padding(vertical = Dimens.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("商品小计", color = Color.Gray)
                                    Text("${"%.2f".format(state.itemsTotal)}元")
                                }
                                Row(Modifier.fillMaxWidth().padding(vertical = Dimens.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("配送费", color = Color.Gray)
                                    Text(if (state.deliveryFee == 0.0) "包邮" else "${"%.2f".format(state.deliveryFee)}元")
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
