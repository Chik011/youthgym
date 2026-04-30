package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.GymPackage
import com.chiko0085.testgym.Member
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    members: MutableList<Member>,
    gymPackages: MutableList<GymPackage>,
    totalRevenue: Double,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showPackageList by remember { mutableStateOf(false) }

    if (showPackageList) {
        PackageManagementScreen(
            packages = gymPackages,
            onBack = { showPackageList = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showPackageList = true }) {
                            Icon(Icons.Default.List, contentDescription = "Packages", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Member")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Revenue Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Total Pendapatan", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Rp ${String.format("%,.0f", totalRevenue)}", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Member...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Daftar Member", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(members.filter { it.name.contains(searchQuery, true) }) { member ->
                        MemberCard(
                            member = member,
                            onScan = {
                                val idx = members.indexOfFirst { it.id == member.id }
                                if (idx != -1 && member.remainingDays > 0) {
                                    members[idx] = member.copy(remainingDays = member.remainingDays - 1)
                                }
                            },
                            onEdit = { memberToEdit = member },
                            onDelete = { memberToDelete = member }
                        )
                    }
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, d, price ->
                members.add(Member(UUID.randomUUID().toString().take(5), n, u, p, d))
                onUpdateRevenue(totalRevenue + price)
                showAddDialog = false
            }
        )
    }

    if (memberToEdit != null) {
        EditMemberDialog(member = memberToEdit!!, onDismiss = { memberToEdit = null }, onConfirm = { updated ->
            val idx = members.indexOfFirst { it.id == updated.id }
            if (idx != -1) members[idx] = updated
            memberToEdit = null
        })
    }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Hapus Member") },
            text = { Text("Yakin ingin menghapus ${memberToDelete?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    members.removeAll { it.id == memberToDelete?.id }
                    memberToDelete = null
                }) { Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun MemberCard(member: Member, onScan: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Text(member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${member.remainingDays} Hari", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onScan) { Icon(Icons.Default.Refresh, null, tint = Color(0xFF2196F3)) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF44336)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    packages: List<GymPackage>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableDoubleStateOf(0.0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedPackageName by remember { mutableStateOf("Pilih Paket (Opsional)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Member Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, shape = RoundedCornerShape(12.dp))
                
                // Dropdown Paket
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackageName,
                        onValueChange = {},
                        label = { Text("Pilih Paket") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - Rp ${String.format("%,.0f", pkg.price)}") },
                                onClick = {
                                    selectedPackageName = pkg.name
                                    days = pkg.durationDays.toString()
                                    selectedPrice = pkg.price
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Masa Aktif (Hari)") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, user, pass, days.toIntOrNull() ?: 0, selectedPrice) },
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan Member", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun EditMemberDialog(member: Member, onDismiss: () -> Unit, onConfirm: (Member) -> Unit) {
    var name by remember { mutableStateOf(member.name) }
    var user by remember { mutableStateOf(member.username) }
    var pass by remember { mutableStateOf(member.password) }
    var days by remember { mutableStateOf(member.remainingDays.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Sisa Hari") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(member.copy(name=name, username=user, password=pass, remainingDays=days.toIntOrNull()?:0)) }) { Text("Update") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagementScreen(packages: MutableList<GymPackage>, onBack: () -> Unit) {
    var packageToEdit by remember { mutableStateOf<GymPackage?>(null) }
    var showAddPackage by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Paket Harga") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPackage = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(packages) { pkg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Harga: Rp ${String.format("%,.0f", pkg.price)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Durasi: ${pkg.durationDays} Hari", fontSize = 14.sp)
                        }
                        IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                        IconButton(onClick = { packages.remove(pkg) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    }
                }
            }
        }
    }

    if (showAddPackage) {
        PackageDialog(title = "Tambah Paket", onDismiss = { showAddPackage = false }, onConfirm = { n, p, d ->
            packages.add(GymPackage(UUID.randomUUID().toString(), n, p, d))
            showAddPackage = false
        })
    }

    if (packageToEdit != null) {
        PackageDialog(
            title = "Edit Paket",
            initialPackage = packageToEdit,
            onDismiss = { packageToEdit = null },
            onConfirm = { n, p, d ->
                val idx = packages.indexOfFirst { it.id == packageToEdit?.id }
                if (idx != -1) {
                    packages[idx] = packageToEdit!!.copy(name = n, price = p, durationDays = d)
                }
                packageToEdit = null
            }
        )
    }
}

@Composable
fun PackageDialog(
    title: String,
    initialPackage: GymPackage? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price?.toString() ?: "") }
    var days by remember { mutableStateOf(initialPackage?.durationDays?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Paket") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Durasi (Hari)") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, days.toIntOrNull() ?: 0) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
