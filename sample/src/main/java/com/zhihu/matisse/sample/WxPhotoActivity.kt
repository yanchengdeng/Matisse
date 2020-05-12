package com.zhihu.matisse.sample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.BasePreviewActivity
import com.zhihu.matisse.internal.ui.SelectedPreviewActivity
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import com.zhihu.matisse.ui.MatisseActivity
import kotlinx.android.synthetic.main.activity_wx_photo.*
import java.util.*

/**
 * 仿照微信相册选择框
 * 支持添加  、选择、 预览
 */
class WxPhotoActivity : AppCompatActivity() {



    private var addItemImage : AddImageAdapter ? = null
    //已选择图片Item格式   因为目前 知乎里的 文件引用类型比较多 ，有uri  string  也有变异后的item
    private var imagesSelected = mutableListOf<Item>()
    //已选择图片地址
    private var uploadImages = mutableListOf<String>()

    companion object{
       const val SELECTED_PHOTO = 0x110
        const val MAX_SELECTED = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wx_photo)


        addItemImage = AddImageAdapter(this,R.layout.image_view_photo, mutableListOf())
        rvImage.layoutManager = GridLayoutManager(this,3)
        addItemImage?.setImageData(listOf())
        rvImage.adapter = addItemImage


        addItemImage?.setRecycleClick(object : AddImageAdapter.OnRecyclerViewItemClickListener{
            @SuppressLint("CheckResult")
            override fun onItemClick(view: View?, position: Int) {
                //添加选择
                if (position==-1){
                    RxPermissions(this@WxPhotoActivity).request(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe { grandted ->
                                if (grandted) {
                                    Matisse.from(this@WxPhotoActivity)
                                            .choose(MimeType.ofImage())
                                            .countable(true)
                                            .capture(true)
                                            .selectedItem(imagesSelected)
                                            .captureStrategy(CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                                            .theme(R.style.Matisse_Dracula)
                                            .maxSelectable(MAX_SELECTED)
                                            .appendSelected(true)
                                            .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                            .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
                                            .imageEngine(GlideEngine())
                                            .showSingleMediaType(true)
                                            .forResult(SELECTED_PHOTO)
                                } else {
                                    ToastUtils.showShort("请打开拍照权限")
                                }
                            }
                } else{
                    //图片上 预览
                    val intent = Intent(this@WxPhotoActivity, SelectedPreviewActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(
                            SelectedItemCollection.STATE_SELECTION,
                            ArrayList<Item>(imagesSelected)
                    )
                    bundle.putInt(SelectedItemCollection.STATE_COLLECTION_TYPE, SelectedItemCollection.COLLECTION_IMAGE)
                    intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle)
                    intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, true)
                    startActivityForResult(intent, MatisseActivity.REQUEST_CODE_PREVIEW)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK){
            //添加图片   //预览图片
            if (requestCode == WxPhotoActivity.SELECTED_PHOTO ) {
                val paths = Matisse.obtainPathResult(data)
                imagesSelected = Matisse.obtainItemsResult(data)
                var pathUris = Matisse.obtainResult(data)
                addItemImage?.setImageData(paths)



            }else if (requestCode == MatisseActivity.REQUEST_CODE_PREVIEW){
                val paths = Matisse.obtainPathResult(data)
                imagesSelected = Matisse.obtainItemsResult(data)
                var pathUris = Matisse.obtainResult(data)
                addItemImage?.setImageData(paths)
            }
        }
    }
}