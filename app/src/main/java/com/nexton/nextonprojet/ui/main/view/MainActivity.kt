package com.nexton.nextonprojet.ui.main.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexton.nextonprojet.R
import com.nexton.nextonprojet.data.api.ApiHelper
import com.nexton.nextonprojet.data.api.ApiServiceImpl
import com.nexton.nextonprojet.data.base.ViewModelFactory
import com.nexton.nextonprojet.data.model.Episodes
import com.nexton.nextonprojet.data.model.ProgrammeTvItems
import com.nexton.nextonprojet.databinding.ActivityMainBinding
import com.nexton.nextonprojet.ui.main.adapter.EpisodeAdapter
import com.nexton.nextonprojet.ui.main.adapter.HorizontalAdapter
import com.nexton.nextonprojet.ui.main.adapter.MySliderImageAdapter
import com.nexton.nextonprojet.ui.main.adapter.TVShowAdapter
import com.nexton.nextonprojet.ui.main.viewmodel.MainViewModel
import com.nexton.nextonprojet.utils.Constants
import com.nexton.nextonprojet.utils.Status
import com.nexton.nextonprojet.utils.URLConstants
import com.nexton.nextonprojet.utils.Utility.hideKeyboard
import com.smarteist.autoimageslider.SliderView


class MainActivity : AppCompatActivity() {

    private val mTvShowList: ArrayList<ProgrammeTvItems> = ArrayList()
    private lateinit var mainViewModel: MainViewModel

    private var adapter: TVShowAdapter? = null
    private lateinit var mBinding: ActivityMainBinding


    private lateinit var sharedPrefs: SharedPreferences
    var prefs: String = Constants.SPACE

    private val TAG = javaClass.simpleName




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()

    }

    /**
     * init method
     */
    fun init() {

        setupViewModel()
        setupObserver()
        initAdapter()
        initEvent()
    }

    /**
     * init the Adapter and pass the Grid View
     */
    fun initAdapter() {
        adapter = TVShowAdapter(mTvShowList, this)
        mBinding.gridTv.adapter = adapter

        initSlider()
        initRecyclers()
    }


    fun initEvent() {

        /**
         * hide keyboard and lose focus
         */
        mBinding.containerFilm.setOnClickListener {
            hideKeyboard(it)
            mBinding.header.searchContent.clearFocus()
            mBinding.header.searchContent.setHint(R.string.film_search_hint)
            mBinding.header.historyContent.visibility = View.INVISIBLE
            Log.i(TAG, "container is clicked")
        }


        /**
         * clear search content and set Visibility for GridView
         */
        mBinding.header.searchClear.setOnClickListener {
            mBinding.header.searchContent.text.clear()
            mBinding.header.searchContent.setHint(R.string.film_search_hint)
            hideGrid(true)
            mBinding.progressBar.visibility = View.GONE
            Log.i(TAG, "clear search is clicked")
        }

        /**
         * execute request in text watcher
         */
        mBinding.header.searchContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // execute the request after 3 characters taped
                if (s.toString().count() > 3) {
                    mainViewModel.fetchProgrammeTv(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /**
                 * manage clear button and history textView visibility
                 */
                if (mBinding.header.searchContent.length() > 0) {
                    mBinding.header.searchClear.visibility = View.VISIBLE

                } else {
                    mBinding.header.searchClear.visibility = View.GONE
                }

                /**
                 * manage Loader visibility
                 */
                if (mBinding.header.searchContent.length() == 0)
                    mBinding.progressBar.visibility = View.GONE
            }


        })


        /**
         * manage hint visibility in searchTextView
         */
        mBinding.header.searchContent.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            mBinding.header.searchContent.hint = Constants.SPACE

            if (mBinding.gridTv.visibility == View.VISIBLE) {
                hideGrid(false)
            }
            mBinding.header.searchContent.setHint(R.string.film_search_hint)


            /**
             * manage history TextView  visibility
             */
            getSharedPrefs()
            if (prefs != Constants.SPACE) {
                mBinding.header.historyContent.text = prefs
                mBinding.header.historyContent.visibility = View.VISIBLE
            }
        }


        /**
         * set history TextView content in Search bar
         */
        mBinding.header.historyContent.setOnClickListener {
            if (mBinding.header.historyContent.text != null) {
                hideKeyboard(it)
                mBinding.header.searchContent.setText(mBinding.header.historyContent.text)
                mBinding.header.historyContent.visibility = View.GONE

                //clear sharedValue
                sharedPrefs.edit()?.clear()?.apply()
            }
        }

    }


    /**
     * setup the Observer and retrieve data
     */
    fun setupObserver() {
        mainViewModel.programmeTvLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    mBinding.progressBar.visibility = View.GONE
                    it.data?.let { programme -> renderSearchList(programme.contents) }
                }
                Status.LOADING -> {
                    mBinding.progressBar.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    mBinding.progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * retrieve data and set it in the Adapter
     */
    private fun renderSearchList(@Nullable programmes: List<ProgrammeTvItems>) {
        Log.i(TAG, "programmes : $programmes")

        if (programmes.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            hideGrid(true)
        } else {
            hideGrid(false)
            adapter?.setData(programmes)
        }

    }

    /**
     * set Up the ViewModel
     */
    fun setupViewModel() {
        mainViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(ApiServiceImpl()))
        ).get(MainViewModel::class.java)
    }


    /**
     * Mange GridView Visibility
     */
    fun hideGrid(hide: Boolean) {
        if (hide) {
            mBinding.gridTv.visibility = View.GONE
            mBinding.filmImgLayout.visibility = View.VISIBLE
        } else {
            mBinding.gridTv.visibility = View.VISIBLE
            mBinding.filmImgLayout.visibility = View.GONE
        }
    }

    /**
     * Mange the physical exit button event
     */
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.quit_application), Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed( {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    /**
     * get search history from sharedPreferences
     */
    fun getSharedPrefs() {
        sharedPrefs = getSharedPreferences(Constants.SHAREDPREFS, Context.MODE_PRIVATE)
        prefs = sharedPrefs.getString(Constants.SHAREDPREFS, Constants.SPACE).toString()
    }


    /**
     * init Slider with film images
     */
    private fun initSlider (){

        val imageList: ArrayList<String> = ArrayList()

        imageList.add(URLConstants.POSTER_URL1)
        imageList.add(URLConstants.POSTER_URL2)
        imageList.add(URLConstants.POSTER_URL3)
        imageList.add(URLConstants.POSTER_URL4)
        imageList.add(URLConstants.POSTER_URL5)

        setImageInSlider(imageList, mBinding.slider)
    }

    /**
     * set Images in horizontal slider
     */
    private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
        val adapter = MySliderImageAdapter()
        adapter.renewItems(images)
        imageSlider.setSliderAdapter(adapter)
        imageSlider.isAutoCycle = true
        imageSlider.startAutoCycle()
    }

    /**
     * init Recyclers with static lists
     */
    private fun initRecyclers(){

        mBinding.filmsHori.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.seriesHori.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        val data: ArrayList<String> = ArrayList()
        val films: ArrayList<String> = ArrayList()


        data.add(URLConstants.POSTER_URL5)
        data.add(URLConstants.POSTER_URL4)
        data.add(URLConstants.POSTER_URL2)
        data.add(URLConstants.POSTER_URL1)
        data.add(URLConstants.POSTER_URL3)

        films.add(URLConstants.POSTER_URL6)
        films.add(URLConstants.POSTER_URL7)
        films.add(URLConstants.POSTER_URL8)
        films.add(URLConstants.POSTER_URL9)
        films.add(URLConstants.POSTER_URL10)
        films.add(URLConstants.POSTER_URL11)


        mBinding.filmsHori.adapter = HorizontalAdapter(films)
        mBinding.seriesHori.adapter = HorizontalAdapter(data)
    }


}
