package org.greenstand.android.TreeTracker.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenstand.android.TreeTracker.BuildConfig
import org.greenstand.android.TreeTracker.R
import org.greenstand.android.TreeTracker.models.FeatureFlags
import org.greenstand.android.TreeTracker.models.Planter
import org.greenstand.android.TreeTracker.preferences.PreferencesMigrator
import org.koin.android.ext.android.inject
import timber.log.Timber

class SplashFragment : Fragment() {

    private val user: Planter by inject()
    private val preferencesMigrator: PreferencesMigrator by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.tag("BuildVariant").d("build variant: ${BuildConfig.BUILD_TYPE}")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            whenStarted {

                preferencesMigrator.migrateIfNeeded()

                delay(1000)

                when {
                    user.isLoggedIn ->
                        findNavController()
                            .navigate(SplashFragmentDirections
                                .actionSplashFragment2ToMapsFragment())

                    FeatureFlags.ORG_LINK_ENABLED ->
                        findNavController()
                            .navigate(SplashFragmentDirections
                                .actionSplashFragment2ToOrgWallFragment())

                    else -> findNavController()
                        .navigate(SplashFragmentDirections.actionSplashFragment2ToLoginFlowGraph())
                }
            }
        }
    }
}
