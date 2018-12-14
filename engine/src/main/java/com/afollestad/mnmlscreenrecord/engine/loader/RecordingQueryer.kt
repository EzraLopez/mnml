/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.mnmlscreenrecord.engine.loader

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.rxkprefs.Pref
import java.io.File

const val VIDEOS_URI = "content://media/external/video/media"

/**
 * Handles loading recordings from the [recordingsFolderPref] folder.
 *
 * @author Aidan Follestad (@afollestad)
 */
class RecordingQueryer(
  app: Application,
  private val recordingsFolderPref: Pref<String>
) : LifecycleObserver {

  private var isStarted = false
  private val contentResolver = app.contentResolver

  @OnLifecycleEvent(ON_START)
  fun onStart() {
    isStarted = true
  }

  @OnLifecycleEvent(ON_STOP)
  fun onStop() {
    isStarted = false
  }

  @SuppressLint("Recycle")
  fun queryRecordings(): List<Recording> {
    if (!isStarted) {
      return emptyList()
    }

    val folder = File(recordingsFolderPref.get())
    val cursor = contentResolver.query(
        VIDEOS_URI.toUri(), // uri
        null, // projection
        "bucket_display_name = ?", // selection
        arrayOf(folder.name), // selectionArgs
        "date_added DESC" // sortOrder
    )!!

    cursor.use {
      return mutableListOf<Recording>().also { list ->
        if (it.moveToFirst()) {
          do {
            if (!isStarted) {
              break
            }
            list.add(Recording.pull(it))
          } while (cursor.moveToNext())
        }
      }
    }
  }
}
