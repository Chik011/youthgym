package com.chiko0085.testgym

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import com.chiko0085.testgym.ui.theme.TestGymTheme
import com.google.gson.Gson
import androidx.compose.ui.platform.LocalContext
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestGymTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("YouthGymPrefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    var currentScreen by remember { mutableStateOf("login") }
    var loggedInMemberId by remember { mutableStateOf<String?>(null) }
    
    // Load Total Revenue
    var totalRevenue by remember { 
        mutableDoubleStateOf(sharedPrefs.getFloat("total_revenue", 0f).toDouble()) 
    }

    val memberList = remember {
        val savedData = sharedPrefs.getString("member_list", null)
        val list = if (savedData != null) {
            val type = object : TypeToken<MutableList<Member>>() {}.type
            gson.fromJson<MutableList<Member>>(savedData, type)
        } else {
            mutableListOf(
                Member("CH-001", "Chiko", "chiko", "chiko123", 30),
                Member("TS-002", "Test", "test", "test123", 10)
            )
        }
        mutableStateListOf<Member>().apply { addAll(list) }
    }

    val gymPackages = remember {
        val savedData = sharedPrefs.getString("gym_packages", null)
        val list = if (savedData != null) {
            val type = object : TypeToken<MutableList<GymPackage>>() {}.type
            gson.fromJson<MutableList<GymPackage>>(savedData, type)
        } else {
            mutableListOf(
                GymPackage("1", "Paket Hemat", 100000.0, 30),
                GymPackage("2", "Paket Pro", 250000.0, 90)
            )
        }
        mutableStateListOf<GymPackage>().apply { addAll(list) }
    }

    // Auto-Save
    LaunchedEffect(memberList.toList(), gymPackages.toList(), totalRevenue) {
        sharedPrefs.edit().apply {
            putString("member_list", gson.toJson(memberList.toList()))
            putString("gym_packages", gson.toJson(gymPackages.toList()))
            putFloat("total_revenue", totalRevenue.toFloat())
            apply()
        }
    }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { role, member ->
                if (role == "admin") {
                    currentScreen = "admin_dashboard"
                } else if (member != null) {
                    loggedInMemberId = member.id
                    currentScreen = "member_main"
                }
            },
            memberList = memberList
        )
        "admin_dashboard" -> AdminDashboard(
            members = memberList,
            gymPackages = gymPackages,
            totalRevenue = totalRevenue,
            onUpdateRevenue = { totalRevenue = it },
            onLogout = { currentScreen = "login" }
        )
        "member_main" -> {
            val member = memberList.find { it.id == loggedInMemberId }
            if (member != null) {
                MemberMainScreen(
                    initialMember = member,
                    memberList = memberList,
                    onLogout = {
                        loggedInMemberId = null
                        currentScreen = "login"
                    }
                )
            } else {
                currentScreen = "login"
            }
        }
    }
}
