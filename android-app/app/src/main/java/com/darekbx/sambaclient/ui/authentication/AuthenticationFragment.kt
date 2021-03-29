package com.darekbx.sambaclient.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.darekbx.sambaclient.BuildConfig
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentAuthenticationBinding
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.ui.viewmodel.ResultWrapper
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import com.google.android.material.textfield.TextInputLayout
import com.hierynomus.mssmb2.SMBApiException
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.lang.Exception

/**
 * TODO
 *  - automatic logic, when remember me is chcked
 *  - check if active wifi is connected to home wifi (from settings) if yes then use sambe else heroku remote
 */
class AuthenticationFragment : Fragment(R.layout.fragment_authentication) {

    companion object {
        private const val ANIMATION_DURATION = 500L
        private const val AUTO_LOGIN_ENABLED = true
    }

    private val sambaViewModel: SambaViewModel by sharedViewModel()
    private val authPreferences: AuthPreferences by inject()

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!

    private var authenticated: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authenticated = false
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(sambaViewModel)
        with(sambaViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(authenticationResult) { handleAuthenticationResult(it) }
            observeOnViewLifecycle(diskShareResult) { handleShareNameResult(it) }
            observeOnViewLifecycle(autoAuthenticationResult) { handleAutoAuthenticationResult(it) }
        }

        binding.authenticateButton.setOnClickListener {
            when (authenticated) {
                false -> authenticate()
                true -> useShareName()
            }
        }

        if (AUTO_LOGIN_ENABLED) {
            val credentials = authPreferences.read()
            val shareName = authPreferences.readShareName()
            if (credentials.arePersisted && shareName != null) {
                with(credentials) {
                    sambaViewModel.authenticate(address!!, user, password, shareName)
                }
            }
        } else {
            fillRememberedCredentials()
        }
    }

    private fun authenticate() {
        binding.authStatusView.visibility = View.INVISIBLE

        if (!verifyField(binding.authenticationAddress, R.string.validation_address)) return
        val server = binding.authenticationAddress.getSimpleText()
        val user = binding.authenticationUser.nullIfEmpty()
        val password = binding.authenticationPassword.nullIfEmpty()
        sambaViewModel.authenticate(server!!, user, password)
    }

    private fun useShareName() {
        binding.authStatusView.visibility = View.INVISIBLE
        binding.shareNameStatusView.visibility = View.INVISIBLE
        if (!verifyField(binding.authenticationShareName, R.string.validation_share_name)) return
        val shareName = binding.authenticationShareName.getSimpleText()
        sambaViewModel.connectToDiskShare(shareName!!)
    }

    private fun handleShareNameResult(result: ResultWrapper<Boolean>) {
        if (result.hasError) {
            displaySetShareNameError(result.exception!!)
        } else {
            rememberShareName()
            findNavController().navigate(R.id.action_authenticationFragment_to_fileExplorerFragment)
        }
    }

    private fun handleAutoAuthenticationResult(result: ResultWrapper<Boolean>) {
        if (result.hasError) {
            displayAuthenticateError(result.exception!!)
        } else {
            findNavController().navigate(R.id.action_authenticationFragment_to_fileExplorerFragment)
        }
    }

    private fun handleAuthenticationResult(result: ResultWrapper<Boolean>) {
        if (result.hasError) {
            displayAuthenticateError(result.exception!!)
        } else {
            authenticated = true
            binding.shareNameStatusView.visibility = View.VISIBLE
            binding.shareNameStatusView.setText(R.string.authentication_success)

            fillRememberedShareName()
            animateDiskShare()
            rememberAddressAndUser()
        }
    }

    private fun rememberAddressAndUser() {
        with(binding) {
            if (rememberCheckBox.isChecked) {
                val address = authenticationAddress.getSimpleText()
                val user = authenticationUser.getSimpleText()
                val password = authenticationPassword.getSimpleText()
                authPreferences.persist(address, user, password)
            } else {
                authPreferences.clearAddressAndUser()
            }
        }
    }

    private fun rememberShareName() {
        with(binding) {
            if (rememberShareCheckBox.isChecked) {
                val shareName = authenticationShareName.getSimpleText()
                authPreferences.persist(shareName)
            } else {
                authPreferences.clearShareName()
            }
        }
    }

    private fun fillRememberedCredentials() {
        val credentials = authPreferences.read()
        if (credentials.arePersisted) {
            with(binding) {
                authenticationAddress.editText?.setText(credentials.address)
                authenticationUser.editText?.setText(credentials.user)
                authenticationPassword.editText?.setText(credentials.password)
                rememberCheckBox.isChecked = true
            }
        }
    }

    private fun fillRememberedShareName() {
        val shareName = authPreferences.readShareName()
        if (shareName != null && shareName.isNotBlank()) {
            with(binding) {
                authenticationShareName.editText?.setText(shareName)
                rememberShareCheckBox.postDelayed(
                    { rememberShareCheckBox.isChecked = true },
                    /* Wait till auth animation is completed, if value is set before, then checkbox is not being checked */
                    ANIMATION_DURATION * 2
                )
            }
        }
    }

    private fun displayAuthenticateError(exception: Exception) {
        var message = exception::class.simpleName
        if (exception is SMBApiException) {
            message = exception.status.name
        }

        binding.authStatusView.visibility = View.VISIBLE
        binding.authStatusView.text = message
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }
    }

    private fun displaySetShareNameError(exception: Exception) {
        binding.shareNameStatusView.setCompoundDrawables(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_error,
                requireActivity().theme
            ), null, null, null
        )
        binding.shareNameStatusView.visibility = View.VISIBLE
        binding.shareNameStatusView.text = exception::class.simpleName
    }

    private fun animateDiskShare() {
        val set = ConstraintSet()
        set.clone(binding.root)

        moveViewToRight(set, R.id.authentication_address)
        moveViewToRight(set, R.id.authentication_user)
        moveViewToRight(set, R.id.authentication_password)
        moveViewToRight(set, R.id.remember_check_box)

        set.connect(
            R.id.authentication_share_name,
            ConstraintSet.START,
            R.id.size_keeper,
            ConstraintSet.START
        )
        set.connect(
            R.id.authentication_share_name,
            ConstraintSet.END,
            R.id.size_keeper,
            ConstraintSet.END
        )

        binding.authenticateButton.setText(R.string.button_use_share)

        TransitionManager.beginDelayedTransition(
            binding.root,
            AutoTransition().apply { duration = ANIMATION_DURATION })
        set.applyTo(binding.root)
    }

    private fun moveViewToRight(set: ConstraintSet, viewId: Int) {
        set.clear(viewId, ConstraintSet.START)
        set.clear(viewId, ConstraintSet.END)
        set.connect(
            viewId,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
    }

    private fun TextInputLayout.nullIfEmpty(): String? =
        getSimpleText().run { if (isNullOrEmpty()) null else this }

    private fun TextInputLayout.getSimpleText(): String? = editText?.text?.toString()

    private fun verifyField(field: TextInputLayout, errorMessageId: Int): Boolean {
        val isFilled = field.editText?.text?.isNotBlank() ?: false
        if (!isFilled) {
            field.error = getString(errorMessageId)
            return false
        }
        field.error = null
        return true
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}
