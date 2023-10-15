package com.onlab.oauth.classes

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.interfaces.IStorageContent


class FolderHistory {
    private var history = mutableListOf<IStorageContent>()

    val size: Int
        get() {
            return history.size
        }

    val current: IStorageContent?
        get() {
            return if (!this.isEmpty()) history.last() else null
        }

    val previous: IStorageContent?
        get() {
            if (0 < size) {
                this.history.removeLast()
            }
            return current
        }

    fun isEmpty(): Boolean {
        return size == 0
    }

    fun add(content: IStorageContent): Boolean {
        val oldSize = size
        if (this.isEmpty()) {
            history.add(content)
        } else if (this.current!!.id != content.id && content.type == ContentType.DIRECTORY) {
            history.add(content)
        }
        return size != oldSize
    }

    fun removeLast(): Boolean {
        if (!this.isEmpty()) {
            history.removeLast()
            return true
        }
        return false
    }
}