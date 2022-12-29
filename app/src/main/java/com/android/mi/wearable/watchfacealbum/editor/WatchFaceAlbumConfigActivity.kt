package com.android.mi.wearable.watchfacealbum.editor

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.android.mi.wearable.watchfacealbum.data.watchface.StyleIdAndResourceIds
import com.android.mi.wearable.watchface5.databinding.ActivityWatchFaceConfig5Binding
import com.android.mi.wearable.watchfacealbum.utils.BitmapTranslateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
interface IComplicationClick {
    fun onStylePagerChange(isUp: Boolean)
    fun onColorPagerChange(isUp: Boolean)
}
class WatchFace5ConfigActivity : ComponentActivity(), IComplicationClick {
    private lateinit var binding: ActivityWatchFaceConfig5Binding
    private lateinit var currentColorId: String
    private lateinit var currentStyleId: String
    private var currentStylePosition = 0
    private var currentColorPosition = 0
    private var isFirst: Boolean = true
    var myAdapter: HorizontalPagerAdapter? = null
    var isSelected: Boolean = false
    private val stateHolder: WatchFaceConfigStateHolder by lazy {
    WatchFaceConfigStateHolder(
            lifecycleScope,
            this@WatchFace5ConfigActivity
        )
    }

    override fun onStylePagerChange(isUp: Boolean) {
        TODO("Not yet implemented")
    }


    override fun onColorPagerChange(isUp: Boolean){
        val colorStyleIdAndResourceIdsList = enumValues<StyleIdAndResourceIds>()
        currentColorPosition = if (isUp){
            if (currentColorPosition == 2) 2 else (currentColorPosition + 1)
        }else{
            if (currentColorPosition == 0) 0 else (currentColorPosition - 1)
        }
        val newColorStyle: StyleIdAndResourceIds = colorStyleIdAndResourceIdsList[currentColorPosition]
        stateHolder.setColorStyle(newColorStyle.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchFaceConfig5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            stateHolder.uiState
                .collect { uiState: WatchFaceConfigStateHolder.EditWatchFaceUiState ->
                    when (uiState) {
                        is WatchFaceConfigStateHolder.EditWatchFaceUiState.Loading -> {
                            Log.d("TAG", "StateFlow Loading: ${uiState.message}")
                        }
                        is WatchFaceConfigStateHolder.EditWatchFaceUiState.Success -> {
                            Log.d("TAG", "StateFlow Success.")
                            updateWatchFacePreview(uiState.userStylesAndPreview)
                        }
                        is WatchFaceConfigStateHolder.EditWatchFaceUiState.Error -> {
                            Log.e("TAG", "Flow error: ${uiState.exception}")
                        }
                    }
                }
        }
    }

    private fun initHorizontalViewPager() {
        myAdapter = HorizontalPagerAdapter(listener = this, context = this)
        binding.pager.apply {
            adapter = myAdapter
        }
    }

    fun onConfirmClick(view: View){
        isSelected = true
        onBackPressedDispatcher.onBackPressed()
    }

    private fun updateWatchFacePreview(
        userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview
    ) {
        binding.preview.watchFaceBackground.setImageBitmap(userStylesAndPreview.previewImage)
        if (isFirst){
            isFirst = false
            currentColorId = userStylesAndPreview.colorStyleId
            currentColorPosition = BitmapTranslateUtils.currentColorItemPosition(currentColorId)
            initHorizontalViewPager()
        }
    }

    override fun onPause() {
        if (!isSelected){
            stateHolder.setColorStyle(currentColorId)
            stateHolder.setShapeStyle(currentStyleId)
        }
        super.onPause()
    }
}