package com.crepetete.steamachievements.presentation.fragment

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 17 Jun, 2020; 18:24.
 */
abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    abstract fun getFragmentName(): String
}