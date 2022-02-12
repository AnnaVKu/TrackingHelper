package com.example.core.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.core.repository.AuthenticationRepository
import com.example.core.repository.GetDataRepository
import com.example.core.db.Response
import com.example.core.ResponseWrapper
import com.example.core.utils.UtilsResponse
import com.example.core.utils.UtilsVerificationAuthInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MyViewModel @Inject constructor(
    private val repoAuth: AuthenticationRepository,
    private val repoGetData: GetDataRepository
) : ViewModel() {
    // to perform transitions between fragments

    private val transitionLiveDate = MutableLiveData<Int>()

    // for signIn fragment

    private val disposables1 = CompositeDisposable()

    private val liveDataIsFieldsCorrect = MutableLiveData<Boolean>()
    private val liveDataIsNotValidEmail = MutableLiveData<Boolean>()
    private val liveDataIsNotValidPassword = MutableLiveData<Boolean>()

    private val liveDataIsPasswordSend = MutableLiveData<ResponseWrapper<Boolean>>()

    private var isValidEmail: Boolean = false
    private var isValidPassword: Boolean = false

    private val liveDataIsLogin = MutableLiveData<ResponseWrapper<Boolean>>()

    // for signUp fragment

    private val disposables2 = CompositeDisposable()

    private val liveDataIsNotValidName = MutableLiveData<Boolean>()

    private val liveDataIsCreateNewUser = MutableLiveData<ResponseWrapper<Boolean>>()

    private var isValidName: Boolean = false

    // for maps fragment

    private val disposables3 = CompositeDisposable()

    private val liveDataIsSignOut = MutableLiveData<ResponseWrapper<Boolean>>()

    private var responses: MutableList<Response> = ArrayList()

    private val liveDataResponses = MutableLiveData<MutableList<Response>>()
    private lateinit var  selectedDay: Date
    private val liveDataIsSelectedDay = MutableLiveData<Boolean>()

    init {
        liveDataIsSelectedDay.value = false
    }

    // to perform transitions between fragments

    companion object {
        const val base = 0
        const val maps: Int = 1
        const val signIn: Int = 2
        const val signUp: Int = 3
        const val fromMapsToSignIn: Int = 4
    }

    fun getTransitionLiveData(): LiveData<Int> {
        return transitionLiveDate
    }

    fun base() {
        transitionLiveDate.value = base
    }

    fun replaceMapsFragment() {
        transitionLiveDate.value = maps
    }

    fun backToSignInFragment() {
        transitionLiveDate.value = signIn
    }

    fun replaceSignUpFragment() {
        transitionLiveDate.value = signUp
    }

    fun returnFromMapsFragment() {
        transitionLiveDate.value = fromMapsToSignIn
    }

    fun setBase() {
        transitionLiveDate.value = base
    }

    // for signIn fragment

    fun getLiveDataIsNotValidEmail(): LiveData<Boolean> {
        return liveDataIsNotValidEmail
    }

    fun getLiveDataIsNotValidPassword(): LiveData<Boolean> {
        return liveDataIsNotValidPassword
    }

    fun getLiveDataIsLogin(): LiveData<ResponseWrapper<Boolean>> {
        return liveDataIsLogin
    }

    fun getLiveDataIsPasswordSend(): LiveData<ResponseWrapper<Boolean>> {
        return liveDataIsPasswordSend
    }

    fun signInWasClicked(email: String, password: String) {
        isValidEmail = UtilsVerificationAuthInfo.emailIsValid(email)
        isValidPassword =
            UtilsVerificationAuthInfo.passwordsLengthNotLessSixSymbols(password)

        liveDataIsNotValidEmail.value = !isValidEmail
        liveDataIsNotValidPassword.value = !isValidPassword

        liveDataIsFieldsCorrect.value = isValidEmail && isValidPassword

        if (liveDataIsFieldsCorrect.value == true) {
            val disposable = repoAuth.signIn(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveDataIsLogin.value = ResponseWrapper.success(true)
                    Log.i("Result", "liveDataIsLogin.value = true")
                }, {
                    liveDataIsLogin.value = ResponseWrapper.error(msg = it.message, data = false)
                })
            disposables1.add(disposable)
        }
    }

    fun passwordForgotClicked(email: String) {

        isValidEmail = UtilsVerificationAuthInfo.emailIsValid(email)

        if (isValidEmail) {
            val disposable = repoAuth.sendThePasswordOnTheEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveDataIsPasswordSend.value = ResponseWrapper.success(true)
                }, {
                    liveDataIsPasswordSend.value =
                        ResponseWrapper.error(msg = it.message, data = false)
                })
            disposables1.add(disposable)
        } else {
            liveDataIsPasswordSend.value =
                ResponseWrapper.error(msg = "email is not correct", data = false)
        }
    }

    fun clearDataForSignInFragment() {
        Log.i("Result", "clearDataForSignInFragment()")
        liveDataIsLogin.value = ResponseWrapper.inactive()
        liveDataIsPasswordSend.value = ResponseWrapper.inactive()
        isValidEmail = false
        isValidPassword = false
        liveDataIsFieldsCorrect.value = false
        transitionLiveDate.value = base
    }

    // for signUp fragment

    fun getLiveDataIsNotValidName(): LiveData<Boolean> {
        return liveDataIsNotValidName
    }

    fun getLiveDataIsCreateNewUser(): LiveData<ResponseWrapper<Boolean>> {
        return liveDataIsCreateNewUser
    }

    fun buttonSignUpWasClicked(name: String, email: String, password: String) {

        isValidName = UtilsVerificationAuthInfo.nameIsNotEmpty(name)
        isValidEmail = UtilsVerificationAuthInfo.emailIsValid(email)
        isValidPassword =
            UtilsVerificationAuthInfo.passwordsLengthNotLessSixSymbols(password)

        liveDataIsNotValidName.value = !isValidName
        liveDataIsNotValidEmail.value = !isValidEmail
        liveDataIsNotValidPassword.value = !isValidPassword

        liveDataIsFieldsCorrect.value = isValidEmail && isValidPassword
        if (liveDataIsFieldsCorrect.value == true) {
            val disposable = repoAuth.createNewUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveDataIsCreateNewUser.value = ResponseWrapper.success(true)
                }, {
                    liveDataIsCreateNewUser.value = ResponseWrapper.error(it.message, false)
                })
            disposables2.add(disposable)
        }
    }

    fun clearDataForSignUpFragment() {
        Log.i("Result", "clearDataForSignInFragment()")
        liveDataIsCreateNewUser.value = ResponseWrapper.inactive()
        isValidName = false
        isValidEmail = false
        isValidPassword = false
        liveDataIsFieldsCorrect.value = false
        transitionLiveDate.value = base
    }

    // for mapsFragment

    fun getLiveDataIsSignOut(): LiveData<ResponseWrapper<Boolean>> {
        return liveDataIsSignOut
    }

    fun signOutWasClicked() {
        //disposables1.dispose()
        val disposable = repoAuth.signOut()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                liveDataIsSignOut.value = ResponseWrapper.success(true)
            }
        disposables3.add(disposable)
    }

    fun clearDataForMapsFragmentTrackingApp() {
        Log.i("Result", "clearDataForMapsFragment()")
        liveDataIsSignOut.value = ResponseWrapper.inactive()
        transitionLiveDate.value = base
        liveDataResponses.value?.clear()
        liveDataIsSelectedDay.value = false
    }

    fun clearDataForMapsFragmentShowApp() {
        Log.i("Result", "clearDataForMapsFragment()")
        repoGetData.removeEventListener()
        liveDataIsSignOut.value = ResponseWrapper.inactive()
        transitionLiveDate.value = base
        liveDataResponses.value?.clear()
        liveDataIsSelectedDay.value = false
    }

    fun loadResponses() {
        responses.clear()
        val disposable = repoGetData.getDataFromFirestore()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                liveDataResponses.value = it
                if(responses.isEmpty()) responses = it
                Log.i("Result", "loadResponses()")
            }
        disposables3.add(disposable)
    }

    fun hasResponses(): Boolean = responses.isNotEmpty()

    fun getLiveDataResponses(): LiveData<MutableList<Response>> {
        return liveDataResponses
    }

    fun selectingResponses(
        selectedDate: Date
    ): MutableList<Response> = UtilsResponse.selectLocationsAccordingToTheCurrentDate(
        date = selectedDate,
        responses = responses
    )


    fun selectingResponses(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): MutableList<Response> = UtilsResponse.selectLocationsAccordingToTheCurrentDate(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
        responses = responses
    )

    fun selectedDateWasChanged(date: Date) {
        selectedDay = date
        liveDataIsSelectedDay.value = true
        Log.i("Result", "selectedDateWasChanged")
    }

    fun selectedDateWasNotChanged() {
        liveDataIsSelectedDay.value = false

        Log.i("Result", "selectedDateWasNotChanged()")
    }

    fun getLiveDataIsSelectedDay(): LiveData<Boolean> {
        return liveDataIsSelectedDay
    }

    fun getIsSelectedDay(): Boolean? {
        return liveDataIsSelectedDay.value
    }

    fun getSelectedDay(): Date {
        return selectedDay
    }

    public override fun onCleared() {
        super.onCleared()
        disposables1.dispose()
        disposables2.dispose()
        disposables3.dispose()
        Log.i("Result", "onCleared()")
    }
}