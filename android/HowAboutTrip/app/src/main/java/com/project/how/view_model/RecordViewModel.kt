package com.project.how.view_model

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.project.how.BuildConfig
import com.project.how.data_class.dto.EmptyResponse
import com.project.how.data_class.dto.ErrorResponse
import com.project.how.data_class.dto.recode.image.AddedImage
import com.project.how.data_class.dto.recode.image.ImageElement
import com.project.how.data_class.dto.recode.image.ImagesResponse
import com.project.how.data_class.dto.recode.image.LocationSchedules
import com.project.how.data_class.dto.recode.image.SaveImageRequest
import com.project.how.data_class.dto.recode.ocr.OcrResponse
import com.project.how.data_class.dto.recode.ocr.ProductLineItem
import com.project.how.data_class.dto.recode.receipt.ChangeOrderReceiptRequest
import com.project.how.data_class.dto.recode.receipt.GetReceiptDetail
import com.project.how.data_class.dto.recode.receipt.GetReceiptListResponse
import com.project.how.data_class.dto.recode.receipt.ReceiptDetail
import com.project.how.data_class.dto.recode.receipt.ReceiptDetailListItem
import com.project.how.data_class.dto.recode.receipt.ReceiptSimpleList
import com.project.how.data_class.dto.recode.receipt.StoreType
import com.project.how.model.RecordRepository
import com.project.how.network.client.OCRRetrofit
import com.project.how.network.client.RecordRetrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class RecordViewModel : ViewModel() {
    private val recordRepository : RecordRepository = RecordRepository()
    private val _currentReceiptListLiveData = recordRepository.currentReceiptListLiveData
    private val _uriLiveData = recordRepository.uriLiveData
    private val _ocrResponseLiveData = recordRepository.ocrResponseLiveData
    private val _receiptSimpleListLiveData = recordRepository.receiptSimpleListLiveData
    private val _saveCheckLiveData = recordRepository.saveCheckLiveData
    private val _locationSchedulesLiveData = recordRepository.locationSchedulesLiveData
    private val _imagesLiveData = recordRepository.imagesLiveData
    private val _addedImageLiveDate = recordRepository.addedImageLiveData
    val currentReceiptListLiveData : LiveData<GetReceiptListResponse>
        get() = _currentReceiptListLiveData
    val uriLiveData : LiveData<Uri>
        get() = _uriLiveData
    val ocrResponseLiveData : LiveData<OcrResponse?>
        get() = _ocrResponseLiveData
    val receiptSimpleListLiveData : LiveData<ReceiptSimpleList>
        get() = _receiptSimpleListLiveData
    val saveCheckLiveData : LiveData<Boolean>
        get() = _saveCheckLiveData
    val locationSchedulesLiveData : LiveData<LocationSchedules?>
        get() = _locationSchedulesLiveData
    val imagesLiveData : LiveData<ImagesResponse?>
        get() = _imagesLiveData
    val addedImageLiveDate : LiveData<ImageElement>
        get() = _addedImageLiveDate

    fun uploadReceipt(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = getFileFromURI(context, uri)
            if (imageFile == null){
                Log.d("uploadReceipt", "uploadReceipt prepare is failed\nimageFile : $imageFile")
                recordRepository.getOcrResponseLiveData(null)
                return@launch
            }
            Log.d("uploadReceipt", "Retrofit start!")
            uploadReceipt(imageFile)
        }
    }

    fun getImageUri(uri: Uri){
        recordRepository.getUri(uri)
    }

    fun saveReceiptNonImage(detail: ReceiptDetail) = viewModelScope.launch(Dispatchers.IO) {
        val requestBody =
            Gson().toJson(detail).toRequestBody("application/json".toMediaTypeOrNull())

        RecordRetrofit.getApiService()?.let { apiService->
            apiService.saveReceipt(requestBody, null)
                .enqueue(object : Callback<String>{
                    override fun onResponse(p0: Call<String>, p1: Response<String>) {
                        try {
                            if (p1.code() == 201){
                                recordRepository.getSaveCheck(true)
                            }else{
                                recordRepository.getSaveCheck(false)
                            }
                        }catch (e: Exception){
                            Log.e("saveReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                            recordRepository.getSaveCheck(false)
                        }
                    }
                    override fun onFailure(p0: Call<String>, p1: Throwable) {
                        Log.e("saveReceipt", "onFailure\nerror : ${p1.message}")
                        recordRepository.getSaveCheck(false)
                    }

                })

        }
    }

    fun saveReceipt(context: Context, detail: ReceiptDetail) = viewModelScope.launch (Dispatchers.IO) {
            if (uriLiveData.value == null){
                return@launch
            }

            val imageFile = getFileFromURI(context, uriLiveData.value!!) ?: return@launch
            saveReceipt(imageFile, detail).join()
    }

    fun updateReceipt(context: Context, receiptId : Long, detail: ReceiptDetail) = viewModelScope.launch (Dispatchers.IO){
        if (uriLiveData.value == null){
            updateReceipt(receiptId, null, detail)
        }else{
            val imageFile = getFileFromURI(context, uriLiveData.value!!)
            updateReceipt(receiptId, imageFile, detail).join()
        }
    }

    private fun uploadReceipt(file : File){
        viewModelScope.launch (Dispatchers.IO) {
            val mediaType = getImageType(file)
            val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val detailCheck = "true".toRequestBody("text/plain".toMediaTypeOrNull())

            OCRRetrofit.getApiService()?.let {apiService->
                apiService.uploadReceipt(body, detailCheck, BuildConfig.OCR_API_KEY)
                    .enqueue(object : Callback<OcrResponse>{
                        override fun onResponse(p0: Call<OcrResponse>, p1: Response<OcrResponse>) {
                            try {
                                if (p1.isSuccessful){
                                    val result = p1.body()
                                    Log.d("uploadReceipt", "${result?.entities?.productLineItems?.size ?: "없음"}\n${result?.merchantName?.data ?: "인식 불가"}\ncurrency : ${result?.totalAmount?.currencyCode ?: "알 수 없음"}\ndate : ${result?.date?.data ?: "인식 불가"}")
                                    recordRepository.getOcrResponseLiveData(result)
                                }else{
                                    Log.e("uploadReceipt", "response is failed\ncode : ${p1.code()}")
                                    recordRepository.getOcrResponseLiveData(null)
                                }
                            }catch (e : HttpException){
                                Log.e("uploadReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                                recordRepository.getOcrResponseLiveData(null)
                            }
                        }

                        override fun onFailure(p0: Call<OcrResponse>, p1: Throwable) {
                            Log.e("uploadReceipt", "onFailure\nerror : ${p1.message}")
                            recordRepository.getOcrResponseLiveData(null)
                        }

                    })

            }
        }
    }

    fun getReceiptDetail(receiptId : Long) : Flow<GetReceiptDetail?> = callbackFlow<GetReceiptDetail?> {
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.getReceiptDetail(receiptId)
                .enqueue(object : Callback<GetReceiptDetail>{
                    override fun onResponse(
                        p0: Call<GetReceiptDetail>,
                        p1: Response<GetReceiptDetail>
                    ) {
                        try {
                            val result = p1.body()
                            trySend(result)
                        }catch (e : Exception){
                            Log.e("getReceiptDetail", "code : ${p1.code()}\nerror : ${e.message}")
                            trySend(null)
                        }
                    }

                    override fun onFailure(p0: Call<GetReceiptDetail>, p1: Throwable) {
                        Log.e("getReceiptDetail", "onFailure\nerror : ${p1.message}")
                        trySend(null)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getScheduleListWithReceipt(){
        viewModelScope.launch(Dispatchers.IO) {
            RecordRetrofit.getApiService()?.let { apiService->
                apiService.getScheduleListWithReceipt()
                    .enqueue(object : Callback<ReceiptSimpleList>{
                        override fun onResponse(
                            p0: Call<ReceiptSimpleList>,
                            p1: Response<ReceiptSimpleList>
                        ) {
                            try {
                                val result = p1.body()
                                recordRepository.getReceiptSimpleList(result)
                            }catch (e : Exception){
                                Log.e("getScheduleListWithReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                                recordRepository.getReceiptSimpleList(null)
                            }
                        }

                        override fun onFailure(p0: Call<ReceiptSimpleList>, p1: Throwable) {
                            Log.e("getSchedule", "onFailure\nerror : ${p1.message}")
                            recordRepository.getReceiptSimpleList(null)
                        }

                    })

            }
        }
    }

    fun deleteReceipt(receiptId : Long) : Flow<Boolean> = callbackFlow<Boolean> {
        RecordRetrofit.getApiService()?.let { apiService ->
            apiService.deleteReceipt(receiptId)
                .enqueue(object : Callback<EmptyResponse>{
                    override fun onResponse(p0: Call<EmptyResponse>, p1: Response<EmptyResponse>) {
                        try {
                            Log.d("deleteReceipt", "code : ${p1.code()}")
                            when(p1.code()){
                                SUCCESS->{
                                    Log.d("deleteReceipt", "success")
                                    trySend(true)
                                }
                                NOT_FOUND->{
                                    Log.d("deleteReceipt", "this receipt ($receiptId) is not exist")
                                    trySend(false)
                                }
                                else->{
                                    Log.d("deleteReceipt", "unknown error\ncode : ${p1.code()}")
                                    trySend(false)
                                }
                            }

                        }catch (e : Exception){
                            Log.e("deleteReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                            trySend(false)
                        }
                    }

                    override fun onFailure(p0: Call<EmptyResponse>, p1: Throwable) {
                        Log.e("deleteReceipt", "onFailure\nerror : ${p1.message}")
                    }

                })

        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getReceiptList(scheduleId : Long){
        viewModelScope.launch (Dispatchers.IO){
            RecordRetrofit.getApiService()?.let { apiService->
                apiService.getReceiptList(scheduleId)
                    .enqueue(object : Callback<GetReceiptListResponse>{
                        override fun onResponse(
                            p0: Call<GetReceiptListResponse>,
                            p1: Response<GetReceiptListResponse>
                        ) {
                            try {
                                if (p1.code() == SUCCESS){
                                    val result = p1.body()
                                    recordRepository.getCurrentReceiptList(result!!)
                                }else{
                                    Log.e("getReceiptList", "code : ${p1.code()}\nreponse is success = ${p1.isSuccessful}")
                                }
                            }catch (e : Exception){
                                Log.e("getReceiptList", "code : ${p1.code()}\nerror : ${e.message}")
                            }
                        }

                        override fun onFailure(p0: Call<GetReceiptListResponse>, p1: Throwable) {
                            Log.e("getReceiptList", "onFailure\nnetwork error : ${p1.message}")
                        }

                    })

            }
        }
    }

    private fun updateReceipt(receiptId : Long, file: File?, detail: ReceiptDetail) = viewModelScope.launch(Dispatchers.IO) {
        val requestBody =
            Gson().toJson(detail).toRequestBody("application/json".toMediaTypeOrNull())

        var body : MultipartBody.Part? = null
        if (file != null){
            val mediaType = getImageType(file)
            val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("receiptImg", file.name, requestFile)
        }
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.updateReceipt(receiptId, requestBody, body)
                .enqueue(object : Callback<String>{
                    override fun onResponse(p0: Call<String>, p1: Response<String>) {
                        try {
                            when(p1.code()) {
                                SUCCESS -> {
                                    Log.d("updateReceipt", "success")
                                    recordRepository.getSaveCheck(true)
                                }
                                NOT_FOUND -> {
                                    Log.d("updateReceipt", "this receipt ($receiptId) is not exist")
                                    recordRepository.getSaveCheck(false)
                                }
                                else -> {
                                    Log.d("updateReceipt", "unknown error code : ${p1.code()}")
                                    recordRepository.getSaveCheck(false)
                                }

                            }
                        }catch (e : Exception){
                            Log.e("updateReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                            recordRepository.getSaveCheck(false)
                        }
                    }

                    override fun onFailure(p0: Call<String>, p1: Throwable) {
                        Log.e("updateReceipt", "onFailure\nerror : ${p1.message}")
                        recordRepository.getSaveCheck(false)
                    }

                })
        }
    }

    private fun saveReceipt(file: File, detail: ReceiptDetail) = viewModelScope.launch(Dispatchers.IO) {
            val requestBody =
                Gson().toJson(detail).toRequestBody("application/json".toMediaTypeOrNull())

            val mediaType = getImageType(file)
            val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("receiptImg", file.name, requestFile)

            RecordRetrofit.getApiService()?.let { apiService->
                apiService.saveReceipt(requestBody, body)
                    .enqueue(object : Callback<String>{
                        override fun onResponse(p0: Call<String>, p1: Response<String>) {
                            try {
                                if (p1.code() == 201){
                                    recordRepository.getSaveCheck(true)
                                }else{
                                    recordRepository.getSaveCheck(false)
                                }
                            }catch (e: Exception){
                                Log.e("saveReceipt", "code : ${p1.code()}\nerror : ${e.message}")
                                recordRepository.getSaveCheck(false)
                            }
                        }
                        override fun onFailure(p0: Call<String>, p1: Throwable) {
                            Log.e("saveReceipt", "onFailure\nerror : ${p1.message}")
                            recordRepository.getSaveCheck(false)
                        }

                    })

            }
        }

    fun changeOrderReceipt(scheduleId: Long, changeOrderReceiptRequest: List<ChangeOrderReceiptRequest>) = callbackFlow<Int>{
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.changeOrderReceipt(scheduleId, changeOrderReceiptRequest)
                .enqueue(object : Callback<ErrorResponse?>{
                    override fun onResponse(
                        p0: Call<ErrorResponse?>,
                        p1: Response<ErrorResponse?>
                    ) {
                        try{
                            when(p1.code()){
                                SUCCESS->{
                                    trySend(SUCCESS)
                                }
                                BAD_REQUEST->{
                                    trySend(NOT_ALL)
                                }
                                else->{
                                    trySend(UNKNOWN)
                                }
                            }
                        }catch (e : Exception){
                            Log.e("changeOrderReceipt", "code : ${p1.code()} error : ${e.message}")
                            trySend(UNKNOWN)
                        }
                    }

                    override fun onFailure(p0: Call<ErrorResponse?>, p1: Throwable) {
                        Log.e("changeOrderReceipt", "onFailure\nerror : ${p1.message}")
                        trySend(NETWORK_FAILED)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun readImageCountries() = viewModelScope.launch(Dispatchers.IO){
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.readCountries()
                .enqueue(object : Callback<LocationSchedules>{
                    override fun onResponse(
                        p0: Call<LocationSchedules>,
                        p1: Response<LocationSchedules>
                    ) {
                        try {
                            if (p1.code() == SUCCESS){
                                val result = p1.body()
                                recordRepository.getLocationSchedules(result)
                            }else{
                                recordRepository.getLocationSchedules(null)
                                Log.e("readImageCountries", "code : ${p1.code()}")
                            }
                        }catch (e : Exception){
                            recordRepository.getLocationSchedules(null)
                            Log.e("readImageCountries", "code : ${p1.code()} error : ${e.message}")
                        }
                    }

                    override fun onFailure(p0: Call<LocationSchedules>, p1: Throwable) {
                        recordRepository.getLocationSchedules(null)
                        Log.e("readImageCountries", "onFailure\nerror : ${p1.message}")
                    }

                })
        }
    }

    fun readImages(scheduleId: Long) = viewModelScope.launch(Dispatchers.IO){
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.readImages(scheduleId)
                .enqueue(object : Callback<ImagesResponse> {
                    override fun onResponse(
                        p0: Call<ImagesResponse>,
                        p1: Response<ImagesResponse>
                    ) {
                        try {
                            if (p1.code() == SUCCESS){
                                val result = p1.body()
                                recordRepository.getImages(result)
                            }else{
                                Log.d("readImages", "code : ${p1.code()}")
                                recordRepository.getImages(null)
                            }
                        }catch (e : Exception){
                            Log.e("readImages", "code : ${p1.code()} error : ${e.message}")
                            recordRepository.getImages(null)
                        }
                    }

                    override fun onFailure(p0: Call<ImagesResponse>, p1: Throwable) {
                        Log.e("readImages", "onFailure\nerror : ${p1.message}")
                        recordRepository.getImages(null)
                    }

                })
        }
    }

    fun saveImage(context: Context, uri: Uri, scheduleId: Long) = viewModelScope.launch(Dispatchers.IO){
        val image = getFileFromURI(context, uri)
        val date = getImageDate(context, uri)

        Log.d("getImageDate", "date : $date")
        if (image != null && date != null){
            saveImage(image, date, scheduleId)
            return@launch
        }

    }

    private fun saveImage(file : File, date : String, id: Long) = viewModelScope.launch(Dispatchers.IO){
        val request = SaveImageRequest(id, date)
        val requestBody =
            Gson().toJson(request).toRequestBody("application/json".toMediaTypeOrNull())

        val mediaType = getImageType(file)
        val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.saveImage(requestBody, body)
                .enqueue(object : Callback<AddedImage>{
                    override fun onResponse(p0: Call<AddedImage>, p1: Response<AddedImage>) {
                        try {
                            if (p1.code() == CREATED){
                                val result = p1.body()
                                val addedImage = ImageElement(result!!.id, result.url, result.date)
                                recordRepository.addImage(addedImage)
                                return
                            }
                        }catch (e : Exception){
                            Log.e("saveImage", "code : ${p1.code()} error : ${e.message}")
                        }
                    }

                    override fun onFailure(p0: Call<AddedImage>, p1: Throwable) {
                        Log.e("saveImage", "onFailure\nerror : ${p1.message}")
                    }

                })
        }
    }

    fun deleteImage(imageId : Long) = callbackFlow<Boolean> {
        RecordRetrofit.getApiService()?.let { apiService->
            apiService.deleteImage(imageId)
                .enqueue(object : Callback<EmptyResponse>{
                    override fun onResponse(p0: Call<EmptyResponse>, p1: Response<EmptyResponse>) {
                        try {
                            if (p1.code() == NO_CONTENT){
                                trySend(true)
                            }else{
                                trySend(false)
                            }
                        }catch (e : Exception){
                            Log.e("deleteImage", "code : ${p1.code()} error : ${e.message}")
                            trySend(false)
                        }
                    }

                    override fun onFailure(p0: Call<EmptyResponse>, p1: Throwable) {
                        Log.e("deleteImage", "onFailure\nerror : ${p1.message}")
                        trySend(false)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    private fun getImageType(file: File) : String{
        val mediaType = when {
            file.extension.equals("jpg", true) || file.extension.equals("jpeg", true) -> "image/jpeg"
            file.extension.equals("png", true) -> "image/png"
            else -> "application/octet-stream"
        }

        return mediaType
    }

    private suspend fun getImageDate(context: Context, uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            val dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

            Log.d("getImageDate", "EXIF date : $dateTaken")
            if (!dateTaken.isNullOrEmpty()) {
                val dateTakenFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                return dateTakenFormat.parse(dateTaken)?.let { format.format(it) }
            }else{
                Log.d("getImageDate", "EXIF date is null")

                getFileFromURI(context, uri)?.let { file ->
                    Log.d("getImageDate", "\n" +
                            "\n---------------------file--------------------------imageType : ${getImageType(file)}")
                    val attrs: BasicFileAttributes = Files.readAttributes(
                        file.toPath(),
                        BasicFileAttributes::class.java
                    )
                    val creationTime = attrs.creationTime()
                    val date = creationTime.toString().replace("[TZ]".toRegex(), " ")
                    Log.d("getImageDate", "createTime : ${creationTime.toString().replace("[TZ]".toRegex(), " ")}")
                    return date
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    suspend fun getFileFromURI(context: Context, uri: Uri): File? {
        return withContext(Dispatchers.IO) {
            recordRepository.getUri(uri)
            Log.d("getFileFromURI", "getRealPathFromURI Start\nuri : $uri\nuri.path : ${uri.path}")
            var file: File? = null

            if (uri.scheme == "content") {
                // InputStream을 통해 파일 복사
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    // 임시 파일 생성
                    val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                    file = tempFile
                }
            } else if (uri.scheme == "file") {
                // file URI인 경우 직접 경로를 가져옴
                file = File(uri.path)
            }

            Log.d("getFileFromURI", "getRealPathFromURI end\n$file")
            file
        }
    }

    suspend fun getReceiptDetail(data : GetReceiptDetail, storeName : String) : ReceiptDetail?{
        return try {
            withContext(Dispatchers.IO){
                ReceiptDetail(
                    data.id,
                    storeName,
                    StoreType.PLACE,
                    data.purchaseDate,
                    data.totalPrice,
                    data.receiptDetailList
                )
            }
        }catch (e : Exception){
            null
        }
    }

    suspend fun getReceiptDetail(id: Long, currentDate : String, ocrResponse: OcrResponse): ReceiptDetail? {
        try {
            return withContext(Dispatchers.IO) {
                val totalPrice = getTotalPrice(ocrResponse.entities.productLineItems)
                val receiptDetails = getReceiptDetails(ocrResponse.entities.productLineItems)

                ReceiptDetail(
                    id,
                    ocrResponse.merchantName.data,
                    StoreType.PLACE,
                    currentDate,
                    totalPrice,
                    receiptDetails
                )
            }
        }catch (e : Exception){
            return null
        }
    }

    fun createUri(resolver: ContentResolver) : Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Captured Image")
            put(MediaStore.Images.Media.DESCRIPTION, "Image captured by camera")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun getReceiptDetails(productLineItems: List<ProductLineItem>): List<ReceiptDetailListItem> {
        return productLineItems.map {
            ReceiptDetailListItem(
                it.data.name.data,
                ((it.data.totalPrice?.data ?: 0.0) / (it.data.unitPrice?.data ?: 1.0)).toLong(),
                it.data.unitPrice?.data ?: 1.0
            )
        }
    }

    private fun getTotalPrice(productLineItems: List<ProductLineItem>): Double {
        return if(!productLineItems.isNullOrEmpty()) productLineItems.sumOf { it.data.totalPrice?.data ?: 0.0 } else 0.0
    }

    fun getDaysTitle(startDate : String, tabNum : Int) : String {
        val sd = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val formatter = DateTimeFormatter.ofPattern("MM.dd")
        return sd.plusDays(tabNum.toLong()).format(formatter)
    }

    fun getCurrentDate(startDate: String, tabNum: Int) : String {
        val sd = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return sd.plusDays(tabNum.toLong()).format(formatter)
    }

    fun getDatesList(startDate: String, endDate: String) : List<String>{
        val sd = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val ed = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)

        val datesList = mutableListOf<String>()

        var currentDate = sd
        while (currentDate <= ed) {
            datesList.add(currentDate.toString())
            currentDate = currentDate.plusDays(1)
        }

        return datesList
    }


    companion object{
        const val SUCCESS = 200
        const val CREATED = 201
        const val NO_CONTENT = 204
        const val NOT_FOUND = 404
        const val BAD_REQUEST = 400
        const val NOT_ALL = -400
        const val DUPLICATE_ORDER_NUM = -401
        const val UNKNOWN = -1
        const val NETWORK_FAILED = -2
    }
}