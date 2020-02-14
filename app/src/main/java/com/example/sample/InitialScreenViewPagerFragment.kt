/*
 * Copyright © 2019 Rosetta Stone. All rights reserved.
 */
package com.example.sample

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Scene
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_initial_screen_layout.*
import kotlinx.android.synthetic.main.scene_a.*

class InitialScreenViewPagerFragment : Fragment() {

    private val position: Int by lazy { arguments?.getInt(POSITION_KEY) ?: DEFAULT_POSITION }

    private var transition = false

    companion object {
        private const val POSITION_KEY = "position_key"
        private const val DEFAULT_POSITION = 0
        fun newInstance(position: Int): InitialScreenViewPagerFragment =
            InitialScreenViewPagerFragment().apply {
                arguments?.putInt(POSITION_KEY, position)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_initial_screen_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gauge.setMaximumProgress(100f)
        gauge.setCurrentProgress(65f)
        //placeholderEditText.postDelayed({ placeholderEditText.setNumberOfChars(11) }, 2000)

        initialScreenFragmentImage.setOnClickListener {
            rootEnd.removeView(initialScreenFragmentImage)
            TransitionManager.beginDelayedTransition(rootEnd)
        }
        var scene: Scene
        transitionButton.setOnClickListener {
            scene = if (transition) Scene.getSceneForLayout(scene_root, R.layout.scene_a, context!!) else Scene.getSceneForLayout(
                scene_root,
                R.layout.scene_b,
                context!!
            )
            TransitionManager.go(scene, AutoTransition().apply { duration = 1000 })
            transition = !transition
        }
    }
}