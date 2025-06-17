package com.example.sporttracker
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sporttracker.databinding.ActivityMainBinding
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.ui.NavigationUI

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment // Убедитесь, что ID правильный!

        // 2. Получаем NavController из NavHostFragment
        val navController = navHostFragment.navController


        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.workoutRoutinesFragment,
                R.id.exerciseListFragment,
                R.id.trainingHistoryFragment,
                R.id.foodSupplementsFragment
            ),
            binding.drawerLayout
        )
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfig)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }




}
