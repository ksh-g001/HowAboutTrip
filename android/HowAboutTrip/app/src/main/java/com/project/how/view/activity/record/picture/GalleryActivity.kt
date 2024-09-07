package com.project.how.view.activity.record.picture

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.project.how.R
import com.project.how.adapter.recyclerview.record.image.GalleryAdapter
import com.project.how.adapter.recyclerview.record.image.ImageAdapter
import com.project.how.data_class.ScheduleSimpleInfo
import com.project.how.data_class.dto.recode.image.ImagesResponse
import com.project.how.databinding.ActivityGalleryBinding
import com.project.how.interface_af.OnDialogListener
import com.project.how.view.dialog.WarningDialog
import com.project.how.view.serializable_helper.SerializableHelper
import com.project.how.view_model.RecordViewModel
import kotlinx.coroutines.launch

class GalleryActivity :
    AppCompatActivity(),
    OnDialogListener,
    ImageAdapter.OnItemClickListener{
    private lateinit var binding : ActivityGalleryBinding
    private lateinit var adapter : GalleryAdapter
    private val recordViewModel : RecordViewModel by viewModels()
    private lateinit var pickMedia : ActivityResultLauncher<PickVisualMediaRequest>
    private var id : Long = -1
    private lateinit var scheduleName : String
    private lateinit var startDate : String
    private lateinit var endDate : String
    private var empty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)
        binding.gallery = this
        binding.lifecycleOwner = this
        adapter = GalleryAdapter(ImagesResponse(emptyList()), this)
        binding.recyclerView.adapter = adapter
        init()

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri->
            uri?.let{
                recordViewModel.saveImage(applicationContext, uri, id)
                return@registerForActivityResult
            }
            Toast.makeText(this, getString(R.string.image_load_error), Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            recordViewModel.addedImageLiveDate.observe(this@GalleryActivity){
                adapter.add(it)
                if (empty)
                    binding.emptyText.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            recordViewModel.imagesLiveData.observe(this@GalleryActivity){
                if (it == null) {
                    error()
                    return@observe
                }else if (it.scheduleImages.isEmpty()){
                    empty()
                    return@observe
                }

                adapter.update(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init(){
        val info = SerializableHelper().getSerializable(this, getString(R.string.schedulesimpleinfo), ScheduleSimpleInfo::class.java)
        id = info.id
        scheduleName = info.name
        startDate = info.startDate
        endDate = info.endDate
        binding.scheduleName.text = scheduleName
        binding.date.text = getString(R.string.date_text, startDate, endDate)
        if (id == -1L) {
            error()
            return
        }

        recordViewModel.readImages(id)
    }

    fun add(){
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun confirm(){
        if (adapter.getEdit()){
            save()
        }else{
            edit()
        }
    }

    private fun edit(){
        binding.confirm.text = getString(R.string.save)
        binding.confirm.setCompoundDrawablesRelativeWithIntrinsicBounds(
            AppCompatResources.getDrawable(this, R.drawable.icon_save_size_fixed),
            null,
            null,
            null
        )
        binding.add.visibility = View.VISIBLE
        adapter.editStart()
    }

    private fun save(){
        binding.confirm.text = getString(R.string.edit)
        binding.confirm.setCompoundDrawablesRelativeWithIntrinsicBounds(
            AppCompatResources.getDrawable(this, R.drawable.icon_edit_black_size_fixed),
            null,
            null,
            null
        )
        binding.add.visibility = View.GONE
        adapter.editEnd()
    }

    private fun empty(){
        empty = true
        binding.emptyText.visibility = View.VISIBLE
    }

    private fun error(){
        WarningDialog(getString(R.string.location_load_error), this).show(supportFragmentManager, "WarningDialog")
    }

    override fun onDismissListener() {
        finish()
    }

    override fun onDeleteClickListener(imageId: Long) {
        lifecycleScope.launch {
            recordViewModel.deleteImage(imageId).collect{
                if (it){
                    adapter.delete(imageId)
                    return@collect
                }

                Toast.makeText(this@GalleryActivity, getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onItemClickListener(image: String) {

    }
}