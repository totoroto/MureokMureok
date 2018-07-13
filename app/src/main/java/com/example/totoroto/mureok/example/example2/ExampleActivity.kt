package com.example.totoroto.mureok.example.example2

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.totoroto.mureok.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_example.*
import java.lang.ref.WeakReference

/**
 *
 * TODO ApiServer로부터 데이터를 받아와서 coffeeNameView와 coffeePriceView에 아메리카노의 이름과 가격이 출력되게 하세요 (AsyncTask를 사용할 것)
 *
 * @author Changwoo Hong(chawoo@hpcnt.com)
 * @since 05 - 7월 - 2018
 */


class ExampleActivity: Activity() {
    private var disposable : Disposable ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        disposable = ApiServer.observeItem().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    textView.text = it.toString()
                }

        IdTask(applicationContext, coffeeNameView, coffeePriceView).execute()
    }

    private class IdTask(context : Context, coffeeNameView : TextView, coffeePriceView: TextView) : AsyncTask<Void, Void, String?>() {
        private val context = WeakReference(context)
        private val coffeeNameView = WeakReference(coffeeNameView)
        private val coffeePriceView = WeakReference(coffeePriceView)

        override fun doInBackground(vararg params: Void?): String? {
            val idList = ApiServer.getCoffeeIds()

            for (i in 0 until idList.size) {
                if (idList[i] == ApiServer.ID_AMERICANO) {
                    return idList[i]
                }
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result != null) {
                NameTask(context, coffeeNameView, result).executeOnExecutor(THREAD_POOL_EXECUTOR)
                PriceTask(context, coffeePriceView, result).executeOnExecutor(THREAD_POOL_EXECUTOR)
            } else {
                Toast.makeText(context.get(), "데이터 없음", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class NameTask(val context : WeakReference<Context>, val coffeeNameView : WeakReference<TextView>, val id : String) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String? {
            return ApiServer.getName(id)
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if(result != null) {
                coffeeNameView.get()?.text = result
            } else {
                Toast.makeText(context.get(), "getName 에러 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class PriceTask(val context : WeakReference<Context>, val coffeePriceView: WeakReference<TextView>, val id: String) : AsyncTask<Void, Void, String?>(){
        var errorCode = ERROR_DEFAULT

        override fun doInBackground(vararg params: Void?): String? {
            var price: String
            val priceCode: String

            try {
                priceCode = ApiServer.getPriceCode(id)

                try {
                    price = ApiServer.getPrice(priceCode).toString()
                } catch (e: IllegalArgumentException) {
                    errorCode = ERROR_PRICE_CODE
                    try{
                        price = ApiServer.getPrice(priceCode).toString()
                    }catch (e: IllegalArgumentException){
                        try{
                            price = ApiServer.getPrice(priceCode).toString()
                        }catch (e: IllegalArgumentException){
                            errorCode = ERROR_PRICE_REPEAT
                            return null
                        }
                    }
                }

            } catch (e: IllegalArgumentException) {
                errorCode = ERROR_PRICE
                return null
            }

            return price
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if(result == null) {
                when (errorCode) {
                    ERROR_PRICE_CODE -> Toast.makeText(context.get(), "getPriceCode 에러 발생", Toast.LENGTH_SHORT).show()
                    ERROR_PRICE -> Toast.makeText(context.get(), "getPrice 에러 발생", Toast.LENGTH_SHORT).show()
                    ERROR_PRICE_REPEAT -> Toast.makeText(context.get(), "getPrice 반복했지만 에러 발생", Toast.LENGTH_SHORT).show()
                }
            } else {
                coffeePriceView.get()?.text = result
            }
        }

        companion object {
            const val ERROR_DEFAULT = 0
            const val ERROR_PRICE_CODE = 2
            const val ERROR_PRICE = 3
            const val ERROR_PRICE_REPEAT = 4
        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}