package com.example.rightway_out.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.data.local.ShoppingItemEntity
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ShoppingViewModel

val categories = listOf("Stationery", "Uniform", "Bedding", "Toiletries", "Food", "Sports", "General")
val units       = listOf("pcs", "pairs", "kg", "litres", "boxes", "packets", "sets")

fun categoryColor(category: String): Color = when (category) {
    "Stationery" -> Color(0xFF1565C0)
    "Uniform"    -> Maroon700
    "Bedding"    -> Color(0xFF6A1B9A)
    "Toiletries" -> Color(0xFF00838F)
    "Food"       -> Color(0xFF2E7D32)
    "Sports"     -> Color(0xFFE65100)
    else         -> Color(0xFF546E7A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    studentId: String,
    onBack: () -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete  by remember { mutableStateOf<ShoppingItemEntity?>(null) }

    LaunchedEffect(studentId) { viewModel.loadItems(studentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shopping List", fontWeight = FontWeight.Bold)
                        Text("Track your school requirements", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700,
                    titleContentColor = White, navigationIconContentColor = White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Maroon700, contentColor = White
            ) {
                Icon(Icons.Default.Add, "Add Item")
            }
        },
        containerColor = Cream
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Summary banner
            val pending   = state.items.count { !it.isPurchased }
            val purchased = state.items.count { it.isPurchased }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Maroon800).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${state.items.size} items total",
                        color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("$pending pending · $purchased purchased",
                        color = White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. Total", color = Gold, fontSize = 11.sp)
                    Text("KSH %.0f".format(state.totalCost),
                        color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp),
                            tint = Maroon700.copy(alpha = 0.3f))
                        Text("No items yet", fontWeight = FontWeight.SemiBold, color = TextMid)
                        Text("Tap + to add items you need\nfor the next school term",
                            fontSize = 13.sp, color = TextMid.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                // Group by category
                val grouped = state.items.groupBy { it.category }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    grouped.forEach { (category, items) ->
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(modifier = Modifier.size(10.dp, 10.dp)
                                    .background(categoryColor(category), RoundedCornerShape(50)))
                                Text(category.uppercase(), fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor(category), letterSpacing = 1.sp)
                                Divider(modifier = Modifier.weight(1f),
                                    color = categoryColor(category).copy(alpha = 0.3f))
                            }
                        }
                        items(items, key = { it.id }) { item ->
                            ShoppingItemCard(
                                item = item,
                                onToggle = { viewModel.togglePurchased(studentId, item) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, unit, cost, category, notes ->
                viewModel.addItem(studentId, name, qty, unit, cost, category, notes)
                showAddDialog = false
            }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = Maroon700) },
            title = { Text("Remove Item?") },
            text = { Text("Remove '${item.name}' from your shopping list?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteItem(studentId, item.id); itemToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon700)) {
                    Text("Remove")
                }
            },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (item.isPurchased) 0.6f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) CreamDark else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPurchased) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Forest,
                    uncheckedColor = Maroon700
                )
            )

            // Item details
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                    color = if (item.isPurchased) TextMid else TextDark
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${item.quantity} ${item.unit}", fontSize = 12.sp, color = TextMid)
                    if (item.estimatedCost > 0) {
                        Text("·", fontSize = 12.sp, color = TextMid)
                        Text("KSH %.0f".format(item.estimatedCost * item.quantity),
                            fontSize = 12.sp, color = Maroon700, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (item.notes.isNotBlank()) {
                    Text(item.notes, fontSize = 11.sp,
                        color = TextMid.copy(alpha = 0.7f))
                }
            }

            // Category badge
            Box(modifier = Modifier
                .background(categoryColor(item.category).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)) {
                Text(item.category, fontSize = 10.sp, color = categoryColor(item.category),
                    fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.width(4.dp))

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = TextMid.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, Double, String, String) -> Unit
) {
    var name         by remember { mutableStateOf("") }
    var quantity     by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("pcs") }
    var cost         by remember { mutableStateOf("") }
    var selectedCat  by remember { mutableStateOf("General") }
    var notes        by remember { mutableStateOf("") }
    var catExpanded  by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var nameError    by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(modifier = Modifier.padding(24.dp)
                .heightIn(max = 560.dp)
                .then(Modifier), // scroll if needed
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Maroon700)

                OutlinedTextField(value = name, onValueChange = { name = it; nameError = false },
                    label = { Text("Item Name *") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name is required") }} else null,
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                        focusedLabelColor = Maroon700))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                            focusedLabelColor = Maroon700))

                    ExposedDropdownMenuBox(expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = selectedUnit, onValueChange = {}, readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                                focusedLabelColor = Maroon700))
                        ExposedDropdownMenu(expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }) {
                            units.forEach { u ->
                                DropdownMenuItem(text = { Text(u) },
                                    onClick = { selectedUnit = u; unitExpanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(value = cost, onValueChange = { cost = it },
                    label = { Text("Est. Cost per unit (KSH)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                        focusedLabelColor = Maroon700))

                ExposedDropdownMenuBox(expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(value = selectedCat, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                            focusedLabelColor = Maroon700))
                    ExposedDropdownMenu(expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) },
                                onClick = { selectedCat = cat; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700,
                        focusedLabelColor = Maroon700))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isBlank()) { nameError = true; return@Button }
                            onConfirm(
                                name.trim(),
                                quantity.toIntOrNull() ?: 1,
                                selectedUnit,
                                cost.toDoubleOrNull() ?: 0.0,
                                selectedCat,
                                notes.trim()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon700)
                    ) { Text("Add Item") }
                }
            }
        }
    }
}
