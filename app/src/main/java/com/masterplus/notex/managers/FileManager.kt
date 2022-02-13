package com.masterplus.notex.managers

import android.content.Context
import android.net.Uri
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FileManager{

    fun writeFile(data: UnitedBackup, path:File, fileName:String){
        val file = File(path,fileName)
        val fileOutputStream=FileOutputStream(file)
        val objectOutputStream=ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(data)

        fileOutputStream.close()
        objectOutputStream.close()

    }
    fun readFile(uri: Uri, context: Context): UnitedBackup? {
        return try {
            val objectInputStream = ObjectInputStream(context.contentResolver.openInputStream(uri))
            objectInputStream.readObject() as UnitedBackup
        }catch (ex:Exception){
            null
        }
    }
}