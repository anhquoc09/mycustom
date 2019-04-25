package com.example.anhquoc.mycustom

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Profile(val name : String = "abc", val email: String = "example@gmail.com") : Parcelable