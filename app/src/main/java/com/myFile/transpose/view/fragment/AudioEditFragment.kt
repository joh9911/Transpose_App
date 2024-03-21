package com.myFile.transpose.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.SessionCommand
import androidx.navigation.fragment.findNavController

import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.myFile.transpose.R
import com.myFile.transpose.databinding.FragmentAudioEditBinding

import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.viewModel.AudioEditViewModel
import com.myFile.transpose.viewModel.AudioEditViewModelFactory

import com.myFile.transpose.viewModel.SharedViewModel
import kotlin.math.pow

class AudioEditFragment: Fragment() {
    var fbinding: FragmentAudioEditBinding? = null
    val binding get() = fbinding!!
    private lateinit var activity: Activity

    private var selectedEqualizerTextView: TextView? = null

    private var selectedPresetReverbTextView: TextView? = null

    private lateinit var receiver: BroadcastReceiver

    private lateinit var viewModel: AudioEditViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var equalizerTextViewList: MutableList<TextView>
    private lateinit var presetReverbTextViewList: MutableList<TextView>




    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentAudioEditBinding.inflate(inflater, container, false)
        initViewModel()
        initPitchAdjustFunction()
        initTempoAdjustFunction()
        initEqualizerChart()
        initEqualizerScrollViw()
        initEqualizerEvent()
        initBassBoostFunction()
        initLoudnessEnhancerFunction()
        initVirtualizerFunction()
        initPresetReverbFunction()
        initEnvironmentReverbFunction()
        return binding.root
    }

    private fun initViewModel(){
        val viewModelFactory = AudioEditViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[AudioEditViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
        initAudioEffectValue()
        initObserver()
        binding.wholeScrollView.viewTreeObserver.addOnScrollChangedListener {

            sharedViewModel.audioEditFragmentScrollValue = binding.wholeScrollView.scrollY
        }

        binding.wholeScrollView.post {

            binding.wholeScrollView.scrollTo(0, sharedViewModel.audioEditFragmentScrollValue)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("플레이리스트 추가 ","onResume")
    }

    private fun initAudioEffectValue(){
        binding.pitchSeekBar.progress = sharedViewModel.pitchValue.value ?: 100
        binding.tempoSeekBar.progress = sharedViewModel.tempoValue.value ?: 100
        binding.bassSeekBar.progress = sharedViewModel.bassBoostValue.value ?: 0
        binding.loudnessSeekBar.progress = sharedViewModel.loudnessEnhancerValue.value ?: 0
        binding.virtualizerSeekBar.progress = sharedViewModel.virtualizerValue.value ?: 0
        binding.equalizerSwitch.isChecked = sharedViewModel.isEqualizerEnabled.value ?: false
        binding.presetReverbLevelSeekBar.progress = sharedViewModel.presetReverbSendLevel.value ?: 0
        binding.presetReverbSwitch.isChecked = sharedViewModel.isPresetReverbEnabled.value ?: false
    }

    private fun initObserver(){
        sharedViewModel.pitchValue.observe(viewLifecycleOwner){
            binding.pitchSeekBar.progress = it
            setPitch(it)
        }
        sharedViewModel.tempoValue.observe(viewLifecycleOwner){
            binding.tempoSeekBar.progress = it
            setTempo(it)
        }

        sharedViewModel.isEqualizerEnabled.observe(viewLifecycleOwner){ isEnabled ->
            binding.equalizerSwitch.isChecked = isEnabled
            if (isEnabled){
                Log.d("이퀼","$isEnabled")
                setEqualizerVisibility()
                setEqualizer(sharedViewModel.equalizerIndexValue.value ?: 3)
                sharedViewModel.equalizerChartValueList?.let{
                    updateChart(it)
                }
            }
            else{

                setEqualizer(-1)
            }
        }
        sharedViewModel.equalizerIndexValue.observe(viewLifecycleOwner){
            val selectedTextView = equalizerTextViewList[it]
            changeFocusToSelectedEqualizerTextView(selectedTextView)
            val isEnabled = sharedViewModel.isEqualizerEnabled.value ?: false
            if (isEnabled){
                setEqualizer(it)
            }
        }

        sharedViewModel.isPresetReverbEnabled.observe(viewLifecycleOwner){ isEnabled ->
            Log.d("플레이리스트 추가,enable","$isEnabled")
            binding.presetReverbSwitch.isChecked = isEnabled
            if (isEnabled) {
                Log.d("플레이리스트 추가,enable","조건문 안인데")
                setPresetReverbVisibility()
                setPresetReverb(sharedViewModel.presetReverbIndexValue.value ?: 0, binding.presetReverbLevelSeekBar.progress)
            }else {
                setPresetReverb(-1, -1)
            }
        }
        sharedViewModel.presetReverbIndexValue.observe(viewLifecycleOwner){
            val selectedTextView = presetReverbTextViewList[it]
            changeFocusToSelectedPresetReverbTextView(selectedTextView)
            val isEnabled = sharedViewModel.isPresetReverbEnabled.value ?: false
            if (isEnabled){
                setPresetReverb(it, sharedViewModel.presetReverbSendLevel.value ?: 0)
            }
        }
        sharedViewModel.presetReverbSendLevel.observe(viewLifecycleOwner){
            binding.presetReverbLevelSeekBar.progress = it
            val isEnabled = sharedViewModel.isPresetReverbEnabled.value ?: false
            if (isEnabled){
                setPresetReverb(sharedViewModel.presetReverbIndexValue.value ?: 0, it)
            }

        }

        sharedViewModel.bassBoostValue.observe(viewLifecycleOwner){
            binding.bassSeekBar.progress = it
            setBassBoost(it)
        }

        sharedViewModel.loudnessEnhancerValue.observe(viewLifecycleOwner){
            binding.loudnessSeekBar.progress = it
            setLoudnessEnhancer(it)
        }

        sharedViewModel.virtualizerValue.observe(viewLifecycleOwner){
            binding.virtualizerSeekBar.progress = it
            setVirtualizer(it)
        }

    }



    private fun initPitchAdjustFunction(){
        binding.pitchSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val actualValue = (p1 * 0.1) - 10.0

                binding.pitchValueTextView.text = if (actualValue >= 0) {
                    String.format("+%.1f", actualValue)
                } else {
                    String.format("%.1f", actualValue)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setPitchValue(it.progress)
                }
            }

        })
        binding.pitchInitButton.setOnClickListener {
            sharedViewModel.setPitchValue(100)
        }
    }

    private fun initTempoAdjustFunction(){
        binding.tempoSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val actualValue = (p1 * 0.1) - 10.0

                binding.tempoValueTextView.text = if (actualValue >= 0) {
                    String.format("+%.1f", actualValue)
                } else {
                    String.format("%.1f", actualValue)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setTempoValue(it.progress)
                }

            }

        })
        binding.tempoInitButton.setOnClickListener {
            sharedViewModel.setTempoValue(100)
        }

    }

    private fun setPitch(value: Int){
        val controller = activity.controller ?: return

        val semitonesFromCenter = (value - 100) * 0.1
        val adjustedPitch = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentTempoValue = controller.playbackParameters.speed
        controller.playbackParameters = PlaybackParameters(currentTempoValue, adjustedPitch)
    }

    private fun setTempo(value: Int){
        val controller = activity.controller ?: return

        val semitonesFromCenter = (value - 100) * 0.1
        val adjustedTempo = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentPitchValue = controller.playbackParameters.pitch
        controller.playbackParameters = PlaybackParameters(adjustedTempo, currentPitchValue)
    }


    private fun initBassBoostFunction(){
        binding.bassSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.bassValueTextView.text = if (p1 >= 0) {
                    String.format("+%d", p1)
                } else {
                    String.format("-%d", p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setBassBoostValue(it.progress)
                }

            }
        })
        binding.bassInitButton.setOnClickListener {
            sharedViewModel.setBassBoostValue(0)
        }
    }

    private fun setBassBoost(value: Int){
        Log.d("베이스","보냄")
        val action = Actions.SET_BASS_BOOST
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        activity.controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun initLoudnessEnhancerFunction(){
        binding.loudnessSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.loudnessValueTextView.text = if (p1 >= 0) {
                    String.format("+%d", p1)
                } else {
                    String.format("-%d", p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setLoudnessEnhancerValue(it.progress)
                }
            }

        })
        binding.loudnessInitButton.setOnClickListener {
            sharedViewModel.setLoudnessEnhancerValue(0)
        }
    }

    private fun setLoudnessEnhancer(value: Int){
        Log.d("라우드","보냄")
        val action = Actions.SET_LOUDNESS_ENHANCER
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        activity.controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun initVirtualizerFunction(){
        binding.virtualizerSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.virtualizerValueTextView.text = if (p1 >= 0) {
                    String.format("+%d", p1)
                } else {
                    String.format("-%d", p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setVirtualizerValue(it.progress)
                }
            }

        })
        binding.virtualizerInitButton.setOnClickListener {
            sharedViewModel.setVirtualizerValue(0)
        }
    }

    private fun setVirtualizer(value: Int){
        Log.d("버튜얼","보냄")
        val action = Actions.SET_VIRTUALIZER
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        activity.controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun initPresetReverbSendLevelSeekbar(){
        binding.presetReverbLevelSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.presetReverbSendLevelValueTextView.text = String.format("+%d%%", p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    sharedViewModel.setPresetReverbSendLevel(it.progress)
                }
            }

        })
    }


    private fun initPresetReverbFunction(){
        setPresetReverbVisibility()
        binding.presetReverbLinearLayout.setOnClickListener {
            sharedViewModel.isPresetReverbViewFolded = !sharedViewModel.isPresetReverbViewFolded
            setPresetReverbVisibility()
        }

        binding.presetReverbSendLevelInitButton.setOnClickListener {
            sharedViewModel.setPresetReverbSendLevel(0)
        }
        initPresetReverbSwitch()
        initPresetReverbSendLevelSeekbar()
        initPresetScrollView()
    }

    private fun initPresetReverbSwitch(){
        binding.presetReverbSwitch.setOnClickListener {
            sharedViewModel.setIsPresetReverbEnabled(binding.presetReverbSwitch.isChecked)

        }

    }

    private fun initPresetScrollView(){
        val linearLayout = binding.presetReverbScrollViewLinearLayout
        presetReverbTextViewList = mutableListOf()
        val items = resources.getStringArray(R.array.preset_reverb_labels)

        items.forEachIndexed { index, name ->
            val textView = TextView(requireActivity())
            textView.text = name
            textView.setTextColor(resources.getColor(R.color.description_color))
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 0, 40, 0)
            }
            if (index == (sharedViewModel.presetReverbIndexValue.value ?: 0)){
                changeFocusToSelectedPresetReverbTextView(textView)
            }

            textView.setOnClickListener {
                sharedViewModel.setPresetReverbIndexValue(index)
                changeFocusToSelectedPresetReverbTextView(it)
            }
            presetReverbTextViewList.add(textView)
            linearLayout.addView(textView)
        }
    }
    private fun changeFocusToSelectedPresetReverbTextView(view: View) {
        selectedPresetReverbTextView?.setTextColor(resources.getColor(R.color.description_color))
        selectedPresetReverbTextView?.setTypeface(null, Typeface.NORMAL)

        val clickedTextView = view as TextView
        clickedTextView.setTextColor(resources.getColor(R.color.blue_background))  // 현재 선택된 TextView의 색을 파란색으로 변경
        clickedTextView.setTypeface(null, Typeface.BOLD)
        selectedPresetReverbTextView = clickedTextView

        clickedTextView.post{
            val scrollX = (clickedTextView.left - (binding.presetReverbValueScrollView.width / 2)) + (clickedTextView.width / 2)
            binding.presetReverbValueScrollView.smoothScrollTo(scrollX, 0)
        }
    }

    private fun setPresetReverb(presetReverbValue: Int, sendLevel: Int){

        val action = Actions.SET_REVERB
        val bundle = Bundle().apply {
            putInt("value",presetReverbValue)
            putInt("sendLevel",sendLevel)
        }
        val sessionCommand = SessionCommand(action, bundle)
        activity.controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun setPresetReverbVisibility(){
        if (sharedViewModel.isPresetReverbViewFolded) {
            // 뷰들을 숨김
            binding.presetReverbVisibilitySettingLinearLayout.visibility = View.GONE
            binding.presetReverbWarningTextView.visibility = View.GONE
            binding.presetReverbArrowImageView.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.baseline_keyboard_arrow_down_24_black, null))
        } else {
            // 뷰들을 보임
            binding.presetReverbVisibilitySettingLinearLayout.visibility = View.VISIBLE
            binding.presetReverbWarningTextView.visibility = View.VISIBLE
            binding.presetReverbArrowImageView.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.baseline_keyboard_arrow_up_24_black, null))
        }
    }

        


    private fun initEnvironmentReverbFunction(){
//        binding.environmentReverbSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
//            if (isChecked){
//                Log.d("환경 리버브","보냄")
//                val action = Actions.SET_ENVIRONMENT_REVERB
//                val bundle = Bundle()
//                val sessionCommand = SessionCommand(action, bundle)
//                activity.controller?.sendCustomCommand(sessionCommand, bundle)
//            }
//        }
    }

    override fun onStart() {
        super.onStart()
        initBroadcastReceiver()
    }


    private fun initBroadcastReceiver(){
        receiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Actions.GET_EQUALIZER_INFO){
                    Log.d("인텐트가 왔음","ㅎ")
                    val valuesToUpdate = arrayListOf<Entry>()
                    valuesToUpdate.add(Entry(0f,0f))
                    for (index in 0 until 5) {
                        val value = intent.getStringExtra("$index")?.toFloatOrNull() ?: 0f
                        valuesToUpdate.add(Entry(index + 1.0f, value / 1000)) // +1.0f는 Entry에서의 X 값이므로 조절이 필요합니다.
                    }
                    valuesToUpdate.add(Entry(6f,0f))
                    sharedViewModel.equalizerChartValueList = valuesToUpdate

                    updateChart(valuesToUpdate)

                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val filter = IntentFilter(Actions.GET_EQUALIZER_INFO)
            requireActivity().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else{
            val filter = IntentFilter(Actions.GET_EQUALIZER_INFO)
            requireActivity().registerReceiver(receiver, filter)

        }

    }

    private fun updateChart(newValues: ArrayList<Entry>){
        val lineChart = binding.bandwidthChart
        val lineData = lineChart.data
        if (lineData != null) {
            var newSet: LineDataSet? = lineData.getDataSetByIndex(0) as? LineDataSet
            if (newSet == null) { // 초기 데이터 설정 시 null check
                newSet = LineDataSet(newValues, "DataSet Label with circle")
                newSet.lineWidth = 4f
                newSet.color = resources.getColor(R.color.blue_background)
                newSet.setCircleColor(resources.getColor(R.color.blue_background))
                newSet.circleRadius = 7f
                newSet.valueTextSize = 8f
                newSet.setDrawValues(true)
                lineData.addDataSet(newSet)
            } else {
                newSet.values = newValues

                lineData.notifyDataChanged()
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()
                lineChart.animateY(100)

            }

        }
    }

    private fun initEqualizerEvent(){
        setEqualizerVisibility()

        binding.equalizerLinearLayout.setOnClickListener {
            sharedViewModel.isEqualizerViewFolded = !sharedViewModel.isEqualizerViewFolded
            setEqualizerVisibility()
        }
        binding.equalizerSwitch.setOnClickListener {
            sharedViewModel.setIsEqualizerEnabled(binding.equalizerSwitch.isChecked)
        }

    }



    private fun initEqualizerChart(){
        val lineChart = binding.bandwidthChart
        val valuesWithCircle = arrayListOf<Entry>()

        valuesWithCircle.add(Entry(0f, 0f))
        valuesWithCircle.add(Entry(1.0f, 0f)) // 첫 번째 주파수 값
        valuesWithCircle.add(Entry(2.0f, 0f)) // 두 번째 주파수 값
        valuesWithCircle.add(Entry(3.0f, 0f)) // 세 번째 주파수 값
        valuesWithCircle.add(Entry(4.0f, 0f)) // 네 번째 주파수 값
        valuesWithCircle.add(Entry(5.0f, 0f)) // 다섯 번째 주파수 값
        valuesWithCircle.add(Entry(6.0f, 0f))

        val lineDataWithCircle = LineDataSet(valuesWithCircle, "DataSet Label with circle")
        lineDataWithCircle.lineWidth = 4f
        lineDataWithCircle.color = resources.getColor(R.color.blue_background)

        lineDataWithCircle.setCircleColor(resources.getColor(R.color.blue_background))
        lineDataWithCircle.circleRadius = 7f
        lineDataWithCircle.valueTextSize = 8f
        lineDataWithCircle.setDrawValues(false)

        lineChart.data = LineData(lineDataWithCircle)

        val yAxisLeft: YAxis = lineChart.axisLeft
        yAxisLeft.textColor = resources.getColor(R.color.blue_background)
        yAxisLeft.axisMinimum = -1.5f
        yAxisLeft.axisMaximum = 1.5f
        yAxisLeft.setDrawGridLines(false)

        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        val xAxis: XAxis = lineChart.xAxis
        xAxis.textColor = resources.getColor(R.color.blue_background)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.textColor = Color.BLACK
        xAxis.granularity = 0.5f
        xAxis.isGranularityEnabled = true
        xAxis.axisMaximum = 6f
        xAxis.axisMinimum = 0f

        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 5
        xAxis.valueFormatter = object: ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                val xLabels = arrayListOf("60kHz", "250kHz", "500Hz", "2kHz", "4kHz")
                Log.d("밸류","$value")
                return when(value){
                    1.0f -> xLabels[0]
                    2.0f -> xLabels[1]
                    3.0f -> xLabels[2]
                    4.0f -> xLabels[3]
                    5.0f -> xLabels[4]
                    else -> ""
                }
            }

        }

        lineChart.isScaleXEnabled = false
        lineChart.isScaleYEnabled = false

        lineChart.isDragEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.isHighlightPerTapEnabled = false
        lineChart.isHighlightPerDragEnabled = false

        lineChart.invalidate()
    }

    private fun initEqualizerScrollViw(){
        val linearLayout = binding.equalizerScrollViewLinearLayout
        equalizerTextViewList = mutableListOf()

        val items = resources.getStringArray(R.array.equalizer_label)

        items.forEachIndexed { index, name ->
            val textView = TextView(requireActivity())
            textView.text = name
            textView.setTextColor(resources.getColor(R.color.description_color))
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 0, 40, 0)
            }
            if (index == (sharedViewModel.equalizerIndexValue.value ?: 0)){
                changeFocusToSelectedEqualizerTextView(textView)
            }

            textView.setOnClickListener {
                sharedViewModel.setEqualizerIndexValue(index)
                changeFocusToSelectedEqualizerTextView(it)
            }
            equalizerTextViewList.add(textView)
            linearLayout.addView(textView)
        }
    }
    private fun changeFocusToSelectedEqualizerTextView(view: View){
        selectedEqualizerTextView?.setTextColor(resources.getColor(R.color.description_color))
        selectedEqualizerTextView?.setTypeface(null, Typeface.NORMAL)

        val clickedTextView = view as TextView
        clickedTextView.setTextColor(resources.getColor(R.color.blue_background))  // 현재 선택된 TextView의 색을 파란색으로 변경
        clickedTextView.setTypeface(null, Typeface.BOLD)
        selectedEqualizerTextView = clickedTextView

        clickedTextView.post{
            val scrollX = (clickedTextView.left - (binding.valueScrollView.width / 2)) + (clickedTextView.width / 2)
            binding.valueScrollView.smoothScrollTo(scrollX, 0)
        }
    }

    private fun setEqualizer(index: Int){

        Log.d("이퀼","setEqualizer$index")

        val action = Actions.SET_EQUALIZER
        val bundle = Bundle().apply {
            putInt("value",index)
        }
        val sessionCommand = SessionCommand(action, bundle)
        activity.controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun setEqualizerVisibility(){
        // LineChart의 현재 visibility 상태를 확인

        if (sharedViewModel.isEqualizerViewFolded) {
            // 뷰들을 숨김
            binding.valueScrollView.visibility = View.GONE
            binding.bandwidthChart.visibility = View.GONE
            binding.equalizerArrowImageView.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.baseline_keyboard_arrow_down_24_black, null))
        } else {
            // 뷰들을 보임
            binding.valueScrollView.visibility = View.VISIBLE
            binding.bandwidthChart.visibility = View.VISIBLE
            binding.equalizerArrowImageView.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.baseline_keyboard_arrow_up_24_black, null))

        }
    }

    override fun onDestroyView() {
        if (::receiver.isInitialized) {
            requireActivity().unregisterReceiver(receiver)
        }
        super.onDestroyView()
    }
}