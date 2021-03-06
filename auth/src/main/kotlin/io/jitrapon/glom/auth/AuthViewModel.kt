package io.jitrapon.glom.auth

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.jitrapon.glom.auth.oauth.BaseOauthInteractor
import io.jitrapon.glom.base.NAVIGATE_TO_MAIN
import io.jitrapon.glom.base.model.*
import io.jitrapon.glom.base.util.AppLogger
import io.jitrapon.glom.base.viewmodel.BaseViewModel
import io.jitrapon.glom.base.viewmodel.run
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Jitrapon
 */
class AuthViewModel : BaseViewModel() {

    /* observable background URL */
    private val observableBackground = MutableLiveData<String?>()

    /* observable representation of the authentication state */
    private val observableAuth = MutableLiveData<AuthUiModel>()
    private val authUiModel: AuthUiModel = AuthUiModel(false)

    /* observable sign-in hints in case of email address */
    private val observableCredentialPicker = LiveEvent<CredentialPickerUiModel>()

    /* observable smart lock auto-login request */
    private val observableCredentialRequest = LiveEvent<CredentialRequestUiModel>()

    /* observable smart lock save request */
    private val observableCredentialSave = LiveEvent<CredentialSaveUiModel>()

    @Inject
    lateinit var authInteractor: AuthInteractor

    @field:[Inject Named("facebook")]
    lateinit var facebookInteractor: BaseOauthInteractor

    @field:[Inject Named("google")]
    lateinit var googleInteractor: BaseOauthInteractor

    @field:[Inject Named("line")]
    lateinit var lineInteractor: BaseOauthInteractor

    private var currentOauthType: AccountType? = null

    init {
        AuthInjector.getComponent().inject(this)

        observableBackground.value = "https://image.ibb.co/jxh4Xq/busy-30.jpg"

        requestCredential()
    }

    fun continueWithEmail(email: CharArray? = null, password: CharArray? = null, isSignIn: Boolean) {
        if (!authUiModel.showEmailExpandableLayout) {
            // expand menu and show credential hints
            arrayOf({
                observableAuth.value = authUiModel.apply { showEmailExpandableLayout = true }
            }, {
                observableCredentialPicker.value = CredentialPickerUiModel(true, true)
            }).run(500L)
        }
        else {
            val emailIsEmpty = email == null || email.isEmpty()
            val passwordIsEmpty = password == null || password.isEmpty()
            if (!emailIsEmpty && !passwordIsEmpty) {
                observableAuth.value = authUiModel.apply {
                    status = UiModel.Status.POSITIVE
                    emailError = null
                    passwordError = null
                }

                if (isSignIn) signInWithPassword(email!!, password!!)
                else signUpWithPassword(email!!, password!!)
            }
            else {
                observableAuth.value = authUiModel.apply {
                    status = UiModel.Status.EMPTY
                    emailError = if (emailIsEmpty) AndroidString(R.string.auth_email_empty_error) else null
                    passwordError = if (passwordIsEmpty) AndroidString(R.string.auth_password_empty_error) else null
                }
            }
        }
    }

    fun signInWithPassword(email: CharArray, password: CharArray) {
        observableViewAction.value = Loading(true)

        authInteractor.signInWithEmailPassword(email, password) {
            when (it) {
                is AsyncSuccessResult -> {
                    observableViewAction.value = Loading(false)

                    observableCredentialSave.value = CredentialSaveUiModel(email, password, AccountType.PASSWORD)

                    Arrays.fill(email, ' ')
                    Arrays.fill(password, ' ')
                }
                is AsyncErrorResult -> {
                    handleError(it.error, true)

                    // if the cause of the error is a HttpException, delete the stored credentials
                    if (it.error.cause is HttpException) {
                        observableCredentialSave.value = CredentialSaveUiModel(email, password, AccountType.PASSWORD, shouldDelete = true)
                    }

                    Arrays.fill(email, ' ')
                    Arrays.fill(password, ' ')
                }
            }
        }
    }

    private fun signUpWithPassword(email: CharArray, password: CharArray) {
        observableViewAction.value = Loading(true)

        authInteractor.signUpWithEmailPassword(email, password) {
            when (it) {
                is AsyncSuccessResult -> {
                    observableViewAction.value = Loading(false)

                    observableCredentialSave.value = CredentialSaveUiModel(email, password, AccountType.PASSWORD)

                    Arrays.fill(email, ' ')
                    Arrays.fill(password, ' ')
                }
                is AsyncErrorResult -> {
                    handleError(it.error, true)

                    // if the cause of the error is a HttpException, delete the stored credentials
                    if (it.error.cause is HttpException) {
                        observableCredentialSave.value = CredentialSaveUiModel(email, password, AccountType.PASSWORD, shouldDelete = true)
                    }

                    Arrays.fill(email, ' ')
                    Arrays.fill(password, ' ')
                }
            }
        }
    }

    fun onSaveCredentialCompleted() {
        observableViewAction.value = Navigation(NAVIGATE_TO_MAIN)
    }

    private fun requestCredential() {
        observableCredentialRequest.value = CredentialRequestUiModel(true)
    }

    fun continueWithFacebook(authView: AuthView) {
        currentOauthType = AccountType.FACEBOOK

        facebookInteractor.acquireToken(authView) {
            when (it) {
                is AsyncSuccessResult -> handleOauthSuccess(it.result)
                is AsyncErrorResult -> handleOauthError(it.error)
            }
        }
    }

    fun continueWithGoogle(authView: AuthView) {
        currentOauthType = AccountType.GOOGLE

        googleInteractor.acquireToken(authView) {
            when (it) {
                is AsyncSuccessResult -> handleOauthSuccess(it.result)
                is AsyncErrorResult -> handleOauthError(it.error)
            }
        }
    }

    fun continueWithLine(authView: AuthView) {
        currentOauthType = AccountType.LINE

        lineInteractor.acquireToken(authView) {
            when (it) {
                is AsyncSuccessResult -> handleOauthSuccess(it.result)
                is AsyncErrorResult -> handleOauthError(it.error)
            }
        }
    }

    fun processOauthResult(authView: AuthView, requestCode: Int, resultCode: Int, data: Parcelable?) {
        val interactor = when (currentOauthType) {
            AccountType.FACEBOOK -> facebookInteractor
            AccountType.GOOGLE -> googleInteractor
            AccountType.LINE -> lineInteractor
            else -> null
        }
        interactor?.processOauthResult(authView, requestCode, resultCode, data)
    }

    private fun handleOauthSuccess(oAuthToken: String) {
        observableViewAction.value = Loading(true)

        currentOauthType ?: return

        authInteractor.signInWithOAuthCredential(oAuthToken, currentOauthType!!) { result ->
            when (result) {
                is AsyncSuccessResult -> {
                    observableViewAction.value = Loading(false)

                    val name = result.result.displayName

                    // https://stackoverflow.com/questions/34414532/store-facebook-credential-in-android-for-google-smart-lock-password
                    // https://github.com/googlesamples/android-credentials/issues/6#issuecomment-168844689
                    observableCredentialSave.value = CredentialSaveUiModel(
                        result.result.email?.toCharArray(), null,
                        currentOauthType!!, name)
                }
                is AsyncErrorResult -> handleError(result.error, true)
            }
        }
    }

    private fun handleOauthError(ex: Throwable) {
        val oAuthProvider = when (currentOauthType) {
            AccountType.FACEBOOK -> "Facebook"
            AccountType.GOOGLE -> "Google"
            AccountType.LINE -> "LINE"
            else -> "unknown"
        }
        when (ex) {
            is BaseOauthInteractor.OperationCanceledException ->
                observableViewAction.value = Toast(AndroidString(R.string.auth_oauth_canceled, arrayOf(oAuthProvider)))
            is BaseOauthInteractor.NoAccessTokenException,
            is BaseOauthInteractor.OperationFailedException -> {
                observableViewAction.value = Toast(AndroidString(R.string.auth_oauth_failure, arrayOf(oAuthProvider)))

                AppLogger.e(ex)
            }
        }
    }

    override fun onCleared() {
        AuthInjector.clear()
    }

    //region observable getters

    fun getObservableBackground(): LiveData<String?> = observableBackground

    fun getObservableAuth(): LiveData<AuthUiModel> = observableAuth

    fun getCredentialPickerUiModel(): LiveData<CredentialPickerUiModel> = observableCredentialPicker

    fun getCredentialRequestUiModel(): LiveData<CredentialRequestUiModel> = observableCredentialRequest

    fun getCredentialSaveUiModel(): LiveData<CredentialSaveUiModel> = observableCredentialSave

    //endregion
}
