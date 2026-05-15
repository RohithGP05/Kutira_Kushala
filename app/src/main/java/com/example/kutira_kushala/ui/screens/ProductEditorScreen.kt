package com.example.kutira_kushala.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kutira_kushala.data.ImageCompressor
import com.example.kutira_kushala.data.genai.GenAiHelper
import com.example.kutira_kushala.data.model.Product
import com.example.kutira_kushala.data.model.ProductCategory
import com.example.kutira_kushala.data.repo.BusinessRepository
import com.example.kutira_kushala.data.repo.StorageRepository
import com.example.kutira_kushala.ui.components.FormSectionHeader
import com.example.kutira_kushala.ui.components.ImagePickerPlaceholder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorScreen(navController: NavHostController, existingId: String?) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val businessRepo = remember { BusinessRepository() }
    val storageRepo = remember { StorageRepository() }
    val db = remember { FirebaseFirestore.getInstance() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var weeklyCapacity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.OTHER) }
    var imageUrl by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var skillHint by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        val b = db.collection("businesses").document(uid).get().await()
        if (b.exists()) {
            skillHint = b.getString("skillArea").orEmpty()
        }
    }

    LaunchedEffect(existingId) {
        val id = existingId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val doc = db.collection("businesses").document(uid).collection("products").document(id).get().await()
        if (!doc.exists()) return@LaunchedEffect
        val p = Product.fromSnapshot(doc, uid)
        name = p.name
        description = p.description
        price = if (p.wholesalePrice == 0.0) "" else p.wholesalePrice.toString()
        unit = p.unit
        quantity = p.quantityText
        weeklyCapacity = p.weeklyCapacityText
        category = p.category
        imageUrl = p.imageUrl
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                statusIsError = false
                status = "Optimizing image..."
                val bytes = ImageCompressor.compressJpeg(context, uri)
                if (bytes.isEmpty()) {
                    statusIsError = true
                    status = "Failed to process image."
                    return@launch
                }
                status = "Uploading..."
                val path = "businesses/$uid/products/${existingId ?: "new_${System.currentTimeMillis()}"}.jpg"
                val url = storageRepo.uploadBytes(path, bytes)
                imageUrl = url
                status = "Success! Photo uploaded."
            } catch (e: Exception) {
                statusIsError = true
                status = "Upload failed. Please try again."
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (existingId.isNullOrBlank()) "Add New Product" else "Edit Product",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!existingId.isNullOrBlank()) {
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    businessRepo.deleteProduct(uid, existingId)
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    statusIsError = true
                                    status = "Delete failed."
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ImagePickerPlaceholder(
                imageUrl = imageUrl,
                onPick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                placeholderText = "Add Product Photo",
                placeholderIcon = Icons.Outlined.PhotoCamera
            )

            // Info Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSectionHeader("Product Details")
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g. Handwoven Silk Saree") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                Box {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth().clickable { menuExpanded = true },
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Category, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        ProductCategory.entries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.label) },
                                onClick = {
                                    category = c
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                }

                Column {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Tell buyers about your product...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        minLines = 3,
                        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) }
                    )
                    
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val suggestion = GenAiHelper.suggestProductDescription(
                                        businessSkill = skillHint,
                                        productName = name,
                                        category = category,
                                    )
                                    description = suggestion
                                } catch (e: Exception) {
                                    status = "AI suggestion failed."
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Suggest using AI")
                    }
                }
            }

            // Pricing and Stock
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSectionHeader("Wholesale & Capacity")
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { value -> price = value.filter { it.isDigit() || it == '.' } },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("e.g. per kg") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Available Stock") },
                    placeholder = { Text("e.g. 500 meters") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                OutlinedTextField(
                    value = weeklyCapacity,
                    onValueChange = { weeklyCapacity = it },
                    label = { Text("Weekly Production Capacity") },
                    placeholder = { Text("e.g. 2,000 meters/week") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }

            if (status != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusIsError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        status!!,
                        color = if (statusIsError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            isSaving = true
                            val isNew = existingId.isNullOrBlank()
                            val trimmedName = name.trim()
                            val trimmedUnit = unit.trim()
                            val priceValue = price.toDoubleOrNull()
                            
                            if (trimmedName.isBlank()) {
                                statusIsError = true
                                status = "Product name is required."
                                isSaving = false
                                return@launch
                            }
                            if (priceValue == null || priceValue <= 0.0) {
                                statusIsError = true
                                status = "Enter a valid price."
                                isSaving = false
                                return@launch
                            }
                            if (trimmedUnit.isBlank()) {
                                statusIsError = true
                                status = "Unit is required."
                                isSaving = false
                                return@launch
                            }
                            if (isNew && businessRepo.productCount(uid) >= BusinessRepository.MAX_PRODUCTS) {
                                statusIsError = true
                                status = "Limit reached: ${BusinessRepository.MAX_PRODUCTS} products max."
                                isSaving = false
                                return@launch
                            }
                            
                            val product = Product(
                                id = existingId.orEmpty(),
                                businessId = uid,
                                name = trimmedName,
                                description = description.trim(),
                                wholesalePrice = priceValue,
                                unit = trimmedUnit,
                                category = category,
                                quantityText = quantity.trim(),
                                weeklyCapacityText = weeklyCapacity.trim(),
                                imageUrl = imageUrl,
                            )
                            businessRepo.saveProduct(uid, product, existingId)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            statusIsError = true
                            status = "Save failed."
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Product", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
