package com.qwwuyu.file

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.qwwuyu.file.database.DbHelper
import com.qwwuyu.file.database.NoteInfo
import com.qwwuyu.file.utils.*
import com.qwwuyu.file.utils.adapter.SimpleAdapter
import com.qwwuyu.file.utils.adapter.SimpleViewHolder
import com.qwwuyu.file.utils.adapter.VerticalItemDecoration
import kotlinx.android.synthetic.main.a_note.*
import kotlinx.coroutines.GlobalScope
import java.text.SimpleDateFormat
import java.util.*


class NoteActivity : AppCompatActivity() {
    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("MM-dd hh:mm")
    private lateinit var adapter: SimpleAdapter<NoteInfo>

    companion object {
        const val ACTION_REFRESH = "ACTION_REFRESH"

        fun refresh() {
            LocalBroadcastManager.getInstance(WApplication.context)
                .sendBroadcast(Intent(ACTION_REFRESH))
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (ACTION_REFRESH == intent.action) {
                query()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_note)
        SystemBarUtil.setStatusBarColor(this, AppUtils.getColor(R.color.white))
        SystemBarUtil.setStatusBarDarkMode(this, true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            VerticalItemDecoration(
                DisplayUtils.dp2px(0.6f),
                0xffcccccc.toInt()
            )
        )
        adapter = object : SimpleAdapter<NoteInfo>(R.layout.item_note) {
            override fun onBind(position: Int, holder: SimpleViewHolder, data: NoteInfo) {
                holder.setText(R.id.tvTime, format.format(Date(data.time)))
                    .setText(R.id.tvNote, data.text)
                holder.getView<View>(R.id.ivCopy).setOnClickListener {
                    CommUtils.setClipText(data.text)
                    ToastUtil.show("内容已复制到剪切板")
                }
                holder.getView<View>(R.id.ivDelete).setOnClickListener {
                    delete(data)
                }
                holder.itemView.setOnClickListener {
                    showNote(data.text)
                }
            }
        }
        recyclerView.adapter = adapter
        query()

        viewRefresh.setOnClickListener { query() }
        viewAdd.setOnClickListener { insert() }
        viewAdd2.setOnClickListener { insert(CommUtils.getClipText()) }
        viewClear.setOnClickListener { clear() }

        val filter = IntentFilter(ACTION_REFRESH)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun query() {
        lifecycleScope.requestDb({
            DbHelper.noteDao().queryAll()
        }, {
            adapter.setData(it)
        }, {
            ToastUtil.show(it.message)
        })
    }

    private fun clear() {
        AlertDialog.Builder(this)
            .setTitle("温馨提示")
            .setMessage("确定清除所有文本?")
            .setPositiveButton("确定") { _, _ ->
                lifecycleScope.requestDb({
                    DbHelper.noteDao().clear()
                }, {
                    ToastUtil.show("清除成功,共清除${it}条.")
                    query()
                }, {
                    ToastUtil.show(it.message)
                    query()
                })
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun delete(data: NoteInfo) {
        val text = if (data.text.length > 10) {
            data.text.substring(0, 10) + "…"
        } else {
            data.text
        }
        AlertDialog.Builder(this)
            .setTitle("温馨提示")
            .setMessage("确定要删除该条内容?\n内容：${text}")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.requestDb({
                    DbHelper.noteDao().delete(data)
                }, {
                    adapter.removeData(data)
                }, {
                    ToastUtil.show(it.message)
                })
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showNote(text: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("文本")
            .setView(View.inflate(this, R.layout.dialog_note, null))
            .setPositiveButton("复制") { _, _ ->
                CommUtils.setClipText(text)
            }
            .setNegativeButton("关闭", null)
            .show()
        (dialog.findViewById<TextView>(R.id.tvNote)!!).text = text
    }

    private fun insert() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("新增文本")
            .setView(View.inflate(this, R.layout.dialog_edittext, null))
            .setNegativeButton("关闭", null)
            .setPositiveButton("新增") { dialogInterface, _ ->
                val text =
                    ((dialogInterface as Dialog).findViewById<EditText>(R.id.etText)).text.toString()
                insert(text)
            }
            .show()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun insert(text: String) {
        val noteInfo = NoteInfo(time = System.currentTimeMillis(), text = text)
        lifecycleScope.requestDb({
            DbHelper.noteDao().insert(noteInfo)
        }, {
            noteInfo.id = it
            adapter.addData(noteInfo)
        }, {
            ToastUtil.show(it.message)
        })
    }
}