package com.example.kutira_kushala.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.kutira_kushala.data.model.BusinessProfile
import com.example.kutira_kushala.data.repo.BusinessRepository
import com.example.kutira_kushala.data.repo.StorageRepository
import com.example.kutira_kushala.ui.components.FormSectionHeader
import com.example.kutira_kushala.ui.components.ImagePickerPlaceholder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(navController: NavHostController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val businessRepo = remember { BusinessRepository() }
    val storageRepo = remember { StorageRepository() }
    val remote by businessRepo.observeBusiness(uid).collectAsState(initial = null)

    var businessName by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var teamUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(remote) {
        val p = remote ?: return@LaunchedEffect
        businessName = p.businessName
        skill = p.skillArea
        location = p.locationText
        district = p.district
        state = p.state
        contactPhone = p.contactPhone
        whatsapp = p.whatsappNumber
        teamUrl = p.teamPhotoUrl
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                statusIsError = false
                status = "Optimizing photo..."
                val bytes = ImageCompressor.compressJpeg(context, uri)
                if (bytes.isEmpty()) {
                    statusIsError = true
                    status = "Failed to process image."
                    return@launch
                }
                status = "Uploading..."
                val url = storageRepo.uploadBytes("businesses/$uid/team.jpg", bytes)
                teamUrl = url
                status = "Photo updated. Click save to publish."
            } catch (e: Exception) {
                statusIsError = true
                status = "Upload failed. Try again."
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Business Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
                imageUrl = teamUrl,
                onPick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                placeholderText = "Upload Team / Workshop Photo",
                placeholderIcon = Icons.Outlined.Group
            )

            // Basic Info
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSectionHeader("Business Identity")
                
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) }
                )

                Column {
                    OutlinedTextField(
                        value = skill,
                        onValueChange = { skill = it },
                        label = { Text("Core Skills / Craft") },
                        placeholder = { Text("e.g. Pottery, Handloom, Metal Work") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(Icons.Outlined.Lightbulb, contentDescription = null) }
                    )
                    TextButton(
                        onClick = {
                            businessName = GenAiHelper.suggestBusinessTagline(skill, district)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Suggest name with AI")
                    }
                }
            }

            // Location
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSectionHeader("Location")
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Village / Area") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Outlined.Map, contentDescription = null) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // Contact
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSectionHeader("Contact Details")
                
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) }
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Outlined.Chat, contentDescription = null) }
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
                            val trimmedName = businessName.trim()
                            val trimmedSkill = skill.trim()
                            val trimmedDistrict = district.trim()
                            val trimmedState = state.trim()
                            
                            if (trimmedName.isBlank()) {
                                statusIsError = true
                                status = "Business name is required."
                                isSaving = false
                                return@launch
                            }
                            if (trimmedSkill.isBlank()) {
                                statusIsError = true
                                status = "Skill area is required."
                                isSaving = false
                                return@launch
                            }
                            if (trimmedDistrict.isBlank() || trimmedState.isBlank()) {
                                statusIsError = true
                                status = "District and state are required."
                                isSaving = false
                                return@launch
                            }
                            
                            val base = remote
                            val profile = BusinessProfile(
                                id = uid,
                                ownerUid = uid,
                                businessName = trimmedName,
                                skillArea = trimmedSkill,
                                locationText = location.trim(),
                                district = trimmedDistrict,
                                state = trimmedState,
                                teamPhotoUrl = teamUrl,
                                acceptingOrders = base?.acceptingOrders ?: false,
                                dailyCapacityText = base?.dailyCapacityText.orEmpty(),
                                weeklyCapacityNote = base?.weeklyCapacityNote.orEmpty(),
                                contactPhone = contactPhone.trim(),
                                whatsappNumber = whatsapp.trim(),
                                categories = base?.categories ?: emptyList(),
                            )
                            businessRepo.saveBusiness(profile)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            statusIsError = true
                            status = "Save failed. Check connection."
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
                    Text("Save Profile", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
