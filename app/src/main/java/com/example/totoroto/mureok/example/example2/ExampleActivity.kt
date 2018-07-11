package com.example.totoroto.mureok.example.example2

import android.app.Activity
import android.os.Bundle
import com.example.totoroto.mureok.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_example.*
import java.lang.ref.WeakReference

/**
 *
 * TODO ApiServer로부터 데이터를 받아와서 textView에 출력해주세요
 * @author Changwoo Hong(chawoo@hpcnt.com)
 * @since 05 - 7월 - 2018
 */
class ExampleActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        ApiServer.observeItem().subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribe{
                                   val tv = WeakReference(textView)
                                   tv.get()?.text = it.toString()
                               }

    }

}