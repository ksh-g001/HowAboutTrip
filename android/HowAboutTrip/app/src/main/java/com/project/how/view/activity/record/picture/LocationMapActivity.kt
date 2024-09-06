package com.project.how.view.activity.record.picture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.project.how.R
import com.project.how.data_class.ScheduleSimpleInfo
import com.project.how.data_class.dto.recode.image.LocationSchedules
import com.project.how.data_class.dto.schedule.GetScheduleListResponse
import com.project.how.databinding.ActivityLocationMapBinding
import com.project.how.interface_af.OnDialogListener
import com.project.how.view.activity.MainActivity.Companion.REQUEST_CODE
import com.project.how.view.dialog.ScheduleChoiceDialog
import com.project.how.view.dialog.WarningDialog
import com.project.how.view.map_helper.CameraOptionProducer
import com.project.how.view.map_helper.MarkerProducer
import com.project.how.view_model.RecordViewModel
import com.project.how.view_model.ScheduleViewModel
import kotlinx.coroutines.launch

class LocationMapActivity : AppCompatActivity(),
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    OnDialogListener,
    ScheduleChoiceDialog.OnDialogListener{
    private lateinit var binding : ActivityLocationMapBinding
    private lateinit var supportMapFragment: SupportMapFragment
    private val recordViewModel : RecordViewModel by viewModels()
    private val scheduleViewModel : ScheduleViewModel by viewModels()
    private lateinit var locations : LocationSchedules
    private lateinit var scheduleList : GetScheduleListResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_map)
        binding.map = this
        binding.lifecycleOwner = this

        lifecycleScope.launch {
            recordViewModel.locationSchedulesLiveData.observe(this@LocationMapActivity) {
                if(it != null){
                    locations = it
                    supportMapFragment.getMapAsync(this@LocationMapActivity)
                    return@observe
                }

                error()
            }
        }

       lifecycleScope.launch {
           scheduleViewModel.scheduleListLiveData.observe(this@LocationMapActivity){
               if (it != null){
                   scheduleList = it
                   return@observe
               }

               error()
           }
       }

        lifecycleScope.launch {
            val googleMapOptions = GoogleMapOptions()
                .zoomControlsEnabled(true)

            supportMapFragment = SupportMapFragment.newInstance(googleMapOptions)

            supportFragmentManager.beginTransaction()
                .replace(R.id.map, supportMapFragment)
                .commit()

            supportMapFragment.getMapAsync{
                val location = LatLng(35.95, 128.25)
                val camera = CameraOptionProducer().makeScheduleCameraUpdate(location, 1f)
                it.moveCamera(camera)
                if (ActivityCompat.checkSelfPermission(
                        this@LocationMapActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@LocationMapActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@LocationMapActivity, REQUIRED_PERMISSIONS, REQUEST_CODE)
                    return@getMapAsync
                }
                it.isMyLocationEnabled = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recordViewModel.readImageCountries()
        scheduleViewModel.getScheduleList()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Current location:\n$p0", Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(p0: GoogleMap) {
        lifecycleScope.launch {
            if (locations.locationSchedules.isEmpty()){
                empty()
                return@launch
            }
            p0.clear()
            p0.setOnMarkerClickListener(this@LocationMapActivity)

            for (i in locations.locationSchedules) {
                val location = LatLng(i.lat, i.lng)
                p0.addMarker(MarkerProducer().makeRecordMarkerOptions(this@LocationMapActivity, i.ids.size.toLong(), location))
            }
        }

    }

    private fun error(){
        WarningDialog(getString(R.string.location_load_error), this).show(supportFragmentManager, "WarningDialog")
    }

    private fun empty(){

    }

    override fun onDismissListener() {
        finish()
    }

    override fun onSelectListener(id: Long) {
        val schedule = scheduleList.find { it.id == id }
        val scheduleInfo = ScheduleSimpleInfo(
            id,
            schedule?.scheduleName ?: "",
            schedule?.startDate ?: "",
            schedule?.endDate ?: ""
        )
        val intent = Intent(this, GalleryActivity::class.java)
        intent.putExtra(getString(R.string.schedulesimpleinfo), scheduleInfo)
        startActivity(intent)
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val lat = p0.position.latitude
        val lng = p0.position.longitude

        val scheduleIds = locations.locationSchedules.find { it.lat == lat && it.lng == lng }?.ids ?: emptyList()
        if (scheduleIds.isEmpty() || scheduleList.isEmpty()){
            error()
            return false
        }

        val schedules = scheduleList.filter { scheduleIds.contains(it.id) }
        ScheduleChoiceDialog(schedules, this).show(supportFragmentManager, "ScheduleChoiceDialog")
        return true
    }

    companion object{
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,)
    }
}